package com.dianping.cat.report.page.app.display;

import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.MapChart;

public class DashBoardInfo {

	private MapChart m_mapChart;
	
	private BarChart m_operatorChart;
	
	public MapChart getMapChart() {
		return m_mapChart;
	}

	public void setMapChart(MapChart mapChart) {
		m_mapChart = mapChart;
	}

	public BarChart getOperatorChart() {
		return m_operatorChart;
	}

	public void setOperatorChart(BarChart operatorChart) {
		m_operatorChart = operatorChart;
	}
	
}
