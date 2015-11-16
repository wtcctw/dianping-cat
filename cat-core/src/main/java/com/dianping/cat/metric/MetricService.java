package com.dianping.cat.metric;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MetricService {

	public <T> Map<Date, T> query(QueryParameter queryParameter);

	public boolean insert(List<MetricEntity> entities);

	public List<String> queryMeasurements(String category);

}
