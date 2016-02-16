package com.dianping.cat.api.page.metric;

public enum Status {

	SUCCESS(200, "success"),

	FAILURE(500, "failed");

	private int m_code;

	private String m_info;

	private Status(int code, String info) {
		m_code = code;
		m_info = info;
	}

	public String getStatusJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("{\"status\":").append(m_code).append(", \"info\":\"").append(m_info).append("\"}");
		return sb.toString();
	}

}
