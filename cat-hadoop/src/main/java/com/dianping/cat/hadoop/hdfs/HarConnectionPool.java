package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.HarFileSystem;

import com.dianping.cat.config.server.ServerConfigManager;

public class HarConnectionPool {

	private ServerConfigManager m_serverConfigManager;

	private MessageFormat m_format = new MessageFormat("{0}/{1}/{2,date,yyyyMMdd}.har");

	private Map<Date, HarFileSystem> m_hars = new LinkedHashMap<Date, HarFileSystem>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<Date, HarFileSystem> eldest) {
			return size() > m_serverConfigManager.getHdfsMaxStorageTime();
		}

	};

	public HarConnectionPool(ServerConfigManager manager) {
		m_serverConfigManager = manager;
	}

	public HarFileSystem getHarfsConnection(String id, Date date, FileSystem fs) throws IOException {
		String serverUri = m_serverConfigManager.getHarfsServerUri(id);
		String baseUri = m_serverConfigManager.getHarfsBaseDir(id);
		String harUri = m_format.format(new Object[] { serverUri, baseUri, date });
		HarFileSystem har = m_hars.get(date);

		if (har == null) {
			synchronized (this) {
				if (har == null) {
					URI uri = URI.create(harUri);
					har = new HarFileSystem(fs);

					har.initialize(uri, har.getConf());
					m_hars.put(date, har);
				}
			}
		}
		return har;
	}
}
