package com.dianping.cat.report.page.browser;

public enum JspFile {
	VIEW("/jsp/report/browser/web.jsp"),

	PIECHART("/jsp/report/browser/piechart.jsp"),

	JS_ERROR("/jsp/report/browser/jsError.jsp"),
	
	JS_ERROR_DETAIL("/jsp/report/browser/jsErrorDetail.jsp")
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
