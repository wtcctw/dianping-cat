package org.unidal.cat.message.storage.local;

import java.nio.ByteBuffer;

public interface ByteBufCache {

	public ByteBuffer get();

	public void put(ByteBuffer buf);

}
