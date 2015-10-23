package com.dianping.cat.influxdb;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.influxdb.InfluxDB.ConsistencyLevel;
import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.influxdb.config.entity.InfluxdbConfig;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.influxdb.entity.InsertEntity;

public class InfluxDBManager implements Initializable {

	@Inject
	private InfluxDBConfigManager m_configManager;

	private InfluxdbConfig m_influxdbConfig;

	private InfluxDB m_influxDB;

	public InfluxDB getInfluxDB() {
		return m_influxDB;
	}

	@Override
	public void initialize() throws InitializationException {
		m_influxdbConfig = m_configManager.getConfig();
		m_influxDB = InfluxDBFactory.connect("http://" + m_influxdbConfig.getHost() + ":" + m_influxdbConfig.getPort(),
		      m_influxdbConfig.getUsername(), m_influxdbConfig.getPassword());
		m_influxDB.createDatabase(m_influxdbConfig.getDatabase());
	}

	public void setInfluxDB(InfluxDB influxDB) {
		m_influxDB = influxDB;
	}

	public void insert(List<InsertEntity> entities) {
		BatchPoints batchPoints = BatchPoints.database(m_influxdbConfig.getDatabase()).retentionPolicy("default")
		      .consistency(ConsistencyLevel.ALL).build();

		for (InsertEntity entity : entities) {
			try {
				Point point = Point.measurement(entity.getMeasure()).tag(entity.getTags())
				      .time(entity.getTimestamp(), TimeUnit.MILLISECONDS).fields(entity.getFields()).build();
				batchPoints.point(point);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		m_influxDB.write(batchPoints);
	}

	public QueryResult query(String query) {
		return m_influxDB.query(new Query(query, m_influxdbConfig.getDatabase()));
	}
}
