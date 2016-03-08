package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	public void testWrite() throws IOException {
		BucketManager manager = lookup(BucketManager.class, "local");

		for (int i = 0; i < 3000000; i++) {
			String domain = "mock" + (i % 9);
			Bucket bucket = manager.getBucket(domain, 404448, true);


			Block block = new MockBlock(domain, 404448, 10, i / 9);

			try {
				bucket.put(block);
			} catch (Exception e) {
				System.out.println(i);
				e.printStackTrace();
				break;
			}
		}

		manager.closeBuckets();
	}

	private static class MockBlock implements Block {
		private String m_domain;

		private int m_hour;

		private int m_capacity = 1536;

		private Map<MessageId, Integer> m_mappings = new HashMap<MessageId, Integer>();

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

			buf.writeZero(m_capacity);
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
	      // TODO Auto-generated method stub
      }

		@Override
      public MessageTree findTree(MessageId id) {
	      // TODO Auto-generated method stub
	      return null;
      }
	}
}
