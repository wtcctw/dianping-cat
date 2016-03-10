package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public class BucketTest extends ComponentTestCase {
	@Before
	public void before() {
		StorageConfiguration config = lookup(StorageConfiguration.class);

		config.setBaseDataDir(new File("target"));
	}

	@Test
	public void testRead() throws IOException {
		String domain = "cat";
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, 404448, true);
		String ip1 = "0a010680";
		String ip2 = "0a010681";
		String ip3 = "0a010682";

		for (int i = 0; i < 100; i++) {
			Block block1 = new MockBlock(domain, ip1, 404448, i);
			// Block block2 = new MockBlock(domain, ip2, 404448, 10, i);
			// Block block3 = new MockBlock(domain, ip3, 404448, 10, i);

			try {
				bucket.put(block1);
				// bucket.put(block2);
				// bucket.put(block3);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		manager.closeBuckets();

		String id = "cat-0a010680-404448-";

		for (int i = 0; i < 100 * 100; i++) {
			Block block = bucket.get(MessageId.parse(id + i));
		}
	}

	@Test
	public void testWrite() throws IOException {
		BucketManager manager = lookup(BucketManager.class, "local");

		for (int i = 0; i < 3000000; i++) {
			String domain = "mock" + (i % 9);
			Bucket bucket = manager.getBucket(domain, 404448, true);
			Block block = new MockBlock(domain, 404448, 10, i / 9);

			try {
				bucket.put(block);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		manager.closeBuckets();
	}

	private static class MockBlock implements Block {
		private String m_domain;

		private int m_hour;

		private int m_capacity;

		private int m_blockSeq;

		private int m_count = 100;

		private Map<MessageId, Integer> m_mappings = new HashMap<MessageId, Integer>();

		public MockBlock(String domain, String ip, int hour, int blockSeq) {
			m_domain = domain;
			m_hour = hour;
			m_blockSeq = blockSeq;
			m_capacity = m_count * 16; // 16 per message 8 int length messge 8

			for (int i = 0; i < m_count; i++) {
				int msgIndex = m_count * blockSeq + i;
				MessageId id = new MessageId(domain, ip, hour, msgIndex);

				m_mappings.put(id, i * 16);
			}
		}

		public MockBlock(String domain, int hour, int count, int index) {
			m_domain = domain;
			m_hour = hour;

			String ip = "10.1.2.4";

			for (int i = 0; i < count; i++) {
				MessageId id = new MessageId(domain, ip, hour, count * index + i);

				m_mappings.put(id, i % m_capacity);
			}
		}

		@Override
		public void finish() {
		}

		@Override
		public ByteBuf getData() throws IOException {
			ByteBuf buf = Unpooled.buffer(m_capacity);

			for (int i = 0; i < m_count; i++) {
				buf.writeInt(8);
				int messageContent = m_blockSeq * m_count + i;

				buf.writeInt(messageContent);
			}

			ByteBufOutputStream os = new ByteBufOutputStream(buf);
			GZIPOutputStream out = new GZIPOutputStream(os, 1024);

			out.flush();
			out.close();

			int len = buf.readableBytes();
			byte[] array = buf.array();
			for(int i=0;i<len;i++){
				System.out.print(array[i]+' ');
			}
			System.out.println();
			return buf;
		}

		@Override
		public String getDomain() {
			return m_domain;
		}

		@Override
		public int getHour() {
			return m_hour;
		}

		@Override
		public Map<MessageId, Integer> getMappings() {
			return m_mappings;
		}

		@Override
		public boolean isFull() {
			return true;
		}

		@Override
		public ByteBuf unpack(MessageId id) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void pack(MessageId id, MessageTree tree) throws IOException {
		}

		@Override
		public MessageTree findTree(MessageId id) {
			return null;
		}
	}
}
