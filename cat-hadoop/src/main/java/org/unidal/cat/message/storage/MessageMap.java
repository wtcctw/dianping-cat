package org.unidal.cat.message.storage;

import com.dianping.cat.message.internal.MessageId;

public class MessageMap {

	private MessageId m_from;

	private MessageId m_to;

	public MessageMap(MessageId from, MessageId to) {
		m_from = from;
		m_to = to;
	}

	public MessageId getFrom() {
		return m_from;
	}

	public MessageId getTo() {
		return m_to;
	}

}
