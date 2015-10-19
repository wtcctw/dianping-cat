package com.dianping.cat.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.InfluxDB.ConsistencyLevel;
import com.dianping.cat.influxdb.InfluxDBFactory;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.report.page.database.DatabaseGroup;
import com.dianping.cat.report.page.system.graph.SystemGraphCreator;

public class InfluxdbDemo {

	private List<String> m_databaseHosts = Arrays.asList("db-mysql-10.10.10.1-3306", "db-mysql-10.10.10.2-3306",
	      "db-mysql-10.10.10.3-3306", "db-mysql-10.10.10.4-3306");

	private List<String> m_paasHosts = Arrays.asList("10.1.1.1", "10.1.1.2", "10.1.1.3", "10.1.14");

	@Test
	public void mockData() {
		InfluxDB influxDB = InfluxDBFactory.connect("http://10.128.53.56:8086", "root", "123456");
		String dbName = "cat";
		influxDB.createDatabase(dbName);

		BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy("default")
		      .consistency(ConsistencyLevel.ALL).build();

		Point point1 = Point.measurement("cpu").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
		      .field("idle", 90L).field("system", 9L).field("system", 1L).build();
		Point point2 = Point.measurement("disk").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
		      .field("used", 80L).field("free", 1L).build();

		batchPoints.point(point1);
		batchPoints.point(point2);
		influxDB.write(batchPoints);
		QueryResult result = influxDB.query(new Query("SELECT idle FROM cpu", dbName));
		QueryResult result2 = influxDB.query(new Query("SELECT used FROM disk", dbName));

		System.out.println(result);
		System.out.println(result2);
		// influxDB.deleteDatabase(dbName);
	}

	public List<Point> buildNetworkPoints() {
		List<String> NETWORK_HOSTS = Arrays.asList("f5-3600-1", "f5-3600-2", "switch-3600-1", "switch-3600-2");
		List<String> NETWORK_PORTS = Arrays.asList("1", "2", "3");
		List<String> NETWORK_DIRECT = Arrays.asList("in", "out");
		List<String> NETWORK_TYPE = Arrays.asList("server", "client");
		List<Point> points = new ArrayList<Point>();

		for (String host : NETWORK_HOSTS) {
			points.add(Point.measurement("flow").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).tag("host", host)
			      .field("idle", 90L).field("system", 9L).field("system", 1L).build());
			points.add(Point.measurement("disk").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
			      .field("used", 80L).field("free", 1L).build());
		}
		return null;
	}

}
