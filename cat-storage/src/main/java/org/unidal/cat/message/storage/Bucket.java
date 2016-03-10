package org.unidal.cat.message.storage;

import java.io.IOException;

import com.dianping.cat.message.internal.MessageId;

public interface Bucket {
	
	public void close();

	public Block get(MessageId id) throws IOException;

	public void put(Block block) throws IOException;
}
