package com.dianping.cat.influxdb.service;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.InfluxDBFactory;
import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.influxdb.config.entity.Influxdb;
import com.dianping.cat.influxdb.config.entity.InfluxdbConfig;
import com.dianping.cat.metric.DataSourceService;

public class DataSourceServiceImpl implements DataSourceService<InfluxDBConnection> {

	@Inject
	private InfluxDBConfigManager m_configManager;

	private InfluxdbConfig m_influxdbConfig;

	private Map<String, InfluxDBConnection> m_influxDBs = new ConcurrentHashMap<String, InfluxDBConnection>();

	@Override
	public InfluxDBConnection getConnection(String category) {
		return m_influxDBs.get(category);
	}

	public Map<String, InfluxDBConnection> getInfluxDBs() {
		return m_influxDBs;
	}

	@Override
	public void initialize() throws InitializationException {
		m_influxdbConfig = m_configManager.getConfig();

		initialize(m_influxdbConfig);
	}

	public void initialize(InfluxdbConfig influxdbConfig) {
		for (Entry<String, Influxdb> entry : influxdbConfig.getInfluxdbs().entrySet()) {
			try {
				Influxdb config = entry.getValue();
				String url = String.format("http://%s:%s", config.getHost(), config.getPort());
				InfluxDB influxDB = InfluxDBFactory.connect(url, config.getUsername(), config.getPassword());
				final String database = config.getDatabase();

				influxDB.createDatabase(database);
				m_influxDBs.put(entry.getKey(), new InfluxDBConnection(influxDB, database));
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}
}
