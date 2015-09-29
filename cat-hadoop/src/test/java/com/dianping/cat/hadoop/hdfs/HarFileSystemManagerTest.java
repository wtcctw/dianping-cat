package com.dianping.cat.hadoop.hdfs;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.hadoop.hdfs.bucket.HdfsMessageBucketManager;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;

@RunWith(JUnit4.class)
public class HarFileSystemManagerTest extends ComponentTestCase {

	@Test
	public void test() {
		try {
			ServerConfigManager serverConfigManager = lookup(ServerConfigManager.class);
			serverConfigManager.initialize(new File("/data/appdatas/cat/server.xml"));

			MessageBucketManager hdfsMsgBucketManager = lookup(MessageBucketManager.class, HdfsMessageBucketManager.ID);
			MessageTree msg = hdfsMsgBucketManager.loadMessage("cat-c0a8d573-400912-7437");

			System.out.println(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
