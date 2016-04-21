package org.unidal.cat.message.storage.hdfs;

import java.text.MessageFormat;
import java.util.Date;

import org.unidal.cat.message.storage.PathBuilder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.config.server.ServerConfigManager;

@Named(type = PathBuilder.class, value = "hdfs")
public class HdfsFileBuilder implements PathBuilder {

	@Inject
	private ServerConfigManager m_manager;

	@Override
	public String getPath(String domain, Date startTime, String ip, FileType type) {
		MessageFormat format;
		String path;

		switch (type) {
		case TOKEN:
			format = new MessageFormat("dump/{0,date,yyyyMMdd}/{0,date,HH}/{2}.{3}");
			path = format.format(new Object[] { startTime, null, ip, type.getExtension() });
			break;
		default:
			format = new MessageFormat("dump/{0,date,yyyyMMdd}/{0,date,HH}/{1}-{2}.{3}");
			path = format.format(new Object[] { startTime, domain, ip, type.getExtension() });
			break;
		}

		return m_manager.getHdfsBaseDir(ServerConfigManager.DUMP_DIR) + path;
	}
}
