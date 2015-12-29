package com.dianping.cat.report.page.app.display;

import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.MapChart;

public class DashBoardInfo {

	private MapChart m_mapChart;
	
	private MapChart m_successMapChart;
	
	private BarChart m_operatorChart;
	
	private BarChart m_versionChart;
	
	private BarChart m_platformChart;
	
	private BarChart m_operatorSuccessChart;
	
	private BarChart m_platformSuccessChart;
	
	private BarChart m_versionSuccessChart;
	
	private LineChart m_lineChart;
	
	public MapChart getSuccessMapChart() {
		return m_successMapChart;
	}

	public void setSuccessMapChart(MapChart successMapChart) {
		m_successMapChart = successMapChart;
	}

	public BarChart getPlatformSuccessChart() {
		return m_platformSuccessChart;
	}

	public void setPlatformSuccessChart(BarChart platformSuccessChart) {
		m_platformSuccessChart = platformSuccessChart;
	}

	public BarChart getVersionSuccessChart() {
		return m_versionSuccessChart;
	}

	public void setVersionSuccessChart(BarChart versionSuccessChart) {
		m_versionSuccessChart = versionSuccessChart;
	}

	public BarChart getOperatorSuccessChart() {
		return m_operatorSuccessChart;
	}

	public void setOperatorSuccessChart(BarChart operatorSuccessChart) {
		m_operatorSuccessChart = operatorSuccessChart;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public BarChart getPlatformChart() {
		return m_platformChart;
	}

	public void setPlatformChart(BarChart platformChart) {
		m_platformChart = platformChart;
	}

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

	public BarChart getVersionChart() {
		return m_versionChart;
	}

	public void setVersionChart(BarChart versionChart) {
		m_versionChart = versionChart;
	}
	
}
