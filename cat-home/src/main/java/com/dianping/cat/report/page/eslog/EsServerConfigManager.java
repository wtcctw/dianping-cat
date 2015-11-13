package com.dianping.cat.report.page.eslog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.eslog.entity.EsServerConfig;
import com.dianping.cat.home.eslog.entity.Log;
import com.dianping.cat.home.eslog.transform.DefaultSaxParser;

public class EsServerConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private volatile EsServerConfig m_esServerConfig;

	protected Logger m_logger;

	private static final String CONFIG_NAME = "esServerConfig";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public EsServerConfig getConfig() {
		return m_esServerConfig;
	}

	public String getLogServer(String logId) {
		Log log = m_esServerConfig.findLog(logId);

		if (log != null) {
			StringBuilder sb = new StringBuilder();

			sb.append("http://").append(log.getHost()).append(":").append(log.getPort()).append("/");
			sb.append(log.getIndex()).append("/").append(log.getType()).append("/").append("_search");
			return sb.toString();
		} else {
			return null;
		}
	}

	public List<String> getLogTypes() {
		return new ArrayList<String>(m_esServerConfig.getLogs().keySet());
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_esServerConfig = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();
				Date now = new Date();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				config.setModifyDate(now);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_esServerConfig = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_esServerConfig == null) {
			m_esServerConfig = new EsServerConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			EsServerConfig esServerConfig = DefaultSaxParser.parse(xml);

			m_esServerConfig = esServerConfig;
			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_esServerConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}
}