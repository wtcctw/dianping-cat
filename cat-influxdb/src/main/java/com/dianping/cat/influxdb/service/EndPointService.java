package com.dianping.cat.influxdb.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.influxdb.dto.QueryResult.Result;
import com.dianping.cat.influxdb.dto.QueryResult.Series;

public class EndPointService implements Initializable {

	@Inject
	private DataSourceServiceImpl m_dataSourceService;

	private Set<String> m_endPoints = new HashSet<String>();

	@Override
	public void initialize() throws InitializationException {

		List<String> measurements = parseData("show measurements", 0);

		for (String measurement : measurements) {
			List<String> endPoints = parseData("show tag values from " + measurement + " with key='endPoint'", 0);

			m_endPoints.addAll(endPoints);
			System.out.println(endPoints);
		}

		System.out.println(m_endPoints);

	}

	@SuppressWarnings("unchecked")
	private <T> List<T> parseData(String query, int index) {
		List<T> ret = new ArrayList<T>();
		QueryResult result = m_dataSourceService.getInfluxDBs().get("").getInfluxDB().query(null);

		List<Result> results = result.getResults();

		for (Result r : results) {
			List<Series> series = r.getSeries();
			if (series != null) {
				for (Series s : series) {
					for (List<Object> v : s.getValues()) {
						ret.add((T) (v.get(index)));
					}
				}
			}
		}
		return ret;
	}

}
