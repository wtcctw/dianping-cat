package com.dianping.cat.report.page.browser;

public enum JspFile {
	JS_ERROR("/jsp/report/browser/jsError.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
