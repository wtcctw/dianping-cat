package com.dianping.cat.influxdb.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	public <T> Map<Date, T> query(QueryParameter parameter) {
		InfluxDBConnection conn = m_dataSourceService.getConnection(parameter.getCategory());
		String format = "select %1$s(value) from %2$s where time >= '%3$tF %3$tT' and time < '%4$tF %4$tT' group by time(%5$s) fill(none)";
		String query = String.format(format, parameter.getType().getName(), parameter.getMeasurement(),
		      parameter.getStart(), parameter.getEnd(), parameter.getInterval());

		QueryResult queryResult = conn.getInfluxDB().query(new Query(query, conn.getDataBase()));
		Map<Date, T> datas = parseData(queryResult);

		return datas;
	}

	@SuppressWarnings("unchecked")
	private <T> Map<Date, T> parseData(QueryResult result) {
		Map<Date, T> datas = new LinkedHashMap<Date, T>();

		for (Result r : result.getResults()) {
			List<Series> series = r.getSeries();

			if (series != null) {
				for (Series s : series) {
					if (s != null && s.getValues() != null) {
						for (List<Object> v : s.getValues()) {
							try {
								Date date = m_sdf.parse(String.valueOf(v.get(0)));
								T data = (T) (v.get(1));

								datas.put(date, data);
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
	public void initialize() throws InitializationException {
		m_sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		m_sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

}
