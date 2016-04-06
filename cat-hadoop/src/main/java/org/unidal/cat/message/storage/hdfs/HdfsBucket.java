package org.unidal.cat.message.storage.hdfs;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.FileBuilder;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.cat.metric.Benchmark;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.FileSystemManager;
import com.dianping.cat.message.internal.MessageId;

@Named(type = Bucket.class, value = HdfsBucket.ID, instantiationStrategy = Named.PER_LOOKUP)
public class HdfsBucket implements Bucket, Initializable {
	private static final int SEGMENT_SIZE = 32 * 1024;

	public static final String ID = "hdfs";

	@Inject(HdfsBucket.ID)
	private FileBuilder m_bulider;

	@Inject
	protected FileSystemManager m_manager;

	private DataHelper m_data = new DataHelper();

	private IndexHelper m_index = new IndexHelper();

	@Override
	public void close() {
		if (m_index.isOpen()) {
			m_index.close();
			m_data.close();
		}
	}

	public void flush() {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public ByteBuf get(MessageId id) throws IOException {
		long address = m_index.read(id);

		if (address < 0) {
			return null;
		} else {
			int segmentOffset = (int) (address & 0xFFFFFFL);
			long dataOffset = address >> 24;

			byte[] data = m_data.read(dataOffset);

			DefaultBlock block = new DefaultBlock(id, segmentOffset, data);

			return block.unpack(id);
		}
	}

	@Override
	public void initialize(String domain, String ip, int hour) throws IOException {
		long timestamp = hour * 3600 * 1000L;

		String basePath = domain + '-' + ip;
		String dataFile = "";

		// TODO
		StringBuilder sb = new StringBuilder();
		FileSystem fs = m_manager.getFileSystem(ServerConfigManager.DUMP_DIR, sb);

		FSDataInputStream indexStream = fs.open(new Path(basePath, dataFile + ".idx"));
		FSDataInputStream dataStream = fs.open(new Path(basePath, dataFile + ".dat"));

		m_data.init(dataStream);
		m_index.init(indexStream);
	}

	@Override
	public void puts(ByteBuf data, Map<MessageId, Integer> mappings) throws IOException {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), m_data.getPath());
	}

	private class DataHelper {
		private File m_path;

		private FSDataInputStream m_dataStream;

		public void close() {
			try {
				m_dataStream.close();
			} catch (IOException e) {
				Cat.logError(e);
			}
		}

		public File getPath() {
			return m_path;
		}

		public void init(FSDataInputStream dataStream) throws IOException {
			m_dataStream = dataStream;
		}

		public byte[] read(long dataOffset) throws IOException {
			m_dataStream.seek(dataOffset);

			int len = m_dataStream.readInt();
			byte[] data = new byte[len];

			m_dataStream.readFully(data);

			return data;
		}
	}

	private class IndexHelper {
		private static final int BYTE_PER_MESSAGE = 8;

		private static final int BYTE_PER_ENTRY = 8;

		private static final int MESSAGE_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_MESSAGE;

		private static final int ENTRY_PER_SEGMENT = SEGMENT_SIZE / BYTE_PER_ENTRY;

		private Header m_header = new Header();

		private FSDataInputStream m_indexSteam;

		public void close() {
		}

		public void init(FSDataInputStream indexStream) throws IOException {
			m_indexSteam = indexStream;

			// TODO
			FileSystem hdfs = FileSystem.get(URI.create("fff"), null);
			FileStatus fs = hdfs.getFileStatus(new Path("hdfs:/dump/2015/10/3"));
			long size = fs.getLen();
			int totalHeaders = (int) Math.ceil((size * 1.0 / (ENTRY_PER_SEGMENT * SEGMENT_SIZE)));

			if (totalHeaders == 0) {
				totalHeaders = 1;
			}

			for (int i = 0; i < totalHeaders; i++) {
				m_header.load(i);
			}
		}

		public boolean isOpen() {
			return m_indexSteam != null;
		}

		public long read(MessageId id) throws IOException {
			int index = id.getIndex();
			long position = m_header.getOffset(id.getIpAddressValue(), index);

			m_indexSteam.seek(position);

			long address = m_indexSteam.readLong();

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
				Segment segment = new Segment(m_indexSteam, headBlockIndex * ENTRY_PER_SEGMENT * SEGMENT_SIZE);
				long magicCode = segment.readLong();

				if (magicCode != -1) {
					throw new IOException("Invalid index file: " + m_indexSteam);
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

			private long m_address;

			private ByteBuffer m_buf;

			private Segment(FSDataInputStream channel, long address) throws IOException {
				m_address = address;
				byte[] b = new byte[SEGMENT_SIZE];

				channel.readFully(b);
				m_buf = ByteBuffer.wrap(b);
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

	@Override
	public void setBenchmark(Benchmark benchmark) {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public Benchmark getBechmark() {
		throw new RuntimeException("unsupport operation");
	}

	@Override
	public void initialize() throws InitializationException {

	}

}
