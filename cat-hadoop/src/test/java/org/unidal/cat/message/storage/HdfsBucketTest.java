package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.IOException;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class HdfsBucketTest extends ComponentTestCase {

	@Test
	public void test() {
		Bucket hdfsbucket = lookup(Bucket.class, "hdfs");
		MessageCodec m_plainText = lookup(MessageCodec.class, PlainTextMessageCodec.ID);

		try {
			MessageId id = MessageId.parse("cat-ac187982-405559-25145");
			hdfsbucket.initialize(id.getDomain(), id.getIpAddress(), id.getHour());

			ByteBuf byteBuf = hdfsbucket.get(id);

			if (byteBuf != null) {
				MessageTree tree = m_plainText.decode(byteBuf);

				System.out.println(tree.toString());

				m_plainText.reset();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
