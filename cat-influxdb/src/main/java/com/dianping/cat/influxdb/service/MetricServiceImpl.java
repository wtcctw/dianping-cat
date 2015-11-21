package com.dianping.cat.influxdb.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.InfluxDB.ConsistencyLevel;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.influxdb.dto.QueryResult.Result;
import com.dianping.cat.influxdb.dto.QueryResult.Series;
import com.dianping.cat.influxdb.service.InfluxDBConnection;
import com.dianping.cat.metric.DataSourceService;
import com.dianping.cat.metric.MetricEntity;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.metric.QueryParameter;

public class MetricServiceImpl implements MetricService, Initializable {

	@Inject(InfluxDB.ID)
	private DataSourceService<InfluxDBConnection> m_dataSourceService;

	private SimpleDateFormat m_sdf;

	private Map<String, List<MetricEntity>> buildCategories(List<MetricEntity> entities) {
		Map<String, List<MetricEntity>> categories = new LinkedHashMap<String, List<MetricEntity>>();
		for (MetricEntity entity : entities) {
			String category = entity.getCategory();
			List<MetricEntity> list = categories.get(category);

			if (list == null) {
				list = new LinkedList<MetricEntity>();

				categories.put(category, list);
			}
			list.add(entity);
		}
		return categories;
	}

	@Override
	public void initialize() throws InitializationException {
		m_sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		m_sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	@Override
	public boolean insert(List<MetricEntity> entities) {
		Map<String, List<MetricEntity>> categories = buildCategories(entities);

		for (Entry<String, List<MetricEntity>> entry : categories.entrySet()) {
			try {
				InfluxDBConnection conn = m_dataSourceService.getConnection(entry.getKey());
				BatchPoints batchPoints = BatchPoints.database(conn.getDataBase()).retentionPolicy("default")
				      .consistency(ConsistencyLevel.ALL).build();

				for (MetricEntity entity : entry.getValue()) {
					Point point = Point.measurement(entity.getMeasure()).tag(entity.getTags())
					      .time(entity.getTimestamp(), TimeUnit.MILLISECONDS).fields(entity.getFields()).build();
					batchPoints.point(point);
				}

				conn.getInfluxDB().write(batchPoints);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return true;
	}

	private List<String> parseData(QueryResult result, int index) {
		List<String> results = new ArrayList<String>();

		for (Result r : result.getResults()) {
			List<Series> series = r.getSeries();

			if (series != null) {
				for (Series s : series) {
					if (s != null && s.getValues() != null) {
						for (List<Object> v : s.getValues()) {
							results.add(String.valueOf(v.get(index)));
						}
					}
				}
			}
		}
		return results;
	}

	private Map<Long, Double> parseValue(QueryResult result) {
		Map<Long, Double> datas = new LinkedHashMap<Long, Double>();

		for (Result r : result.getResults()) {
			List<Series> series = r.getSeries();

			if (series != null) {
				for (Series s : series) {
					if (s != null && s.getValues() != null) {
						for (List<Object> v : s.getValues()) {
							try {
								Date date = m_sdf.parse(String.valueOf(v.get(0)));
								double data = (double) (v.get(1));

								datas.put(date.getTime(), data);
							} catch (ParseException e) {
								Cat.logError(e);
							}
						}
					}
				}
			}
		}
		return datas;
	}

	@Override
	public Map<Long, Double> query(QueryParameter parameter) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(parameter.getCategory());

		if (conn != null) {
			String format = "SELECT %s(value) FROM \"%s\" WHERE %s TIME >= '%s' AND TIME < '%s' GROUP BY time(%s) fill(0)";
			String query = String.format(format, parameter.getType().getName(), parameter.getMeasurement(),
			      parameter.getTags(), m_sdf.format(parameter.getStart()), m_sdf.format(parameter.getEnd()),
			      parameter.getInterval());

			QueryResult queryResult = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			Map<Long, Double> datas = parseValue(queryResult);

			return datas;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	public List<String> queryMeasurements(String category) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			QueryResult result = conn.getInfluxDB().query(new Query("SHOW MEASUREMENTS", conn.getDataBase()));
			List<String> results = parseData(result, 0);

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryTagValues(String category, String measurement, String tag) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			String format = "SHOW TAG VALUES FROM \"%s\" WITH KEY='%s'";
			String query = String.format(format, measurement, tag);
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = parseData(result, 0);

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryEndPoints(String category, List<String> keywords) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			String format = "SHOW TAG VALUES FROM  /.*/  WITH KEY = \"endPoint\"  WHERE endPoint =~ /.*%s.*/";
			String query = String.format(format, StringUtils.join(keywords, "|"));
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = parseData(result, 0);

			return results;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<String> queryMeasurements(String category, List<String> endPoints) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);

		if (conn != null) {
			List<String> list = new ArrayList<String>();

			for (String endPoint : endPoints) {
				String query = "endPoint='" + endPoint + "'";

				list.add(query);
			}

			String format = "SHOW MEASUREMENTS WHERE %s";
			String query = String.format(format, StringUtils.join(list, " OR "));
			QueryResult result = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			List<String> results = parseData(result, 0);

			return results;
		} else {
			return Collections.emptyList();
		}
	}
}
