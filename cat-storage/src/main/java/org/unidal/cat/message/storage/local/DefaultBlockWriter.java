package org.unidal.cat.message.storage.local;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

@Named(type = BlockWriter.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultBlockWriter implements BlockWriter {
	@Inject
	private BucketManager m_manager;

	private int m_index;

	private BlockingQueue<Block> m_queue;

	private AtomicBoolean m_enabled;

	private CountDownLatch m_latch;

	private int m_count;

	@Override
	public String getName() {
		return getClass().getSimpleName() + "-" + m_index;
	}

	@Override
	public void shutdown() {
		m_enabled.set(false);

		try {
			m_latch.await();
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	@Override
	public void run() {
		Block block;

		try {
			while (m_enabled.get()) {
				block = m_queue.poll(5, TimeUnit.MILLISECONDS);

				if (block != null) {
					try {
						String domain = block.getDomain();
						Bucket bucket = m_manager.getBucket(domain, block.getHour(), true);

						if ((m_count++) % 1 == 0) {
							Transaction t = Cat.newTransaction("Block", domain);

							try {
								bucket.put(block);
							} catch (Exception e) {
								t.setStatus(e);
								Cat.logError(e);
							}
							t.setStatus(Transaction.SUCCESS);
							t.complete();
						} else {
							bucket.put(block);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		} catch (InterruptedException e) {
			// ignore it
		}

		System.out.println(getClass().getSimpleName() + "-" + m_index + " is shutdown");
		m_latch.countDown();
	}

	@Override
	public void initialize(int index, BlockingQueue<Block> queue) {
		m_index = index;
		m_queue = queue;
		m_enabled = new AtomicBoolean(true);
		m_latch = new CountDownLatch(1);
	}
}
