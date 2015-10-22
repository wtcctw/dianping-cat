package com.dianping.cat.report.page.web.service;

import java.util.Date;

public class JsErrorQueryEntity {

	private String m_module;

	private String m_browser;

	private String m_level;

	private String m_msg;

	private Date m_start;

	private Date m_end;
	
	public JsErrorQueryEntity() {
		
	}
	
	public JsErrorQueryEntity(String query) {
		
	}

	public String getModule() {
		return m_module;
	}

	public void setModule(String module) {
		m_module = module;
	}

	public String getBrowser() {
		return m_browser;
	}

	public void setBrowser(String browser) {
		m_browser = browser;
	}

	public String getLevel() {
		return m_level;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public String getMsg() {
		return m_msg;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

	public Date getStart() {
		return m_start;
	}

	public void setStart(Date start) {
		m_start = start;
	}

	public Date getEnd() {
		return m_end;
	}

	public void setEnd(Date end) {
		m_end = end;
	}

}
