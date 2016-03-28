package org.unidal.cat.message.storage.hdfs;

import io.netty.buffer.ByteBuf;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.FileBuilder.FileType;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkEnabled;
import org.unidal.cat.metric.Metric;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Bucket.class, value = "hdfs", instantiationStrategy = Named.PER_LOOKUP)
public class HdfsBucket implements Bucket, BenchmarkEnabled {
	private static final int SEGMENT_SIZE = 32 * 1024;

	@Inject("local")
	private FileBuilder m_bulider;

	private Metric m_indexMetric;

	private Metric m_dataMetric;

	private DataHelper m_data = new DataHelper();

	private IndexHelper m_index = new IndexHelper();

	private Benchmark m_benchmark;

	@Override
	public void close() {
		if (m_index.isOpen()) {
			m_indexMetric.start();
			m_index.close();
			m_indexMetric.end();

			m_dataMetric.start();
			m_data.close();
			m_dataMetric.end();
		}
	}

	public void flush() {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public ByteBuf get(MessageId id) throws IOException {
		m_indexMetric.start();
		long address = m_index.read(id);
		m_indexMetric.end();

		if (address < 0) {
			return null;
		} else {
			int segmentOffset = (int) (address & 0xFFFFFFL);
			long dataOffset = address >> 24;

			m_dataMetric.start();
			byte[] data = m_data.read(dataOffset);
			m_dataMetric.end();

			DefaultBlock block = new DefaultBlock(id, segmentOffset, data);

			return block.unpack(id);
		}
	}

	@Override
	public Benchmark getBechmark() {
		return m_benchmark;
	}

	@Override
	public void initialize(String domain, String ip, int hour) throws IOException {
		long timestamp = hour * 3600 * 1000L;
		Date startTime = new Date(timestamp);
		File dataPath = m_bulider.getFile(domain, startTime, ip, FileType.DATA);
		File indexPath = m_bulider.getFile(domain, startTime, ip, FileType.INDEX);

		m_dataMetric.start();
		m_data.init(dataPath);
		m_dataMetric.end();

		m_indexMetric.start();
		m_index.init(indexPath);
		m_indexMetric.end();
	}

	@Override
	public void puts(ByteBuf data, Map<MessageId, Integer> mappings) throws IOException {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public void setBenchmark(Benchmark benchmark) {
		m_benchmark = benchmark;
		m_indexMetric = benchmark.get("index");
		m_dataMetric = benchmark.get("data");
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), m_data.getPath());
	}

	private class DataHelper {
		private File m_path;

		private RandomAccessFile m_file;

		private DataOutputStream m_out;

		public void close() {
			try {
				m_file.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				if (m_out != null) {
					m_out.close();
				}
			} catch (IOException e) {
				Cat.logError(e);
			}

			m_file = null;
		}

		public File getPath() {
			return m_path;
		}

		public void init(File dataPath) throws IOException {
			m_path = dataPath;
			m_path.getParentFile().mkdirs();

			m_out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(m_path, true), SEGMENT_SIZE));
			m_file = new RandomAccessFile(m_path, "r"); // read-only
		}

		public byte[] read(long dataOffset) throws IOException {
			m_out.flush();
			m_file.seek(dataOffset);

			int len = m_file.readInt();
			byte[] data = new byte[len];

			m_file.readFully(data);

			return data;
		}
	}

	private class IndexHelper {
		private static final int BYTE_PER_MESSAGE = 8;

		private static final int BYTE_PER_ENTRY = 8;

		private static final int MESSAGE_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_MESSAGE;

		private static final int ENTRY_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_ENTRY;

		private RandomAccessFile m_file;

		private File m_path;

		private FileChannel m_indexChannel;

		private Header m_header = new Header();

		public void close() {
			try {
				m_indexChannel.force(false);
				m_indexChannel.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			try {
				m_file.close();
			} catch (IOException e) {
				Cat.logError(e);
			}

			m_file = null;
		}

		public void init(File indexPath) throws IOException {
			m_path = indexPath;
			m_path.getParentFile().mkdirs();

			// read-write without meta sync
			m_file = new RandomAccessFile(m_path, "rwd");
			m_indexChannel = m_file.getChannel();

			long size = m_file.length();
			int totalHeaders = (int) Math.ceil((size * 1.0 / (ENTRY_PER_SEGMENT * SEGMENT_SIZE)));

			if (totalHeaders == 0) {
				totalHeaders = 1;
			}

			for (int i = 0; i < totalHeaders; i++) {
				m_header.load(i);
			}
		}

		public boolean isOpen() {
			return m_file != null;
		}

		public long read(MessageId id) throws IOException {
			int index = id.getIndex();
			long position = m_header.getOffset(id.getIpAddressValue(), index);

			m_file.seek(position);

			long address = m_file.readLong();

			return address;
		}

		private class Header {
			private Map<Integer, Map<Integer, Integer>> m_table = new LinkedHashMap<Integer, Map<Integer, Integer>>();

			private int m_nextSegment;

			private Integer findSegment(int ip, int index) throws IOException {
				Map<Integer, Integer> map = m_table.get(ip);

				if (map != null) {
					return map.get(index);
				}
				return null;
			}

			public long getOffset(int ip, int seq) throws IOException {
				int segmentIndex = seq / MESSAGE_PER_SEGMENT;
				int segmentOffset = (seq % MESSAGE_PER_SEGMENT) * BYTE_PER_MESSAGE;
				Integer segmentId = findSegment(ip, segmentIndex);

				if (segmentId != null) {
					long offset = segmentId.intValue() * SEGMENT_SIZE + segmentOffset;

					return offset;
				} else {
					return -1;
				}
			}

			public void load(int headBlockIndex) throws IOException {
				Segment segment = new Segment(m_indexChannel, headBlockIndex * ENTRY_PER_SEGMENT * SEGMENT_SIZE);
				long magicCode = segment.readLong();

				if (magicCode != -1) {
					throw new IOException("Invalid index file: " + m_path);
				}

				m_nextSegment = 1 + ENTRY_PER_SEGMENT * headBlockIndex;

				int readerIndex = 1;

				while (readerIndex < ENTRY_PER_SEGMENT) {
					int ip = segment.readInt();
					int index = segment.readInt();

					readerIndex++;

					if (ip != 0) {
						Map<Integer, Integer> map = m_table.get(ip);

						if (map == null) {
							map = new HashMap<Integer, Integer>();
							m_table.put(ip, map);
						}

						Integer segmentNo = map.get(index);

						if (segmentNo == null) {
							segmentNo = m_nextSegment++;

							map.put(index, segmentNo);
						}
					} else {
						break;
					}
				}
			}
		}

		private class Segment {
			private FileChannel m_segmentChannel;

			private long m_address;

			private ByteBuffer m_buf;

			private Segment(FileChannel channel, long address) throws IOException {
				m_segmentChannel = channel;
				m_address = address;
				m_buf = ByteBuffer.allocate(SEGMENT_SIZE);
				m_buf.mark();
				m_segmentChannel.read(m_buf, address);
				m_buf.reset();
			}

			public int readInt() throws IOException {
				return m_buf.getInt();
			}

			public long readLong() throws IOException {
				return m_buf.getLong();
			}

			@Override
			public String toString() {
				return String.format("%s[address=%s]", getClass().getSimpleName(), m_address);
			}
		}
	}

}
