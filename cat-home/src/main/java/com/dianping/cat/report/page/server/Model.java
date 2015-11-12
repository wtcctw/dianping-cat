package com.dianping.cat.report.page.server;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.unidal.web.mvc.view.annotation.EntityMeta;

import com.dianping.cat.home.screen.entity.Screen;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.metric.Range;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_opState = SUCCESS;

	private String m_config;

	private Date m_startTime;

	private Date m_endTime;

	private Collection<Screen> m_screens;

	public Model(Context ctx) {
		super(ctx);
	}

	public Range[] getAllRange() {
		return Range.values();
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
		return getDisplayDomain();
	}

	@Override
	public Collection<String> getDomains() {
		return new HashSet<String>();
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public String getOpState() {
		return m_opState;
	}

	public Collection<Screen> getScreens() {
		return m_screens;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public void setConfig(String config) {
		m_config = config;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
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

	public void setScreens(Collection<Screen> screens) {
		m_screens = screens;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}
}
