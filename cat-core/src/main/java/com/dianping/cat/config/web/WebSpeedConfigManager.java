package com.dianping.cat.config.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.configuration.web.speed.entity.Step;
import com.dianping.cat.configuration.web.speed.entity.WebSpeedConfig;
import com.dianping.cat.configuration.web.speed.transform.DefaultSaxParser;
import com.dianping.cat.configuration.web.speed.entity.Speed;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.helper.JsonBuilder;

public class WebSpeedConfigManager implements Initializable {

	private static final String CONFIG_NAME = "web-speed-config";

	private volatile WebSpeedConfig m_config;

	@Inject
	protected ConfigDao m_configDao;

	private int m_configId;

	@Inject
	private ContentFetcher m_fetcher;

	private long m_modifyTime;

	private volatile Map<String, Speed> m_speeds = new ConcurrentHashMap<String, Speed>();

	public String getPage2StepsJson() {
		return new JsonBuilder().toJson(m_config.getSpeeds());
	}

	public boolean deleteSpeed(int id) {
		m_config.removeSpeed(id);
		return storeConfig();
	}

	public boolean deleteStep(int pageId, int stepId) {
		Speed speed = m_config.getSpeeds().get(pageId);

		if (speed != null) {
			speed.removeStep(stepId);
		}

		if (speed.getSteps().size() == 0) {
			m_config.removeSpeed(pageId);
		}
		return storeConfig();
	}

	private int generateId(List<Integer> ids) {
		int max = 0;

		if (!ids.isEmpty()) {
			Collections.sort(ids);
			max = ids.get(ids.size() - 1);
		}

		if (ids.size() < max) {
			for (int i = 1; i <= max; i++) {
				if (!ids.contains(i)) {
					return i;
				}
			}
		}
		return max + 1;
	}

	public int generateSpeedId() {
		List<Integer> ids = new ArrayList<Integer>();

		for (Speed s : m_config.getSpeeds().values()) {
			ids.add(s.getId());
		}

		return generateId(ids);
	}

	public int generateStepId(String page) {
		List<Integer> ids = new ArrayList<Integer>();
		Speed speed = m_speeds.get(page);

		if (speed != null) {
			for (Step step : speed.getSteps().values()) {
				ids.add(step.getId());
			}
		}
		return generateId(ids);
	}

	public Map<String, Speed> getSpeeds() {
		return m_speeds;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
			m_modifyTime = config.getModifyDate().getTime();
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
			m_config = new WebSpeedConfig();
		}

		updateData();
		Threads.forGroup("cat").start(new ConfigReloadTask());
	}

	public Speed querySpeed(String page) {
		return m_speeds.get(page);
	}

	public int querySpeedId(String page) {
		int value = -1;
		Speed speed = m_speeds.get(page);

		if (speed != null) {
			value = speed.getId();
		}
		return value;
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();

			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_config.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);

			updateData();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public void updateConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				WebSpeedConfig appConfig = DefaultSaxParser.parse(content);

				m_config = appConfig;
				m_modifyTime = modifyTime;
				updateData();
			}
		}
	}

	public boolean updateConfig(Speed speed) {
		m_config.addSpeed(speed);
		return storeConfig();
	}

	private void updateData() {
		Map<Integer, Speed> speeds = m_config.getSpeeds();
		Map<String, Speed> tmp = new ConcurrentHashMap<String, Speed>();

		for (Entry<Integer, Speed> entry : speeds.entrySet()) {
			Speed s = entry.getValue();

			tmp.put(s.getPage(), s);
		}
		m_speeds = tmp;
	}

	class ConfigReloadTask implements Task {

		@Override
		public String getName() {
			return "Web-Speed-Config-Reload";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					updateConfig();
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}
}
