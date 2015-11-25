package com.dianping.cat.report.page.server.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.influxdb.config.InfluxDBConfigManager;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.report.page.server.display.MetricConstants;

public class EndPointService {

	@Inject
	private MetricService m_metricService;

	@Inject
	private InfluxDBConfigManager m_configManager;

	private String cleanRedundancyTag(String measure) {
		return measure.replaceAll("(domain=[^,]*(,|$))|(endPoint=[^,]*(,|$))", "").replaceAll(",$", "")
		      .replaceAll(",", ";");
	}

	public Set<String> queryEndPoints(String search, List<String> keywords) {
		Set<String> endPoints = new HashSet<String>();
		Set<String> keySet = m_configManager.getConfig().getInfluxdbs().keySet();

		if (MetricConstants.END_POINT.equals(search)) {
			for (String key : keySet) {
				endPoints.addAll(m_metricService.queryEndPoints(key, search, keywords));
			}
		} else {
			for (String key : keySet) {
				endPoints.addAll(m_metricService.queryEndPointsByTag(key, keywords));
			}
		}

		return endPoints;
	}

	public Set<String> queryMeasurements(List<String> endPoints) {
		Set<String> measurements = new HashSet<String>();

		for (String key : m_configManager.getConfig().getInfluxdbs().keySet()) {
			List<String> measures = m_metricService.queryMeasurements(key, endPoints);
			List<String> results = new ArrayList<String>();

			for (String measure : measures) {
				results.add(cleanRedundancyTag(measure));
			}

			measurements.addAll(results);
		}

		return measurements;
	}
}
