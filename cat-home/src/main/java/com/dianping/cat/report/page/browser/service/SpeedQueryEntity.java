package com.dianping.cat.report.page.browser.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class SpeedQueryEntity {

	public static final int DEFAULT_VALUE = -1;

	private Date m_date;

	private int m_network = DEFAULT_VALUE;

	private int m_platfrom = DEFAULT_VALUE;

	private int m_city = DEFAULT_VALUE;

	private int m_operator = DEFAULT_VALUE;

	private int m_pageId = DEFAULT_VALUE;

	private int m_stepId = DEFAULT_VALUE;

	public SpeedQueryEntity() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		m_date = cal.getTime();
	}

	public SpeedQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_pageId = parseValue(strs.get(1));
			m_stepId = parseValue(strs.get(2));
			m_network = parseValue(strs.get(3));
			m_platfrom = parseValue(strs.get(4));
			m_city = parseValue(strs.get(5));
			m_operator = parseValue(strs.get(6));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public int getCity() {
		return m_city;
	}

	public Date getDate() {
		return m_date;
	}

	public int getNetwork() {
		return m_network;
	}

	public int getOperator() {
		return m_operator;
	}

	public int getPlatfrom() {
		return m_platfrom;
	}

	public int getPageId() {
		return m_pageId;
	}

	public int getStepId() {
		return m_stepId;
	}

	private Date parseDate(String dateStr) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if (StringUtils.isNotEmpty(dateStr)) {
			return sdf.parse(dateStr);
		} else {
			Calendar cal = Calendar.getInstance();

			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);

			return cal.getTime();
		}
	}

	private int parseValue(String str) {
		if (StringUtils.isEmpty(str)) {
			return DEFAULT_VALUE;
		} else {
			return Integer.parseInt(str);
		}
	}

	public void setPageId(int pageId) {
		m_pageId = pageId;
	}

	public void setStepId(int stepId) {
		m_stepId = stepId;
	}

}