package org.unidal.cat.message.storage.internals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.Index;
import org.unidal.cat.message.storage.IndexManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Transaction;

@Named(type = BlockWriter.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultBlockWriter implements BlockWriter {

	@Inject("local")
	private BucketManager m_bucketManager;

	@Inject("local")
	private IndexManager m_indexManager;

	private int m_index;

	private BlockingQueue<Block> m_queue;

	private long m_hour;

	private int m_count;

	private AtomicBoolean m_enabled;

	private CountDownLatch m_latch;

	@Override
	public String getName() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return getClass().getSimpleName() + " " + sdf.format(new Date(TimeUnit.HOURS.toMillis(m_hour))) + "-" + m_index;
	}

	@Override
	public void initialize(int hour, int index, BlockingQueue<Block> queue) {
		m_hour = hour;
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
		m_latch = new CountDownLatch(1);
	}

	private void processBlock(String ip, Block block) {
		try {
			Bucket bucket = m_bucketManager.getBucket(block.getDomain(), ip, block.getHour(), true);
			boolean monitor = (++m_count) % 100 == 0;

			if (monitor) {
				Transaction t = Cat.newTransaction("Block", block.getDomain());

				bucket.puts(block.getData(), block.getOffsets());

				t.setStatus(Transaction.SUCCESS);
				t.complete();
			} else {
				bucket.puts(block.getData(), block.getOffsets());
			}

			Index index = m_indexManager.getIndex(block.getDomain(), ip, block.getHour(), true);

			if (monitor) {
				Transaction t = Cat.newTransaction("Index", block.getDomain());

				index.maps(block.getMappIds());

				t.setStatus(Transaction.SUCCESS);
				t.complete();
			} else {
				index.maps(block.getMappIds());
			}
		} catch (Exception e) {
			Cat.logError(e);
		} catch (Error e) {
			Cat.logError(e);
		} finally {
			block.clear();
		}
	}

	@Override
	public void run() {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		try {
			while (m_enabled.get() || !m_queue.isEmpty()) {
				Block block = m_queue.poll(5, TimeUnit.MILLISECONDS);

				if (block != null) {
					processBlock(ip, block);
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}

		m_latch.countDown();
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);
	}

	@Override
	public void await() {
		try {
			m_latch.await();
		} catch (InterruptedException e) {
			// ignore it
		}
		while (true) {
			Block block = m_queue.poll();

			if (block != null) {
				processBlock(NetworkInterfaceManager.INSTANCE.getLocalHostAddress(), block);
			} else {
				break;
			}
		}
	}
	
}
