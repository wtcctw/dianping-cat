package com.dianping.cat.report;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.HourlyReport;

@Named(type = TableProvider.class, value = HourlyReportTableProvider.LOGIC_TABLE_NAME)
public class HourlyReportTableProvider implements TableProvider, Initializable {

	public final static String LOGIC_TABLE_NAME = "report";

	private String m_logicalTableName = LOGIC_TABLE_NAME;

	private String m_physicalTableName = LOGIC_TABLE_NAME;

	private String m_dataSourceName = "cat";

	private Date m_historyDate;

	@Override
	public String getDataSourceName(Map<String, Object> hints) {
		return m_dataSourceName;
	}

	@Override
	public String getLogicalTableName() {
		return m_logicalTableName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints) {
		HourlyReport command = (HourlyReport) hints.get(QueryEngine.HINT_DATA_OBJECT);

		if (command.getPeriod().before(m_historyDate)) {
			return m_physicalTableName;
		} else {
			return "hourlyreport";
		}
	}

	@Override
	public void initialize() throws InitializationException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		try {
			m_historyDate = sdf.parse("2016-04-16 00:00");
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}