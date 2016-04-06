package com.dianping.cat.config.app;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.app.broker.entity.BrokerConfig;
import com.dianping.cat.configuration.app.broker.entity.Type;
import com.dianping.cat.configuration.app.broker.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.task.ConfigSyncTask;
import com.dianping.cat.task.ConfigSyncTask.SyncHandler;

@Named
public class BrokerConfigManager implements Initializable {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	private int m_configId;

	private long m_modifyTime;

	private volatile BrokerConfig m_config;

	private static final String CONFIG_NAME = "broker-config";

	public BrokerConfig getConfig() {
		return m_config;
	}

	public String getLocalFlushPath() {
		return m_config.getCheckpoint().getLocalFlush().getPath();
	}

	public String getLogPath(String logType) {
		Type type = m_config.findType(logType);

		if (type != null) {
			return type.getPath();
		} else {
			return null;
		}
	}

	public int getLogReservceDuration(String logType) {
		Type type = m_config.findType(logType);

		if (type != null) {
			return type.getDurationInHours();
		} else {
			return 3;
		}
	}

	public String getTooLongCommand() {
		return m_config.getTooLongCommand().getId();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
			m_config = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);
				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new BrokerConfig();
		}

		ConfigSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return CONFIG_NAME;
			}

			@Override
			public void handle() throws Exception {
				refreshConfig();
			}
		});
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	public boolean isDevMode() {
		return m_config.isDevMode();
	}

	public boolean isGroupStatisticEnabled() {
		return m_config.isGroupStatisticsEnabled();
	}

	public boolean isLocalFlushEnabled() {
		return m_config.getCheckpoint().isLocalFlushEnabled();
	}

	public boolean isRemteIpServiceEnabled() {
		return m_config.isRemoteIpServiceEnabled();
	}

	private void refreshConfig() throws Exception {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				BrokerConfig brokerConfig = DefaultSaxParser.parse(content);

				m_config = brokerConfig;
				m_modifyTime = modifyTime;
			}
		}
	}

	public boolean shouldLog(String logType) {
		Type type = m_config.findType(logType);

		if (type != null) {
			return type.isEnabled();
		} else {
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
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
