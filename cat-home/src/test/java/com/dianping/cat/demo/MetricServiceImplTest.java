package com.dianping.cat.demo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.config.entity.InfluxdbConfig;
import com.dianping.cat.influxdb.config.transform.DefaultSaxParser;
import com.dianping.cat.influxdb.service.InfluxDBConnection;
import com.dianping.cat.influxdb.service.MetricServiceImpl;
import com.dianping.cat.metric.DataSourceService;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.metric.MetricType;
import com.dianping.cat.metric.QueryParameter;

public class MetricServiceImplTest extends ComponentTestCase {

	private MetricService m_metricService;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHH");

	@Override
	public void setUp() throws Exception {
		super.setUp();
		m_metricService = lookup(MetricService.class, InfluxDB.ID);
	}

	@Test
	public void test() throws Exception {
		Date start = m_sdf.parse("2015111107");
		Date end = m_sdf.parse("2015111207");
		QueryParameter parameter = new QueryParameter();

		parameter.setCategory("cpu").setStart(start).setEnd(end).setMeasurement("userCpu").setType(MetricType.AVG)
		      .setInterval("1m");

		Map<Date, Double> result = m_metricService.query(parameter);

		System.out.println(result);
	}

}
