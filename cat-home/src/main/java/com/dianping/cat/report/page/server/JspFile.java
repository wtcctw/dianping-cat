package com.dianping.cat.report.page.server;

public enum JspFile {
	VIEW("/jsp/report/server/server.jsp"),

	CONFIG("/jsp/report/server/config.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
