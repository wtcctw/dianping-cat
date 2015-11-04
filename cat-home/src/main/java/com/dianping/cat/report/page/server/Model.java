package com.dianping.cat.report.page.server;

import java.util.Collection;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_opState = SUCCESS;

	private String m_config;

	public Model(Context ctx) {
		super(ctx);
	}


	public String getConfig() {
		return m_config;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getDomains() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getOpState() {
		return m_opState;
	}


	public void setConfig(String config) {
		m_config = config;
	}


	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public void setOpState(String opState) {
		m_opState = opState;
	}
}
