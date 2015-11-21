package com.dianping.cat.report.page.server;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.metric.Range;
import com.dianping.cat.report.page.server.display.MetricScreenInfo;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private List<LineChart> m_lineCharts;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_opState = SUCCESS;

	private String m_config;

	private Date m_startTime;

	private Date m_endTime;

	private List<Graph> m_graphs;

	private Map<String, List<String>> m_screenGroups;

	private Set<String> m_endPoints;

	private String m_json;

	private MetricScreenInfo m_metricScreenInfo;

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

	public Set<String> getEndPoints() {
		return m_endPoints;
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public List<Graph> getGraphs() {
		return m_graphs;
	}

	public String getJson() {
		return m_json;
	}

	public List<LineChart> getLineCharts() {
		return m_lineCharts;
	}

	public MetricScreenInfo getMetricScreenInfo() {
		return m_metricScreenInfo;
	}

	public String getOpState() {
		return m_opState;
	}

	public Map<String, List<String>> getScreenGroups() {
		return m_screenGroups;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public void setConfig(String config) {
		m_config = config;
	}

	public void setEndPoints(Set<String> endPoints) {
		m_endPoints = endPoints;
	}

	public void setEndTime(Date endTime) {
		m_endTime = endTime;
	}

	public void setGraphs(List<Graph> graphs) {
		m_graphs = graphs;
	}

	public void setJson(String json) {
		m_json = json;
	}

	public void setLineCharts(List<LineChart> lineCharts) {
		m_lineCharts = lineCharts;
	}

	public void setMetricScreenInfo(MetricScreenInfo metricScreenInfo) {
		m_metricScreenInfo = metricScreenInfo;
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

	public void setScreenGroups(Map<String, List<String>> screenGroups) {
		m_screenGroups = screenGroups;
	}

	public void setStartTime(Date startTime) {
		m_startTime = startTime;
	}
}
