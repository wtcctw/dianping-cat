package com.dianping.cat.report.page.server;

public enum JspFile {
	VIEW("/jsp/report/server/server.jsp"),

	SCREEN("/jsp/report/server/screen.jsp"),

	AGGREGATE("/jsp/report/server/aggregate.jsp"),

	JSON("/jsp/report/server/json.jsp"),

	SCREEN_CONFIG_UPDATE("/jsp/report/server/screenConfigUpdate.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
