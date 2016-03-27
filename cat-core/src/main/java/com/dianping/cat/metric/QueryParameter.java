package com.dianping.cat.metric;

import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

public class QueryParameter {

	private String m_category;

	private String m_measurement;

	private String m_tags;

	private MetricType m_type;

	private String m_interval;

	private Date m_start;

	private Date m_end;

	public String getCategory() {
		return m_category;
	}

	public Date getEnd() {
		return m_end;
	}

	public String getInterval() {
		return m_interval;
	}

	public String getMeasurement() {
		return m_measurement;
	}

	public Date getStart() {
		return m_start;
	}

	public String getSqlTags() {
		String tag = "";
		List<String> tags = Splitters.by(";").noEmptyItem().split(m_tags);

		if (!tags.isEmpty()) {
			tag = StringUtils.join(tags, " AND ") + " AND ";
		}

		return tag;
	}

	public String getTags() {
		return m_tags;
	}

	public MetricType getType() {
		return m_type;
	}

	public QueryParameter setCategory(String category) {
		m_category = category;
		return this;
	}

	public QueryParameter setEnd(Date end) {
		m_end = end;
		return this;
	}

	public QueryParameter setInterval(String interval) {
		m_interval = interval;
		return this;
	}

	public QueryParameter setMeasurement(String measurement) {
		m_measurement = measurement;
		return this;
	}

	public QueryParameter setStart(Date start) {
		m_start = start;
		return this;
	}

	public QueryParameter setTags(String tags) {
		m_tags = tags;
		return this;
	}

	public QueryParameter setType(MetricType type) {
		m_type = type;
		return this;
	}
}
