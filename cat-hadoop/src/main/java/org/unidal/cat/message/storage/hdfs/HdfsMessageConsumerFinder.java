package org.unidal.cat.message.storage.hdfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.unidal.cat.message.storage.FileType;
import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.message.internal.MessageId;

@Named(type = MessageConsumerFinder.class, value = "hdfs")
public class HdfsMessageConsumerFinder implements MessageConsumerFinder {

	@Inject("hdfs")
	private PathBuilder m_pathBuilder;

	@Inject
	private FileSystemManager m_fileSystemManager;

	@Override
	public List<String> findConsumerIps(MessageId id) {
		final String domain = id.getDomain();
		Date start = new Date(id.getTimestamp());
		String parent = m_pathBuilder.getPath(domain, start, id.getIpAddress(), FileType.PARENT);
		FileSystem fs;

		try {
			fs = m_fileSystemManager.getFileSystem();
		} catch (IOException e) {
			Cat.logError(e);
			return null;
		}

		final List<String> ips = new ArrayList<String>();

		try {
			final Path basePath = new Path(parent);

			if (fs != null) {
				fs.listStatus(basePath, new PathFilter() {
					@Override
					public boolean accept(Path p) {
						String name = p.getName();

						if (name.contains(domain) && name.endsWith(".dat")) {
							int start = name.lastIndexOf('-');
							int end = name.length() - 4;

							ips.add(name.substring(start + 1, end));
						}
						return false;
					}
				});
			}
		} catch (IOException e) {
			Cat.logError(e);
		}
		return ips;
	}

}
