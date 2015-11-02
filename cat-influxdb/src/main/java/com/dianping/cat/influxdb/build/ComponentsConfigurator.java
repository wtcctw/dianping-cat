package com.dianping.cat.influxdb.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.influxdb.InfluxDBManager;
import com.dianping.cat.influxdb.config.InfluxDBConfigManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(InfluxDBConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(InfluxDBManager.class).req(InfluxDBConfigManager.class));
		return all;
	}
}
