package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.unidal.cat.message.storage.internals.DefaultBlock;
import org.unidal.cat.metric.Benchmark;
import org.unidal.cat.metric.BenchmarkManager;
import org.unidal.cat.metric.Metric;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class BucketTest extends ComponentTestCase {
	private MessageCodec m_codec;

	private BenchmarkManager m_benchmarkManager;

	@Before
	public void before() {
		File baseDir = new File("target");

		Files.forDir().delete(new File(baseDir, "dump"), true);

		lookup(StorageConfiguration.class).setBaseDataDir(baseDir);
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		m_benchmarkManager = lookup(BenchmarkManager.class);
		System.setProperty("devMode", "true");
	}

	@Test
	public void testManyDomainIpWrite() throws Exception {
		TreeHelper.init(m_codec);

		long start = System.currentTimeMillis();
		MessageDumperManager manager = lookup(MessageDumperManager.class);
		int hour = 405746;
		MessageDumper dumper = manager.findOrCreate(hour);

		for (int i = 0; i < 1000000; i++) {
			long interStart = System.currentTimeMillis();

			for (int domainIndex = 0; domainIndex < 300; domainIndex++) {
				String domain = "domain" + domainIndex;

				for (int ipIndex = 0; ipIndex < 9; ipIndex++) {
					String ip = "0a01020" + ipIndex;
					MessageId id = new MessageId(domain, ip, hour, i * 10 + ipIndex);
					MessageTree tree = TreeHelper.cacheTree(m_codec, id);

					((DefaultMessageTree) tree).setMessageId(id.toString());
					tree.setFormatMessageId(id);

					dumper.process(tree);
				}
			}

			long duration = System.currentTimeMillis() - interStart;

			if (i % 100 == 0) {
				System.out.println("duration:" + duration + ":qps:" + 3000 * 1000 / duration);
			}
		}

		long duration = System.currentTimeMillis() - start;
		System.out.println("write cost" + duration);
	}

	@Test
	public void testWithManyThreads() {
		TreeHelper.init(m_codec);
		long start = System.currentTimeMillis();
		List<TestThread> threads = new ArrayList<TestThread>();

		for (int i = 0; i < 1; i++) {
			TestThread task = new TestThread(i);

			Threads.forGroup("cat").start(task);
			threads.add(task);
		}

		while (true) {
			try {
				Thread.sleep(10000);

				long total = 0;

				for (TestThread thread : threads) {
					total = total + thread.m_totalCount.get();
				}
				long duration = System.currentTimeMillis() - start;
				System.out.println("qps:" + total * 1000 / duration);
			} catch (InterruptedException e) {
				break;
			}
		}
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
			bucket.puts(block.getData(), block.getOffsets());

			for (MessageId id : block.getOffsets().keySet()) {
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
			bucket.puts(block.getData(), block.getOffsets());
		}

		manager.closeBuckets(hour);

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
			bucket.puts(block.getData(), block.getOffsets());
		}

		manager.closeBuckets(hour);

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
			bucket.puts(block.getData(), block.getOffsets());
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
			bucket.puts(block.getData(), block.getOffsets());
		}

		manager.closeBuckets(hour);

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
			bucket.puts(block.getData(), block.getOffsets());
		}

		for (int i = 0; i < 10000; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (int index = 9; index < 10; index++) {
				MessageId id = new MessageId(domain, ip, hour, i * 10 + index);
				MessageTree tree = TreeHelper.tree(m_codec, id);

				block.pack(id, tree.getBuffer());
			}

			block.finish();
			bucket.puts(block.getData(), block.getOffsets());
		}

		manager.closeBuckets(hour);

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
			bucket.puts(block.getData(), block.getOffsets());

			for (MessageId id : block.getOffsets().keySet()) {
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}

		manager.closeBuckets(hour);
		bucket = manager.getBucket(domain, ip, hour, true);

		for (int i = 0; i < 500; i++) {
			Block block = new DefaultBlock(domain, hour);

			for (MessageId id : block.getOffsets().keySet()) {
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
			bucket.puts(block.getData(), block.getOffsets());

			for (MessageId id : block.getOffsets().keySet()) {
				ByteBuf buf = bucket.get(id);
				MessageTree tree = m_codec.decode(buf);

				Assert.assertEquals(id.toString(), tree.getMessageId());
			}
		}
	}

	@Test
	public void testWritePerf() throws IOException {
		BucketManager manager = lookup(BucketManager.class, "local");
		Benchmark bm = m_benchmarkManager.get("bucket");
		Metric mb = bm.get("build");
		Metric mo = bm.get("other");
		Metric mc = bm.get("close");

		for (int i = 0; i < 100000; i++) {
			mo.start();

			String domain = "mock";
			Bucket bucket = manager.getBucket(domain, "0a010203", 404448, true);

			mo.end();

			mb.start();
			Block block = new MockBlock(domain, 404448, 10, i);
			mb.end();

			mo.start();
			try {
				bucket.puts(block.getData(), block.getOffsets());
			} catch (Exception e) {
				System.out.println(i);
				e.printStackTrace();
				break;
			}
			mo.end();
		}

		mc.start();
		manager.closeBuckets(404448);
		mc.end();

		bm.print();
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
		public void clear() {
		}

		@Override
		public ByteBuf find(MessageId id) {
			// TODO Auto-generated method stub
			return null;
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
		public Map<MessageId, Integer> getOffsets() {
			return m_mappings;
		}

		@Override
		public boolean isFull() {
			return true;
		}

		@Override
		public void pack(MessageId id, ByteBuf buf) throws IOException {
		}

		@Override
		public ByteBuf unpack(MessageId id) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
      public Map<MessageId, MessageId> getMappIds() {
	      return null;
      }

		@Override
      public void map(MessageId from, MessageId to) {
      }

	}

	public class TestThread implements Task {

		public AtomicInteger m_totalCount = new AtomicInteger(0);

		public int m_threadIndex;

		public TestThread(int index) {
			m_threadIndex = index;
		}

		@Override
		public String getName() {
			return "task";
		}

		@Override
		public void run() {
			MessageDumperManager manager = lookup(MessageDumperManager.class);
			int hour = 405746;
			MessageDumper dumper = manager.findOrCreate(hour);

			for (int i = 0; i < 10000000; i++) {
				for (int domainIndex = 0; domainIndex < 300; domainIndex++) {
					String domain = "domain_" + domainIndex;

					for (int ipIndex = 0; ipIndex < 10; ipIndex++) {
						String ip = "0a01020" + ipIndex;
						MessageId id = new MessageId(domain, ip, hour, i * 10 + ipIndex);
						MessageTree tree = TreeHelper.cacheTree(m_codec, id);

						((DefaultMessageTree) tree).setMessageId(id.toString());
						tree.setFormatMessageId(id);

						dumper.process(tree);
						m_totalCount.addAndGet(1);
					}
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
