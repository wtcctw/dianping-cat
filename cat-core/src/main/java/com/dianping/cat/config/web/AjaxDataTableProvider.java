package com.dianping.cat.config.web;

import java.util.Map;

import org.unidal.dal.jdbc.QueryEngine;
import org.unidal.dal.jdbc.mapping.TableProvider;

import com.dianping.cat.web.AjaxData;

public class AjaxDataTableProvider implements TableProvider {

	private String m_logicalTableName = "ajax-data";

	private String m_physicalTableName = "ajax_data";

	private String m_dataSourceName = "web";

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
		AjaxData webApiData = (AjaxData) hints.get(QueryEngine.HINT_DATA_OBJECT);

		return m_physicalTableName + "_" + webApiData.getApiId();
	}

	public void setDataSourceName(String dataSourceName) {
		m_dataSourceName = dataSourceName;
	}

	public void setLogicalTableName(String logicalTableName) {
		m_logicalTableName = logicalTableName;
	}

}