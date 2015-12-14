package com.dianping.cat.report.page.app.display;

import java.util.Collection;
import java.util.List;

import com.dianping.cat.configuration.server.filter.entity.CrashLogDomain;
import com.dianping.cat.report.ErrorMsg;
import com.dianping.cat.report.page.app.service.CrashLogService.FieldsInfo;

public class CrashLogDisplayInfo {
	
	private Collection<CrashLogDomain> m_appNames;
	
	private FieldsInfo m_fieldsInfo;
	
	private int m_totalCount;
	
	private List<ErrorMsg> m_errors;

	public Collection<CrashLogDomain> getAppNames() {
		return m_appNames;
	}

	public List<ErrorMsg> getErrors() {
		return m_errors;
	}

	public FieldsInfo getFieldsInfo() {
		return m_fieldsInfo;
	}

	public void setFieldsInfo(FieldsInfo fieldsInfo) {
		m_fieldsInfo = fieldsInfo;
	}

	public int getTotalCount() {
		return m_totalCount;
	}

	public void setAppNames(Collection<CrashLogDomain> appNames) {
		m_appNames = appNames;
	}

	public void setErrors(List<ErrorMsg> errors) {
		m_errors = errors;
	}

	public void setTotalCount(int totalCount) {
		m_totalCount = totalCount;
	}
	
}
