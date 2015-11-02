package com.dianping.cat.influxdb.entity;

import java.util.HashMap;
import java.util.Map;

public class InsertEntity {

	public static final String END_POINT = "endPoint";

	private String m_measure;

	private String m_endPoint;

	private long m_timestamp;

	private Map<String, String> m_tags = new HashMap<String, String>();

	private Map<String, Object> m_fields = new HashMap<String, Object>();

	public InsertEntity(String measure, String endPoint, long timestamp) {
		m_measure = measure;
		m_endPoint = endPoint;
		m_timestamp = timestamp;

		m_tags.put(END_POINT, endPoint);
	}

	public void addField(String field, Object value) {
		m_fields.put(field, value);
	}

	public void addFields(Map<String, Object> fields) {
		m_fields.putAll(fields);
	}

	public void addTag(String tag, String value) {
		m_tags.put(tag, value);
	}

	public void addTags(Map<String, String> tags) {
		m_tags.putAll(tags);
	}

	public String getEndPoint() {
		return m_endPoint;
	}

	public Map<String, Object> getFields() {
		return m_fields;
	}

	public String getMeasure() {
		return m_measure;
	}

	public Map<String, String> getTags() {
		return m_tags;
	}

	public long getTimestamp() {
		return m_timestamp;
	}

	public void setEndPoint(String endPoint) {
		m_endPoint = endPoint;
	}

	public void setFields(Map<String, Object> fields) {
		m_fields = fields;
	}

	public void setMeasure(String measure) {
		m_measure = measure;
	}

	public void setTags(Map<String, String> tags) {
		m_tags = tags;
	}

	public void setTimestamp(long timestamp) {
		m_timestamp = timestamp;
	}
}
