package org.unidal.cat.message.storage;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface BucketManager {
	public void closeBuckets(int hour);

	public Bucket getBucket(String domain, String ip, int hour, boolean createIfNotExists) throws IOException;

	public MessageTree loadMessage(String messageId);
}
