package com.dianping.cat.report.page.browser;

public enum JspFile {
	VIEW("/jsp/report/browser/web.jsp"),

	PIECHART("/jsp/report/browser/piechart.jsp"),

	JS_ERROR("/jsp/report/browser/jsError.jsp"),
	
	JS_ERROR_DETAIL("/jsp/report/browser/jsErrorDetail.jsp"),
	
	SPEED("/jsp/report/browser/speed.jsp"),
	
	SPEED_GRAPH("/jsp/report/browser/speedGraph.jsp")
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
