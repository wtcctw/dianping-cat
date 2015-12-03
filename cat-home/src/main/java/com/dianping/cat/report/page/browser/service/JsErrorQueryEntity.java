package com.dianping.cat.report.page.browser.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.config.web.js.Level;
import com.dianping.cat.helper.TimeHelper;

public class JsErrorQueryEntity {

	private String m_startTime;

	private String m_endTime;

	private String m_level;

	private String m_module;

	private String m_msg;

	private String m_dpid;

	private static final String ALL = "ALL";

	private String m_day;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Date buildEndTime() {
		if (StringUtils.isNotBlank(m_day) && StringUtils.isNotBlank(m_endTime)) {
			try {
				Date date = m_format.parse(m_day + " " + m_endTime);
				return date;
			} catch (ParseException e) {
			}
		}
		return TimeHelper.getCurrentDay(1);
	}

	public int buildLevel() {
		if (StringUtils.isEmpty(m_level) || ALL.equals(m_level)) {
			return -1;
		} else {
			return Level.getCodeByName(m_level);
		}
	}

	public Date buildStartTime() {
		if (StringUtils.isNotBlank(m_day) && StringUtils.isNotBlank(m_startTime)) {
			try {
				Date date = m_format.parse(m_day + " " + m_startTime);
				return date;
			} catch (ParseException e) {
			}
		}
		return TimeHelper.getCurrentHour();
	}

	public String getDpid() {
		if (StringUtils.isEmpty(m_dpid)) {
			return null;
		} else {
			return m_dpid;
		}
	}

	public String getEndTime() {
		return m_endTime;
	}

	public String getLevel() {
		return m_level;
	}

	public String getModule() {
		if (StringUtils.isEmpty(m_module)) {
			return null;
		} else {
			return m_module;
		}
	}

	public String getMsg() {
		return m_msg;
	}

	public String getStartTime() {
		return m_startTime;
	}

	public void setDpid(String dpid) {
		m_dpid = dpid;
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public void setModule(String module) {
		m_module = module;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	public String getDay() {
		return m_day;
	}

	public void setDay(String day) {
		m_day = day;
	}

}
