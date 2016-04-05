package com.dianping.cat.report.page.app.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Splitters;

import com.dianping.cat.Cat;

public class CommandQueryEntity extends BaseQueryEntity implements LogEnabled {

	public static final int DEFAULT_COMMAND = 1;

	protected int m_code = DEFAULT_VALUE;

	protected int m_connectType = DEFAULT_VALUE;

	protected int m_source = DEFAULT_VALUE;

	private int m_startMinuteOrder = DEFAULT_VALUE;

	private int m_endMinuteOrder = DEFAULT_VALUE;

	private Logger m_logger;

	public CommandQueryEntity() {
		super();
		m_id = DEFAULT_COMMAND;
	}

	public CommandQueryEntity(Date date, String conditions, int start, int end) {
		m_date = date;
		m_startMinuteOrder = start - start % 5;
		m_endMinuteOrder = end - end % 5;

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

			if (strs.size() > 8) {
				m_source = parseValue(strs.get(8));
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public CommandQueryEntity(int id) {
		super();
		m_id = id;
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		m_endMinuteOrder = hour * 60 + minute;
		m_endMinuteOrder = m_endMinuteOrder - m_endMinuteOrder % 5;
		m_startMinuteOrder = m_endMinuteOrder - 30;

		if (m_startMinuteOrder < 0) {
			m_startMinuteOrder = DEFAULT_VALUE;
		}
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
			m_source = parseValue(strs.get(9));
			m_startMinuteOrder = convert2MinuteOrder(strs.get(10));
			m_endMinuteOrder = convert2MinuteOrder(strs.get(11));
		} catch (Exception e) {
			m_logger.error(query, e);
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

	public int getSource() {
		return m_source;
	}

	public int getStartMinuteOrder() {
		return m_startMinuteOrder;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}