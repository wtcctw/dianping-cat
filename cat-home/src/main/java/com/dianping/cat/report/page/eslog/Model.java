package com.dianping.cat.report.page.eslog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private List<String> m_logs;

	private List<String> m_logTypes;

	private String m_content;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getContent() {
		return m_content;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public List<String> getLogs() {
		return m_logs;
	}

	public List<String> getLogTypes() {
		return m_logTypes;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setLogs(List<String> logs) {
		m_logs = logs;
	}

	public void setLogTypes(List<String> logTypes) {
		m_logTypes = logTypes;
	}

}
