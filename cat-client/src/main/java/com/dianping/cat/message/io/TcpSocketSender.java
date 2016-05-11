package com.dianping.cat.message.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageStatistics;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;

@Named
public class TcpSocketSender implements Task, MessageSender, LogEnabled {

	public static final int SIZE = 5000;

	@Inject(PlainTextMessageCodec.ID)
	private MessageCodec m_codec;

	@Inject
	private MessageStatistics m_statistics;

	@Inject
	private ClientConfigManager m_configManager;

	@Inject
	private MessageIdFactory m_factory;

	private MessageQueue m_queue = new DefaultMessageQueue(SIZE);

	private MessageQueue m_atomicQueue = new DefaultMessageQueue(SIZE);

	private ChannelManager m_channelManager;

	private Logger m_logger;

	private boolean m_active;

	private AtomicInteger m_count = new AtomicInteger();

	private AtomicInteger m_errors = new AtomicInteger();

	private AtomicInteger m_sampleCount = new AtomicInteger();

	private MergeAtomicTask m_mergeTask;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "TcpSocketSender";
	}

	@Override
	public void initialize(List<InetSocketAddress> addresses) {
		m_channelManager = new ChannelManager(m_logger, addresses, m_configManager, m_factory);
		m_mergeTask = new MergeAtomicTask();

		Threads.forGroup("cat").start(this);
		Threads.forGroup("cat").start(m_channelManager);
		Threads.forGroup("cat").start(m_mergeTask);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				m_logger.info("shut down cat client in runtime shut down hook!");
				shutdown();
			}
		});

		StatusExtensionRegister.getInstance().register(new StatusExtension() {

			@Override
			public Map<String, String> getProperties() {
				Map<String, String> map = new HashMap<String, String>();

				map.put("msg-queue", String.valueOf(m_queue.size()));
				map.put("atomic-queue", String.valueOf(m_queue.size()));
				return map;
			}

			@Override
			public String getId() {
				return "client-send-queue";
			}

			@Override
			public String getDescription() {
				return "client-send-queue";
			}
		});
	}

	private void logQueueFullInfo(MessageTree tree) {
		if (m_statistics != null) {
			m_statistics.onOverflowed(tree);
		}

		int count = m_errors.incrementAndGet();

		if (count % 1000 == 0 || count == 1) {
			m_logger.error("Message queue is full in tcp socket sender! Count: " + count);
		}

		tree = null;
	}

	private void offer(MessageTree tree) {
		if (m_configManager.isAtomicMessage(tree)) {
			boolean result = m_atomicQueue.offer(tree);

			if (!result) {
				logQueueFullInfo(tree);
			}
		} else {
			boolean result = m_queue.offer(tree);

			if (!result) {
				logQueueFullInfo(tree);
			}
		}
	}

	@Override
	public void run() {
		m_active = true;

		while (m_active) {
			ChannelFuture channel = m_channelManager.channel();

			if (channel != null) {
				try {
					MessageTree tree = m_queue.poll();

					if (tree != null) {
						sendInternal(tree);
						tree.setMessage(null);
					}

				} catch (Throwable t) {
					m_logger.error("Error when sending message over TCP socket!", t);
				}
			} else {
				try {
					Thread.sleep(5);
				} catch (Exception e) {
					// ignore it
					m_active = false;
				}
			}
		}

		while (true) {
			MessageTree tree = m_queue.poll();

			if (tree != null) {
				sendInternal(tree);
				tree.setMessage(null);
			} else {
				break;
			}
		}
	}

	@Override
	public void send(MessageTree tree) {
		if (!m_configManager.isBlock()) {
			double sampleRatio = m_configManager.getSampleRatio();

			if (tree.canDiscard() && sampleRatio < 1.0) {
				if (sampleRatio > 0) {
					int count = m_sampleCount.incrementAndGet();

					if (count % (1 / sampleRatio) == 0) {
						offer(tree);
					}
				}
			} else {
				offer(tree);
			}
		}
	}

	private void sendInternal(MessageTree tree) {
		if (tree.getMessageId() == null) {
			tree.setMessageId(m_factory.getNextId());
		}

		ChannelFuture future = m_channelManager.channel();

		if (future != null) {
			ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(4 * 1024); // 4K

			m_codec.encode(tree, buf);

			int size = buf.readableBytes();
			Channel channel = future.channel();

			if (m_count.incrementAndGet() % 10 == 0) {
				channel.writeAndFlush(buf);
			} else {
				channel.write(buf);
			}

			if (m_statistics != null) {
				m_statistics.onBytes(size);
			}
		} else {
			offer(tree);
		}
	}

	@Override
	public void shutdown() {
		m_mergeTask.shutdown();
		m_active = false;
		m_channelManager.shutdown();
	}

	public class MergeAtomicTask implements Task {

		private static final int MAX_CHILD_NUMBER = 200;

		private static final int MAX_DURATION = 1000 * 30;

		@Override
		public String getName() {
			return "merge-atomic-task";
		}

		private MessageTree mergeTree(MessageQueue handler) {
			int max = MAX_CHILD_NUMBER;
			DefaultTransaction tran = new DefaultTransaction("_CatMergeTree", "_CatMergeTree", null);
			MessageTree first = handler.poll();

			tran.setStatus(Transaction.SUCCESS);
			tran.setCompleted(true);
			tran.setDurationInMicros(0);
			tran.addChild(first.getMessage());

			while (max >= 0) {
				MessageTree tree = handler.poll();

				if (tree == null) {
					break;
				}
				tran.addChild(tree.getMessage());
				max--;
			}
			((DefaultMessageTree) first).setMessage(tran);
			return first;
		}

		@Override
		public void run() {
			boolean m_active = true;
			while (m_active) {
				if (shouldMerge(m_atomicQueue)) {
					MessageTree tree = mergeTree(m_atomicQueue);
					boolean result = m_queue.offer(tree);

					if (!result) {
						logQueueFullInfo(tree);
					}
				} else {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						break;
					}
				}
			}

			MessageTree tree = mergeTree(m_atomicQueue);

			m_queue.offer(tree);
		}

		private boolean shouldMerge(MessageQueue queue) {
			MessageTree tree = queue.peek();

			if (tree != null) {
				long firstTime = tree.getMessage().getTimestamp();

				if (System.currentTimeMillis() - firstTime > MAX_DURATION || queue.size() >= MAX_CHILD_NUMBER) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void shutdown() {
			m_active = false;
		}
	}
}
