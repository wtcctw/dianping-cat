package com.dianping.cat.system.page.web;

import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("page")
	private String m_webPage;

	@FieldMeta("stepId")
	private int m_stepId;

	@ObjectMeta("step")
	private Step m_step;

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.SPEED_LIST);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getWebPage() {
		return m_webPage;
	}

	public int getStepId() {
		return m_stepId;
	}

	public String getReportType() {
		return "";
	}

	public Step getStep() {
		return m_step;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.WEB);
	}

	public void setWebPage(String webPage) {
		m_webPage = webPage;
	}

	public void setStepId(int stepId) {
		m_stepId = stepId;
	}

	public void setStep(Step step) {
		m_step = step;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.SPEED_LIST;
		}
	}
}
