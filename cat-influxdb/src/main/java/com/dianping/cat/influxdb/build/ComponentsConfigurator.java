package com.dianping.cat.influxdb.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.build.CatDatabaseConfigurator;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.influxdb.service.DataSourceServiceImpl;
import com.dianping.cat.influxdb.service.MetricServiceImpl;
import com.dianping.cat.metric.DataSourceService;
import com.dianping.cat.metric.MetricService;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		all.add(C(InfluxDBConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(MetricService.class, InfluxDB.ID, MetricServiceImpl.class).req(DataSourceService.class, InfluxDB.ID));
		all.add(C(DataSourceServiceImpl.class).req(InfluxDBConfigManager.class));
		all.add(C(DataSourceService.class, InfluxDB.ID, DataSourceServiceImpl.class).req(InfluxDBConfigManager.class));

		return all;
	}
}
