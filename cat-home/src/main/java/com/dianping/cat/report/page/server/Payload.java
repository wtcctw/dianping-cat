package com.dianping.cat.report.page.server;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("timeRange")
	private int m_timeRange = 24;

	@FieldMeta("screen")
	private String m_screen;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("graphId")
	private int m_graphId;

	private ReportPage m_page;

	public Payload() {
		super(ReportPage.SERVER);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getContent() {
		return m_content;
	}

	public int getGraphId() {
		return m_graphId;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getScreen() {
		return m_screen;
	}

	public int getTimeRange() {
		return m_timeRange;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setGraphId(int graphId) {
		m_graphId = graphId;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.SERVER);
	}

	public void setScreen(String screen) {
		m_screen = screen;
	}

	public void setTimeRange(int timeRange) {
		m_timeRange = timeRange;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
