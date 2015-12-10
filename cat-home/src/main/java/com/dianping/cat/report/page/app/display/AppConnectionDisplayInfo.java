package com.dianping.cat.report.page.app.display;

import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChartDetailInfo;

public class AppConnectionDisplayInfo {
	
	private PieChart m_pieChart;

	private PieChartDetailInfo m_pieChartDetailInfo;

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	public PieChartDetailInfo getPieChartDetailInfo() {
		return m_pieChartDetailInfo;
	}

	public void setPieChartDetailInfo(PieChartDetailInfo pieChartDetailInfo) {
		m_pieChartDetailInfo = pieChartDetailInfo;
	}
}
