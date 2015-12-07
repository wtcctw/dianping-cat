package com.dianping.cat.report.alert.spi;

public enum AlertLevel {

	WARNING("warning"),

	ERROR("error");

	private String m_level;

	public static AlertLevel findByName(String level) {
		for (AlertLevel tmp : values()) {
			if (tmp.getLevel().equals(level)) {
				return tmp;
			}
		}
		return WARNING;
	}

	private AlertLevel(String level) {
		m_level = level;
	}

	public String getLevel() {
		return m_level;
	}

	public void setLevel(String level) {
		m_level = level;
	}

}
