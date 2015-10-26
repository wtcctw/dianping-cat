package com.dianping.cat.report.page.browser;

import java.util.List;

public class ErrorMsg {
	private String m_msg;

	private int m_count;

	private List<Integer> m_ids;

	public String getMsg() {
		return m_msg;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

	public int getCount() {
		return m_count;
	}

	public void setCount(int count) {
		m_count = count;
	}

	public List<Integer> getIds() {
		return m_ids;
	}

	public void setIds(List<Integer> ids) {
		m_ids = ids;
	}

}
