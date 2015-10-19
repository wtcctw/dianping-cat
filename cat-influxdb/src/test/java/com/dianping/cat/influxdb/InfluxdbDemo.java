package com.dianping.cat.influxdb;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.dianping.cat.influxdb.InfluxDB.ConsistencyLevel;
import com.dianping.cat.influxdb.dto.BatchPoints;
import com.dianping.cat.influxdb.dto.Point;
import com.dianping.cat.influxdb.dto.Query;
import com.dianping.cat.influxdb.dto.QueryResult;

public class InfluxdbDemo {

	@Test
	public void test() {
		InfluxDB influxDB = InfluxDBFactory.connect("http://10.128.53.56:8086", "root", "123456");
		String dbName = "aTimeSeries";
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
}
