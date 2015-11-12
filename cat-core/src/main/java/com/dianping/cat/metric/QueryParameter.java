package com.dianping.cat.metric;

import java.util.Date;

import com.dianping.cat.metric.MetricType;

public class QueryParameter {

	private String category;

	private String measurement;

	private MetricType type;

	private String interval;

	private Date start;

	private Date end;

	public String getCategory() {
		return category;
	}

	public Date getEnd() {
		return end;
	}

	public String getInterval() {
		return interval;
	}

	public String getMeasurement() {
		return measurement;
	}

	public Date getStart() {
		return start;
	}

	public MetricType getType() {
		return type;
	}

	public QueryParameter setCategory(String category) {
		this.category = category;
		return this;
	}

	public QueryParameter setEnd(Date end) {
		this.end = end;
		return this;
	}

	public QueryParameter setInterval(String interval) {
		this.interval = interval;
		return this;
	}

	public QueryParameter setMeasurement(String measurement) {
		this.measurement = measurement;
		return this;
	}

	public QueryParameter setStart(Date start) {
		this.start = start;
		return this;
	}

	public QueryParameter setType(MetricType type) {
		this.type = type;
		return this;
	}
}
