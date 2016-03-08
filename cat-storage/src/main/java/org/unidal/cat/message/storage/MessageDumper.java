package org.unidal.cat.message.storage;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public interface MessageDumper {
	public void awaitTermination() throws InterruptedException;

	public void process(MessageTree tree);

	public MessageTree find(MessageId id);
}
