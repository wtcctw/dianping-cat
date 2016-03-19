package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class BucketTest extends ComponentTestCase {
	private MessageCodec m_codec;

	@Test
	public void batchWriteBlockWithManyDomianManyIp() throws IOException {
		long time = System.currentTimeMillis();
		long putTime = 0L;
		BucketManager manager = lookup(BucketManager.class, "local");

		for (int i = 0; i < 10; i++) {
			for (int domainIndex = 0; domainIndex < 10; domainIndex++) {
				String domain = "cat" + domainIndex;
				Bucket bucket = manager.getBucket(domain, "0a010203", 404448, true);

				for (int j = 10; j < 90; j++) {
					String ip = "0a0106" + j;
					Block block1 = new MockBlock(domain, ip, 404448, i);

					try {
						ByteBuf data = block1.getData();
						long putStart = System.currentTimeMillis();
						bucket.puts(data, block1.getMappings());

						long du = System.currentTimeMillis() - putStart;
						putTime = putTime + du;
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}
		}
		long duration = System.currentTimeMillis() - time;
		System.out.println("total time: " + duration);
		System.out.println("put time: " + putTime);

		long closeStart = System.currentTimeMillis();
		manager.closeBuckets(404448 * TimeHelper.ONE_HOUR);
		System.out.println("close time" + (System.currentTimeMillis() - closeStart));
	}

	@Test
	public void batchWriteBlockWithManyIp() throws IOException {
		long start = System.currentTimeMillis();

		String domain = "cat3";
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, "0a010203", 404448, true);

		for (int i = 0; i < 100; i++) {
			for (int j = 10; j < 90; j++) {
				String ip = "0a0106" + j;
				Block block1 = new MockBlock(domain, ip, 404448, i);
				try {
					bucket.puts(block1.getData(), block1.getMappings());
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
		long duration = System.currentTimeMillis() - start;
		System.err.println("duration:" + duration);

		manager.closeBuckets(404448 * TimeHelper.ONE_HOUR);
	}

	@Before
	public void before() {
		File baseDir = new File("target");

		Files.forDir().delete(new File(baseDir, "dump"), true);

		lookup(StorageConfiguration.class).setBaseDataDir(baseDir);
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
	}

	@Test
	public void testWriteAndRead() throws Exception {
		String ip = "0a010203";
		String domain = "mock";
		int hour = 404857;
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 1000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 0; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());

			for (MessageId id : block.getMappings().keySet()) {
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
	}

	@Test
	public void testWriteAndReadInSequence() throws Exception {
		String ip = "0a010203";
		String domain = "mock";
		int hour = 404857;
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 10000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 0; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());
		}

		manager.closeBuckets(hour * TimeHelper.ONE_HOUR);

		bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 10000; i++) {
			for (int index = 0; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
	}

	@Test
	public void testWriteAndReadManyIpsInSequence() throws Exception {
		String domain = "mock";
		int hour = 404857;
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, "0a010203", hour, true);

		for (int i = 0; i < 10000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 0; index < 10; index++) {
				for (int j = 10; j < 15; j++) {
					String ip = "0a0106" + j;
					try {
						MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
						MessageTree tree = TreeHelper.tree(m_codec, id);

						block.pack(id, tree.getBuffer());
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());
		}

		manager.closeBuckets(hour * TimeHelper.ONE_HOUR);

		bucket = manager.getBucket(domain, "0a010203", hour, true);

		for (int i = 0; i < 10000; i++) {
			for (int index = 0; index < 10; index++) {

				for (int j = 10; j < 15; j++) {
					String ip = "0a0106" + j;
					try {
						MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
						ByteBuf buf = bucket.get(id);
						MessageTree tree = m_codec.decode(buf);

						Assert.assertEquals(id.toString(), tree.getMessageId());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Test
	public void testWriteAndReadManyIpsNotInSequence() throws Exception {
		String domain = "mock";
		int hour = 404857;
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, "0a010203", hour, true);

		for (int i = 0; i < 10000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 0; index < 9; index++) {
				for (int j = 10; j < 15; j++) {
					String ip = "0a0106" + j;
					try {
						int msgSeq = i * 10 + index;
						MessageId id = new MessageId(domain, ip, hour, msgSeq);
						MessageTree tree = TreeHelper.tree(m_codec, id);

						block.pack(id, tree.getBuffer());
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());
		}

		for (int i = 0; i < 10000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 9; index < 10; index++) {
				for (int j = 10; j < 15; j++) {
					String ip = "0a0106" + j;
					try {
						int msgSeq = i * 10 + index;
						MessageId id = new MessageId(domain, ip, hour, msgSeq);
						MessageTree tree = TreeHelper.tree(m_codec, id);

						block.pack(id, tree.getBuffer());
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());
		}

		manager.closeBuckets(hour * TimeHelper.ONE_HOUR);

		bucket = manager.getBucket(domain, "0a010203", hour, true);

		for (int i = 0; i < 1000; i++) {
			for (int index = 0; index < 10; index++) {

				for (int j = 10; j < 15; j++) {
					String ip = "0a0106" + j;
					try {
						int msgSeq = i * 10 + index;
						MessageId id = new MessageId(domain, ip, hour, msgSeq);
						ByteBuf buf = bucket.get(id);
						MessageTree tree = m_codec.decode(buf);

						Assert.assertEquals(id.toString(), tree.getMessageId());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Test
	public void testWriteAndReadNotInSequence() throws Exception {
		String ip = "0a010203";
		String domain = "mock";
		int hour = 404857;
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 10000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 0; index < 9; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());
		}

		for (int i = 0; i < 10000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 9; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());
		}

		manager.closeBuckets(hour * TimeHelper.ONE_HOUR);

		bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 10000; i++) {
			for (int index = 0; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
	}

	@Test
	public void testWriteAndReadReloadWriteAndRead() throws Exception {
		String ip = "0a010203";
		String domain = "mock";
		int hour = 404857;
		BucketManager manager = lookup(BucketManager.class, "local");
		Bucket bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 500; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 0; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());

			for (MessageId id : block.getMappings().keySet()) {
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
		
		manager.closeBuckets(hour * TimeHelper.ONE_HOUR);
		bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 500; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (MessageId id : block.getMappings().keySet()) {
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
		
		for (int i = 500; i < 1000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 0; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}

			block.finish();
			bucket.puts(block.getData(), block.getMappings());

			for (MessageId id : block.getMappings().keySet()) {
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
	}

	@Test
	public void testWritePerf() throws IOException {
		long start = System.currentTimeMillis();
		BucketManager manager = lookup(BucketManager.class, "local");

		for (int i = 0; i < 100000; i++) {
			String domain = "mock";
			Bucket bucket = manager.getBucket(domain, "0a010203", 404448, true);
			Block block = new MockBlock(domain, 404448, 10, i);
			try {
				bucket.puts(block.getData(), block.getMappings());
			} catch (Exception e) {
				System.out.println(i);
				e.printStackTrace();
				break;
			}
		}

		manager.closeBuckets(404448 * TimeHelper.ONE_HOUR);

		long duration = System.currentTimeMillis() - start;

		System.out.println("duration:" + duration);
	}

	private static class MockBlock implements Block {
		private String m_domain;

		private int m_hour;

		private int m_capacity;

		private int m_blockSeq;

		private int m_count = 100;

		private int m_perMessageSize = 8;

		private Map<MessageId, Integer> m_mappings = new LinkedHashMap<MessageId, Integer>();

		public static byte[] gZip(byte[] data) {
			byte[] b = null;
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream gzip = new GZIPOutputStream(bos);
				gzip.write(data);
				gzip.finish();
				gzip.close();
				b = bos.toByteArray();
				bos.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return b;
		}

		public MockBlock(String domain, int hour, int count, int index) {
			m_domain = domain;
			m_hour = hour;

			String ip = "10.1.2.4";
			m_capacity = 1536;

			for (int i = 0; i < count; i++) {
				MessageId id = new MessageId(domain, ip, hour, count * index + i);

				m_mappings.put(id, i % m_capacity);
			}
		}

		public MockBlock(String domain, String ip, int hour, int blockSeq) {
			m_domain = domain;
			m_hour = hour;
			m_blockSeq = blockSeq;
			m_capacity = m_count * m_perMessageSize; // 16 per message 8 int length messge 8

			for (int i = 0; i < m_count; i++) {
				int msgIndex = m_count * blockSeq + i;
				MessageId id = new MessageId(domain, ip, hour, msgIndex);

				m_mappings.put(id, i * m_perMessageSize);
			}
		}

		@Override
		public ByteBuf findTree(MessageId id) {
			return null;
		}

		@Override
		public void finish() {
		}

		@Override
		public ByteBuf getData() throws IOException {
			ByteBuf buf = Unpooled.buffer(m_capacity);

			for (int i = 0; i < m_count; i++) {
				buf.writeInt(4);
				int messageContent = m_blockSeq * m_count + i;

				buf.writeInt(messageContent);
				for (int j = 0; j < 10; j++) {
					buf.writeBytes("this is test messge tree".getBytes());
				}
			}

			byte[] dataArray = buf.array();

			// print("source",dataArray);

			byte[] gzipResult = gZip(dataArray);

			// print("gzip", gzipResult);

			// byte[] dataArray2 = unGZip(gzipResult);

			// print("source", dataArray2);

			ByteBuf buff = Unpooled.wrappedBuffer(gzipResult);

			return buff;
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
		public void pack(MessageId id, ByteBuf buf) throws IOException {
			// TODO Auto-generated method stub

		}

		@Override
		public ByteBuf unpack(MessageId id) throws IOException {
			throw new UnsupportedOperationException();
		}

	}
}
