package com.dianping.cat.report.page.eslog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("day")
	private String m_day;

	@FieldMeta("start")
	private String m_start;

	@FieldMeta("end")
	private String m_end;

	@FieldMeta("business")
	private String m_business;

	@FieldMeta("dpid")
	private String m_dpid;

	@FieldMeta("content")
	private String m_content;
	
	@FieldMeta("type")
	private String m_type = "applog";

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public Payload() {
		super(ReportPage.ESLOG);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getBusiness() {
		return m_business;
	}

	public String getContent() {
		return m_content;
	}

	public String getDpid() {
		return m_dpid;
	}

	public String getEnd() {
		return new SimpleDateFormat("HH:mm").format(getEndDate());
	}

	public Date getEndDate() {
		if (m_day != null && m_end != null) {
			try {
				return m_sdf.parse(m_day + " " + m_end);
			} catch (ParseException e) {
				// ingnore
			}
		}
		return new Date();
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getStart() {
		return m_sdf.format(getStartDate());
	}

	public Date getStartDate() {
		if (m_day != null && m_start != null) {
			try {
				return m_sdf.parse(m_day + " " + m_start);
			} catch (ParseException e) {
				// ingnore
			}
		}
		return new Date(System.currentTimeMillis() - TimeHelper.ONE_HOUR);
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setBusiness(String business) {
		m_business = business;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDpid(String dpid) {
		m_dpid = dpid;
	}

	public void setEnd(String end) {
		m_end = end;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ESLOG);
	}

	public void setStart(String start) {
		m_start = start;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}

}
