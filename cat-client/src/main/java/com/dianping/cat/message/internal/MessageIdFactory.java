package com.dianping.cat.message.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.unidal.helper.Splitters;

import com.dianping.cat.configuration.NetworkInterfaceManager;

public class MessageIdFactory {
	private volatile long m_timestamp = getTimestamp();

	private volatile AtomicInteger m_index;

	private String m_domain;

	private String m_ipAddress;

	private MappedByteBuffer m_byteBuffer;

	private RandomAccessFile m_markFile;

	private Map<String, AtomicInteger> m_maps = new LinkedHashMap<String, AtomicInteger>(100);

	public static final long HOUR = 3600 * 1000L;

	public void close() {
		try {
			saveMark();
			m_markFile.close();
		} catch (Exception e) {
			// ignore it
		}
	}

	private File createMarkFile(String domain) {
		File mark = new File("/data/appdatas/cat/", "cat-" + domain + ".mark");

		if (!mark.exists()) {
			boolean success = true;
			try {
				success = mark.createNewFile();
			} catch (Exception e) {
				success = false;
			}
			if (!success) {
				mark = createTempFile(domain);
			}
		} else if (!mark.canWrite()) {
			mark = createTempFile(domain);
		}
		return mark;
	}

	private File createTempFile(String domain) {
		String tmpDir = System.getProperty("java.io.tmpdir");
		File mark = new File(tmpDir, "cat-" + domain + ".mark");

		return mark;
	}

	public String getNextId() {
		long timestamp = getTimestamp();

		if (timestamp != m_timestamp) {
			m_index.set(0);
			m_timestamp = timestamp;
		}

		int index = m_index.getAndIncrement();

		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		sb.append(m_domain).append('-').append(m_ipAddress).append('-').append(timestamp).append('-').append(index);

		return sb.toString();
	}

	public String getNextMapId() {
		return getNextMapId("default");
	}

	public String getNextMapId(String domain) {
		long timestamp = getTimestamp();

		if (timestamp != m_timestamp) {
			synchronized (m_maps) {
				for (Entry<String, AtomicInteger> entry : m_maps.entrySet()) {
					entry.getValue().set(0);
				}
			}
			m_timestamp = timestamp;
		}

		AtomicInteger value = m_maps.get(domain);

		if (value == null) {
			synchronized (m_maps) {
				value = m_maps.get(domain);

				if (value == null) {
					value = new AtomicInteger(0);
					m_maps.put(domain, value);
				}
			}
		}
		int index = value.getAndIncrement();

		StringBuilder sb = new StringBuilder(m_domain.length() + 32);

		sb.append(domain).append('-').append(m_ipAddress).append('-').append(timestamp).append('-').append(index);

		return sb.toString();

	}

	protected long getTimestamp() {
		long timestamp = MilliSecondTimer.currentTimeMillis();

		return timestamp / HOUR; // version 2
	}

	public void initialize(String domain) throws IOException {
		m_domain = domain;

		if (m_ipAddress == null) {
			String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			List<String> items = Splitters.by(".").noEmptyItem().split(ip);
			byte[] bytes = new byte[4];

			for (int i = 0; i < 4; i++) {
				bytes[i] = (byte) Integer.parseInt(items.get(i));
			}

			StringBuilder sb = new StringBuilder(bytes.length / 2);

			for (byte b : bytes) {
				sb.append(Integer.toHexString((b >> 4) & 0x0F));
				sb.append(Integer.toHexString(b & 0x0F));
			}

			m_ipAddress = sb.toString();
		}
		File mark = createMarkFile(domain);

		m_markFile = new RandomAccessFile(mark, "rw");
		m_byteBuffer = m_markFile.getChannel().map(MapMode.READ_WRITE, 0, 1024 * 1024);

		if (m_byteBuffer.limit() > 0) {
			long lastTimestamp = m_byteBuffer.getLong();
			int index = m_byteBuffer.getInt();

			if (lastTimestamp == m_timestamp) { // for same hour
				m_index = new AtomicInteger(index + 1000);

				int mapLength = m_byteBuffer.getInt();
				
				System.err.println("index:"+m_index);

				for (int i = 0; i < mapLength; i++) {
					int domainLength = m_byteBuffer.getInt();
					byte[] domainArray = new byte[domainLength];

					m_byteBuffer.get(domainArray);
					int value = m_byteBuffer.getInt();

					m_maps.put(new String(domainArray), new AtomicInteger(value + 1000));
				}

				System.out.println(m_maps);
			} else {
				m_index = new AtomicInteger(0);
			}

		}

		saveMark();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				saveMark();
			}
		});
	}

	protected void resetIndex() {
		m_index.set(0);
	}

	public void saveMark() {
		try {
			m_byteBuffer.rewind();
			m_byteBuffer.putLong(m_timestamp);
			m_byteBuffer.putInt(m_index.get());
			m_byteBuffer.putInt(m_maps.size());

			for (Entry<String, AtomicInteger> entry : m_maps.entrySet()) {
				byte[] bytes = entry.getKey().toString().getBytes();

				m_byteBuffer.putInt(bytes.length);
				m_byteBuffer.put(bytes);
				m_byteBuffer.putInt(entry.getValue().get());
			}

			m_byteBuffer.force();
		} catch (Exception e) {
			// ignore it
		}
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

}
