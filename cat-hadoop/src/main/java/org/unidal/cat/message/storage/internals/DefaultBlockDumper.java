package org.unidal.cat.message.storage.internals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.QueueFullException;
import org.unidal.cat.message.storage.Block;
import org.unidal.cat.message.storage.BlockDumper;
import org.unidal.cat.message.storage.BlockWriter;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = BlockDumper.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultBlockDumper extends ContainerHolder implements BlockDumper, LogEnabled {
	private List<BlockingQueue<Block>> m_queues = new ArrayList<BlockingQueue<Block>>();

	private List<BlockWriter> m_writers = new ArrayList<BlockWriter>();

	private int m_failCount = -1;

	private Logger m_logger;

	@Override
	public void awaitTermination() throws InterruptedException {
		int index = 0;
		
		while (true && index < 100) {
			boolean allEmpty = true;

			for (BlockingQueue<Block> queue : m_queues) {
				if (!queue.isEmpty()) {
					allEmpty = false;
					break;
				}
			}

			if (allEmpty) {
				break;
			}

			TimeUnit.MILLISECONDS.sleep(10);
			
			index++;
		}

		for (BlockWriter writer : m_writers) {
			writer.shutdown();
			super.release(writer);
		}
	}

	@Override
	public void dump(Block block) throws IOException {
		String domain = block.getDomain();
		int hash = Math.abs(domain.hashCode());
		int index = hash % m_writers.size();
		BlockingQueue<Block> queue = m_queues.get(index);
		boolean success = queue.offer(block);

		if (!success && (++m_failCount % 100) == 0) {
			Cat.logError(new QueueFullException("Error when adding block to queue, fails: " + m_failCount));
			m_logger.info("block dump queue is full " + m_failCount);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize(int hour) {
		int threads = 10;

		for (int i = 0; i < threads; i++) {
			BlockingQueue<Block> queue = new ArrayBlockingQueue<Block>(10000);
			BlockWriter writer = lookup(BlockWriter.class);

			m_queues.add(queue);
			m_writers.add(writer);

			writer.initialize(hour, i, queue);
			Threads.forGroup("Cat").start(writer);
		}
	}
}
