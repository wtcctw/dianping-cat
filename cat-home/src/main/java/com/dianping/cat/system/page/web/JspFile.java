package com.dianping.cat.system.page.web;

public enum JspFile {
	VIEW("/jsp/system/webconfig.jsp"),

	SPEED_UPDATE("/jsp/system/webRule/speedUpdate.jsp"),

	SPEED_LIST("/jsp/system/webRule/speed.jsp")
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
