package org.unidal.cat.message.storage.hdfs;

import java.util.List;

import com.dianping.cat.message.internal.MessageId;

public interface MessageConsumerFinder {

	public List<String> findConsumerIps(MessageId id);

}
