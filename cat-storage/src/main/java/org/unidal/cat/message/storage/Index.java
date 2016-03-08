package org.unidal.cat.message.storage;

import java.io.IOException;

import com.dianping.cat.message.internal.MessageId;

public interface Index {
	public void close();

	public void map(MessageId from, MessageId to) throws IOException;

	public MessageId lookup(MessageId from) throws IOException;
}
