package com.dianping.cat.config.app;

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
import com.dianping.cat.app.AppCommandData;

@Named(type = TableProvider.class, value = AppCommandTableProvider.LOGIC_TABLE_NAME)
public class AppCommandTableProvider implements TableProvider, Initializable {

	public final static String LOGIC_TABLE_NAME = "app-command-data";

	private String m_logicalTableName = LOGIC_TABLE_NAME;

	private String m_physicalTableName = "app_command_data";

	private String m_dataSourceName = "app";

	private Date m_historyDate;

	@Override
	public String getDataSourceName(Map<String, Object> hints) {
		AppCommandData command = (AppCommandData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		if (command.getPeriod().before(m_historyDate)) {
			return m_dataSourceName;
		} else {
			return m_dataSourceName + "_" + command.getCommandId() % 5;
		}
	}

	@Override
	public String getLogicalTableName() {
		return m_logicalTableName;
	}

	@Override
	public String getPhysicalTableName(Map<String, Object> hints) {
		AppCommandData command = (AppCommandData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + command.getCommandId();
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

	@Override
	public void initialize() throws InitializationException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		try {
			m_historyDate = sdf.parse("2016-03-22 21:00");
		} catch (ParseException e) {
			Cat.logError(e);
		}
	}

}