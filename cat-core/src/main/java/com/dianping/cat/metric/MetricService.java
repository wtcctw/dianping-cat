package com.dianping.cat.metric;

import java.util.List;
import java.util.Map;

public interface MetricService {

	public boolean insert(List<MetricEntity> entities);

	public Map<Long, Double> query(QueryParameter queryParameter);

	public List<String> queryMeasurements(String category);

}
