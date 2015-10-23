package com.dianping.cat.report.page.browser;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	public Payload() {
		super(ReportPage.BROWSER);
	}

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.JS_ERROR);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.BROWSER);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.JS_ERROR;
		}
	}
}
