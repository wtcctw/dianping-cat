package com.dianping.cat.metric;

public enum MetricType {

	AVG("mean"),

	SUM("sum"),

	COUNT("count");

	private String m_name;

	MetricType(String name) {
		m_name = name;
	}

	public static MetricType getByName(String name, MetricType defaultType) {
		for (MetricType action : MetricType.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultType;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

}
