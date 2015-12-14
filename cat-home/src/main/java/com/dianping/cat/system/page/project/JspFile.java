package com.dianping.cat.system.page.project;

public enum JspFile {
	VIEW("/jsp/system/project.jsp"),

	PROJECT_UPDATE("/jsp/system/project/api.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
