package com.dianping.cat.report.page.app.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;

import com.dianping.cat.Cat;

public class CommandQueryEntity extends BaseQueryEntity {

	public static final int DEFAULT_COMMAND = 1;

	private int m_code = DEFAULT_VALUE;

	private int m_connectType = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	public CommandQueryEntity() {
		super();
		m_id = DEFAULT_COMMAND;
	}

	public CommandQueryEntity(String query) {
		List<String> strs = Splitters.by(";").split(query);

		try {
			m_date = parseDate(strs.get(0));
			m_id = parseValue(strs.get(1));
			m_code = parseValue(strs.get(2));
			m_network = parseValue(strs.get(3));
			m_version = parseValue(strs.get(4));
			m_connectType = parseValue(strs.get(5));
			m_platfrom = parseValue(strs.get(6));
			m_city = parseValue(strs.get(7));
			m_operator = parseValue(strs.get(8));
			m_startMinuteOrder = convert2MinuteOrder(strs.get(9));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(10));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public CommandQueryEntity(Date date, String conditions, int start, int end) {
		m_date = date;
		m_startMinuteOrder = start;
		m_endMinuteOrder = end;

		List<String> strs = Splitters.by(";").split(conditions);

		try {
			m_id = parseValue(strs.get(0));
			m_code = parseValue(strs.get(1));
			m_network = parseValue(strs.get(2));
			m_version = parseValue(strs.get(3));
			m_connectType = parseValue(strs.get(4));
			m_platfrom = parseValue(strs.get(5));
			m_city = parseValue(strs.get(6));
			m_operator = parseValue(strs.get(7));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public CommandQueryEntity(int id) {
		super();
		m_id = id;
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		m_startMinuteOrder = hour * 60 + minute - 15;

		if (m_startMinuteOrder < 0) {
			m_startMinuteOrder = DEFAULT_VALUE;
		}
	}

	public int getCode() {
		return m_code;
	}

	public int getConnectType() {
		return m_connectType;
	}

	public int getEndMinuteOrder() {
		return m_endMinuteOrder;
	}

	public int getId() {
		return m_id;
	}

	public int getStartMinuteOrder() {
		return m_startMinuteOrder;
	}

}