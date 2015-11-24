package com.dianping.cat.report.page.server;

public enum JspFile {
	GRAPH("/jsp/report/server/graph.jsp"),

	SCREEN("/jsp/report/server/screen.jsp"),

	AGGREGATE("/jsp/report/server/aggregate.jsp"),

	JSON("/jsp/report/server/json.jsp"),

	SCREENS("/jsp/report/server/screens.jsp"),

	SCREEN_UPDATE("/jsp/report/server/screenUpdate.jsp"),

	SCREEN_CONFIG_UPDATE("/jsp/report/server/graphUpdate.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
