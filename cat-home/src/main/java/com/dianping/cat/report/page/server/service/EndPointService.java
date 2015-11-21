package com.dianping.cat.report.page.server.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.metric.MetricService;

public class EndPointService {

	@Inject
	private MetricService m_metricService;

	@Inject
	private InfluxDBConfigManager m_configManager;

	private Set<String> m_endPoints = new HashSet<String>();

	public final static String TAG_KEY = "endPoint";

	public void refresh() {
		Set<String> endPoints = new HashSet<String>();
		Set<String> categories = m_configManager.getConfig().getInfluxdbs().keySet();

		for (String category : categories) {
			List<String> measurements = m_metricService.queryMeasurements(category);

			for (String measurement : measurements) {
				List<String> results = m_metricService.queryTagValues(category, measurement, TAG_KEY);

				endPoints.addAll(results);
			}
		}
		m_endPoints = endPoints;
	}

	public Set<String> getEndPoints() {
		return m_endPoints;
	}

	public Set<String> queryEndPoints(List<String> keywords) {
		Set<String> endPoints = new HashSet<String>();

		for (String key : m_configManager.getConfig().getInfluxdbs().keySet()) {
			endPoints.addAll(m_metricService.queryEndPoints(key, keywords));
		}

		return endPoints;
	}

	public Set<String> queryMeasurements(List<String> endPoints) {
		Set<String> measurements = new HashSet<String>();

		for (String key : m_configManager.getConfig().getInfluxdbs().keySet()) {
			measurements.addAll(m_metricService.queryMeasurements(key, endPoints));
		}

		return measurements;
	}

}
