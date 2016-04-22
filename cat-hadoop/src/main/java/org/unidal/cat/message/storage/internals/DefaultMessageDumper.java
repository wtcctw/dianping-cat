package org.unidal.cat.message.storage.internals;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.QueueFullException;
import org.unidal.cat.message.storage.BlockDumperManager;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.IndexManager;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageProcessor;
import org.unidal.cat.message.storage.TokenMappingManager;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = MessageDumper.class, instantiationStrategy = Named.PER_LOOKUP)
public class DefaultMessageDumper extends ContainerHolder implements MessageDumper, LogEnabled {
	@Inject
	private BlockDumperManager m_blockDumperManager;

	@Inject("local")
	private BucketManager m_bucketManager;

	@Inject("local")
	private IndexManager m_indexManager;

	@Inject("local")
	private TokenMappingManager m_tokenManager;

	@Inject
	private ServerStatisticManager m_statisticManager;

	private List<BlockingQueue<MessageTree>> m_queues = new ArrayList<BlockingQueue<MessageTree>>();

	private List<MessageProcessor> m_processors = new ArrayList<MessageProcessor>();

	private AtomicInteger m_failCount = new AtomicInteger(-1);

	private Logger m_logger;

	@Override
	public void awaitTermination(int hour) throws InterruptedException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		String date = sdf.format(new Date(hour * TimeHelper.ONE_HOUR));

		closeMessageProcessor();

		m_logger.info("starting close dumper manager " + date);
		m_blockDumperManager.close(hour);

		m_logger.info("starting close bucket manager " + date);
		m_bucketManager.closeBuckets(hour);

		m_logger.info("starting close index manager " + date);
		m_indexManager.close(hour);

		m_logger.info("starting close token manager " + date);
		m_tokenManager.close(hour);
	}

	private void closeMessageProcessor() throws InterruptedException {
		while (true) {
			boolean allEmpty = true;

			for (BlockingQueue<MessageTree> queue : m_queues) {
				if (!queue.isEmpty()) {
					allEmpty = false;
					break;
				}
			}

			if (allEmpty) {
				break;
			} else {
				TimeUnit.MILLISECONDS.sleep(1);
			}
		}

		for (MessageProcessor processor : m_processors) {
			processor.shutdown();
			super.release(processor);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private int getIndex(String domain) {
		int hash = Math.abs(domain.hashCode());
		int index = hash % (m_processors.size() - 1); // last one for message overflow

		return index;
	}

	public void initialize(int hour) {
		int processThreads = 12;

		for (int i = 0; i < processThreads; i++) {
			BlockingQueue<MessageTree> queue = new ArrayBlockingQueue<MessageTree>(10000);
			MessageProcessor processor = lookup(MessageProcessor.class);

			m_queues.add(queue);
			m_processors.add(processor);

			processor.initialize(hour, i, queue);
			Threads.forGroup("Cat").start(processor);
		}
	}

	@Override
	public void process(MessageTree tree) {
		MessageId id = tree.getFormatMessageId();
		String domain = id.getDomain();
		int index = getIndex(domain);
		BlockingQueue<MessageTree> queue = m_queues.get(index);

		if (!queue.offer(tree)) { // overflow
			BlockingQueue<MessageTree> last = m_queues.get(m_queues.size() - 1);
			boolean success = last.offer(tree);

			if (!success) {
				m_statisticManager.addMessageDumpLoss(1);

				if ((m_failCount.incrementAndGet() % 100) == 0) {
					Cat.logError(new QueueFullException("Error when adding message to queue, fails: " + m_failCount));

					m_logger.info("message tree queue is full " + m_failCount);
				}
				// tree.getBuffer().release();
			}
		}
	}
}
