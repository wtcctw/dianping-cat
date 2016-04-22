package com.dianping.cat.hadoop.hdfs;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class FileSystemManagerTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		FileSystemManager manager = lookup(FileSystemManager.class);
		StringBuilder baseDir = new StringBuilder();
		FileSystem fileSystem = manager.getFileSystem("dump", baseDir);
		RemoteIterator<LocatedFileStatus> files = fileSystem.listFiles(new Path("/user/cat/dump"), true);

		while (files.hasNext()) {
			System.out.println(files.next().getPath());
		}
		Assert.assertNotNull(fileSystem);

		Assert.assertEquals("dump/", baseDir.toString());
	}
}
