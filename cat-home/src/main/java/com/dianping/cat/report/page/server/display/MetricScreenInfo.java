package com.dianping.cat.report.page.server.display;

import java.util.List;

import com.dianping.cat.home.graph.entity.Graph;

public class MetricScreenInfo {
	private int m_id;

	private String m_name;

	private String m_graphName;

	private String m_category;

	private List<Graph> m_graphs;

	public String getCategory() {
		return m_category;
	}

	public String getGraphName() {
		return m_graphName;
	}

	public List<Graph> getGraphs() {
		return m_graphs;
	}

	public int getId() {
		return m_id;
	}

	public String getName() {
		return m_name;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public void setGraphName(String graphName) {
		m_graphName = graphName;
	}

	public void setGraphs(List<Graph> graphs) {
		m_graphs = graphs;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setName(String name) {
		m_name = name;
	}

}
