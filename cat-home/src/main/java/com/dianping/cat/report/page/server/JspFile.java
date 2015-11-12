package com.dianping.cat.report.page.server;

public enum JspFile {
	VIEW("/jsp/report/server/server.jsp"),

	SCREEN("/jsp/report/server/screen.jsp"),

	CONFIG("/jsp/report/server/config.jsp"),

	AGGREGATE("/jsp/report/server/aggregate.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
