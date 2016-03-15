package com.dianping.cat.system.page.business;

public enum JspFile {
	VIEW("/jsp/system/business.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
