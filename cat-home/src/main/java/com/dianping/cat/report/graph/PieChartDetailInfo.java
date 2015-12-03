package com.dianping.cat.report.graph;

import java.util.List;

public class PieChartDetailInfo {

	private List<Item> m_items;

	public void add(Item item) {
		m_items.add(item);
	}

	public List<Item> getItems() {
		return m_items;
	}

	public void setItems(List<Item> items) {
		m_items = items;
	}

	public static class Item {
		private int m_id;

		private String m_title;

		private double m_requestSum;

		private double m_successRatio;

		public int getId() {
			return m_id;
		}

		public double getRequestSum() {
			return m_requestSum;
		}

		public double getSuccessRatio() {
			return m_successRatio;
		}

		public String getTitle() {
			return m_title;
		}

		public Item setId(int id) {
			m_id = id;
			return this;
		}

		public Item setRequestSum(double requestSum) {
			m_requestSum = requestSum;
			return this;
		}

		public Item setSuccessRatio(double successRatio) {
			m_successRatio = successRatio;
			return this;
		}

		public Item setTitle(String title) {
			m_title = title;
			return this;
		}
	}
}
