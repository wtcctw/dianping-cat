package com.dianping.cat.message.spi;

import java.util.concurrent.atomic.AtomicInteger;

import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.io.ChannelManager;
import com.dianping.cat.message.io.DefaultMessageQueue;

public class MessageQueueHandler {

	private MessageQueue m_queue;

	private MessageIdFactory m_factory;

	private AtomicInteger m_count = new AtomicInteger();

	public MessageQueueHandler(int size, MessageIdFactory factory) {
		m_queue = new DefaultMessageQueue(size);
		m_factory = factory;
	}

	public int getSize() {
		return m_queue.size();
	}

	public boolean offer(MessageTree tree) {
		return m_queue.offer(tree);
	}

	public boolean offer(MessageTree tree, ChannelManager manager) {
		if (!manager.isBlock()) {
			double sampleRatio = manager.getSample();

			if (tree.isSample() && sampleRatio < 1.0) {
				if (sampleRatio > 0) {
					int count = m_count.incrementAndGet();

					if (count % (1 / sampleRatio) == 0) {
						return m_queue.offer(tree);
					} else {
						m_factory.reuse(tree.getMessageId());
					}
				} else {
					m_factory.reuse(tree.getMessageId());
				}
				return true;
			} else {
				return m_queue.offer(tree);
			}
		} else {
			m_factory.reuse(tree.getMessageId());
			return true;
		}
	}

	public MessageTree peek() {
		return m_queue.peek();
	}

	public MessageTree poll() {
		return m_queue.poll();
	}

}
