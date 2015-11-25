package com.dianping.cat.report.page.browser.display;

import java.util.List;
import java.util.Map;

import com.dianping.cat.report.graph.LineChart;

public class WebSpeedDisplayInfo {

	private LineChart m_lineChart;

	private Map<String, WebSpeedDetail> m_webSpeedSummarys;

	private Map<String, List<WebSpeedDetail>> m_webSpeedDetails;

	public Map<String, List<WebSpeedDetail>> getWebSpeedDetails() {
		return m_webSpeedDetails;
	}

	public Map<String, WebSpeedDetail> getWebSpeedSummarys() {
		return m_webSpeedSummarys;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public void setWebSpeedDetails(Map<String, List<WebSpeedDetail>> webSpeedDetails) {
		m_webSpeedDetails = webSpeedDetails;
	}

	public void setWebSpeedSummarys(Map<String, WebSpeedDetail> webSpeedSummarys) {
		m_webSpeedSummarys = webSpeedSummarys;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}
}
