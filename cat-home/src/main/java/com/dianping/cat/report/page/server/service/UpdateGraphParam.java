package com.dianping.cat.report.page.server.service;

import java.util.List;

public class UpdateGraphParam {
	private String m_name;

	private String m_category;

	private String m_graphName;

	private String m_view;

	private List<String> m_endPoints;

	private List<String> m_measurements;

	public String getCategory() {
		return m_category;
	}

	public List<String> getEndPoints() {
		return m_endPoints;
	}

	public String getGraphName() {
		return m_graphName;
	}

	public List<String> getMeasurements() {
		return m_measurements;
	}

	public String getName() {
		return m_name;
	}

	public String getView() {
		return m_view;
	}

	public UpdateGraphParam setCategory(String category) {
		m_category = category;
		return this;
	}

	public UpdateGraphParam setEndPoints(List<String> endPoints) {
		m_endPoints = endPoints;
		return this;
	}

	public UpdateGraphParam setGraphName(String graphName) {
		m_graphName = graphName;
		return this;
	}

	public UpdateGraphParam setMeasurements(List<String> measurements) {
		m_measurements = measurements;
		return this;
	}

	public UpdateGraphParam setName(String name) {
		m_name = name;
		return this;
	}

	public UpdateGraphParam setView(String view) {
		m_view = view;
		return this;
	}
}
