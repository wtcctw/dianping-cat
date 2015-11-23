package com.dianping.cat.report.page.server.display;

import java.util.List;

import com.dianping.cat.home.graph.entity.Graph;

public class MetricScreenInfo {
	private String m_name;

	private String m_graphName;

	private String m_category;

	private List<String> m_endPoints;

	private List<String> m_measures;

	private Graph m_graph;

	public String getCategory() {
		return m_category;
	}

	public List<String> getEndPoints() {
		return m_endPoints;
	}

	public String getGraphName() {
		return m_graphName;
	}

	public Graph getGraph() {
		return m_graph;
	}

	public List<String> getMeasures() {
		return m_measures;
	}

	public String getName() {
		return m_name;
	}

	public MetricScreenInfo setCategory(String category) {
		m_category = category;
		return this;
	}

	public MetricScreenInfo setEndPoints(List<String> endPoints) {
		m_endPoints = endPoints;
		return this;
	}

	public MetricScreenInfo setGraphName(String graphName) {
		m_graphName = graphName;
		return this;
	}

	public MetricScreenInfo setGraph(Graph graph) {
		m_graph = graph;
		return this;
	}

	public MetricScreenInfo setMeasures(List<String> measures) {
		m_measures = measures;
		return this;
	}

	public MetricScreenInfo setName(String name) {
		m_name = name;
		return this;
	}

}
