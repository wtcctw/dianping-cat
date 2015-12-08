package com.dianping.cat.report.page.app.display;

public enum UpdateStatus {

	SUCCESS(200, "success"),

	NO_NAME(500, "name is required"),

	DUPLICATE_NAME(500, "name is duplicated"),

	INTERNAL_ERROR(500, "internal error");

	private int m_code;

	private String m_info;

	private UpdateStatus(int code, String info) {
		m_code = code;
		m_info = info;
	}

	public String getStatusJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("{\"status\":").append(m_code).append(", \"info\":\"").append(m_info).append("\"}");
		return sb.toString();
	}

}
