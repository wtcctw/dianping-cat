package com.dianping.cat.report.page.business;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type;
	
	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.BUSINESS);
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		m_type = type;
	}

	public Date getHistoryEndDate() {
		try {
			if (m_customEnd != null && m_customEnd.length() > 0) {
				return m_format.parse(m_customEnd);
			} else {
				return TimeHelper.getCurrentHour(1);
			}
		} catch (Exception e) {
			return TimeHelper.getCurrentHour(1);
		}
	}

	public Date getHistoryStartDate() {
		try {
			if (m_customStart != null && m_customStart.length() > 0) {

				return m_format.parse(m_customStart);
			} else {
				return TimeHelper.getCurrentHour(-2);
			}
		} catch (Exception e) {
			return TimeHelper.getCurrentHour(-2);
		}
	}
	
	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.BUSINESS);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}

}
