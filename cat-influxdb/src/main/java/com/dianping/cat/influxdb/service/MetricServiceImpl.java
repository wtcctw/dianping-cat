package com.dianping.cat.influxdb.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

import com.dianping.cat.Cat;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.InfluxDB.ConsistencyLevel;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.influxdb.dto.QueryResult.Result;
import com.dianping.cat.influxdb.dto.QueryResult.Series;
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

	private Map<Long, Double> parseData(QueryResult result) {
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

	private List<String> parseMeasurements(QueryResult result) {
		List<String> measurements = new LinkedList<String>();

		for (Result r : result.getResults()) {
			List<Series> series = r.getSeries();

			if (series != null) {
				for (Series s : series) {
					if (s != null && s.getValues() != null) {
						for (List<Object> v : s.getValues()) {
							measurements.add(String.valueOf(v.get(0)));
						}
					}
				}
			}
		}
		return measurements;
	}

	@Override
	public Map<Long, Double> query(QueryParameter parameter) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(parameter.getCategory());

		if (conn != null) {
			String format = "SELECT %1$s(value) FROM %2$s WHERE %3$s TIME >= '%4$tF %4$tT' AND TIME < '%5$tF %5$tT' GROUP BY time(%6$s) fill(none)";
			String query = String.format(format, parameter.getType().getName(), parameter.getMeasurement(),
			      parameter.getTags(), parameter.getStart(), parameter.getEnd(), parameter.getInterval());
			System.out.println(query);

			QueryResult queryResult = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
			Map<Long, Double> datas = parseData(queryResult);

			return datas;
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	public List<String> queryMeasurements(String category) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(category);
		QueryResult result = conn.getInfluxDB().query(new Query("show measurements", conn.getDataBase()));

		return parseMeasurements(result);
	}
}
