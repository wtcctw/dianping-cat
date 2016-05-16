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
import com.dianping.cat.core.dal.HourlyReportContent;

@Named(type = TableProvider.class, value = HourlyReportContentTableProvider.LOGIC_TABLE_NAME)
public class HourlyReportContentTableProvider implements TableProvider, Initializable {

	public final static String LOGIC_TABLE_NAME = "report-content";

	private String m_logicalTableName = LOGIC_TABLE_NAME;

	private Date m_historyDate;

	@Override
	public String getDataSourceName(Map<String, Object> hints) {
		HourlyReportContent report = (HourlyReportContent) hints.get(QueryEngine.HINT_DATA_OBJECT);
		Date period = report.getPeriod();

		if (period != null && period.before(m_historyDate)) {
			return "cat";
		} else {
			return "cat_0";
		}
	}

	@Override
	public String getLogicalTableName() {
		return m_logicalTableName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints) {
		HourlyReportContent report = (HourlyReportContent) hints.get(QueryEngine.HINT_DATA_OBJECT);
		Date period = report.getPeriod();

		if (period != null && period.before(m_historyDate)) {
			return "report_content";
		} else {
			return "hourly_report_content";
		}
	}

	@Override
	public void initialize() throws InitializationException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		try {
			m_historyDate = sdf.parse("2016-05-16 20:00:00");
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}