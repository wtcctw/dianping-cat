package com.dianping.cat.report.page.browser;

import java.util.ArrayList;
import java.util.List;

public class ErrorMsg implements Comparable<ErrorMsg> {

	private String m_msg;

	private int m_count;

	private List<Integer> m_ids = new ArrayList<Integer>();

	private final int MAX_ID_COUNT = 60;

	public void addCount() {
		m_count++;
	}

	public void addId(int id) {
		if (m_ids.size() < MAX_ID_COUNT) {
			m_ids.add(id);
		}
	}

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

	@Override
	public int compareTo(ErrorMsg o) {
		return o.getCount() - this.m_count;
	}

}
