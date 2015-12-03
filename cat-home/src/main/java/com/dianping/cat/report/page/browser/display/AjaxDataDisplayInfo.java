package com.dianping.cat.report.page.browser.display;

import java.util.List;
import java.util.Map;

import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;

public class AjaxDataDisplayInfo {

	private LineChart m_lineChart;

	private PieChart m_pieChart;

	private AjaxPieChartDetailInfos m_pieChartDetailInfos;

	private Map<String, AjaxDataDetail> m_comparisonAjaxDetails;

	private List<AjaxDataDetail> m_ajaxDataDetailInfos;

	public List<AjaxDataDetail> getAjaxDataDetailInfos() {
		return m_ajaxDataDetailInfos;
	}

	public Map<String, AjaxDataDetail> getComparisonAjaxDetails() {
		return m_comparisonAjaxDetails;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public AjaxPieChartDetailInfos getPieChartDetailInfos() {
		return m_pieChartDetailInfos;
	}

	public void setAjaxDataDetailInfos(List<AjaxDataDetail> ajaxDataDetailInfos) {
		m_ajaxDataDetailInfos = ajaxDataDetailInfos;
	}

	public void setComparisonAjaxDetails(Map<String, AjaxDataDetail> comparisonAjaxDetail) {
		m_comparisonAjaxDetails = comparisonAjaxDetail;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	public void setPieChartDetailInfos(AjaxPieChartDetailInfos pieChartDetailInfos) {
		m_pieChartDetailInfos = pieChartDetailInfos;
	}

}
