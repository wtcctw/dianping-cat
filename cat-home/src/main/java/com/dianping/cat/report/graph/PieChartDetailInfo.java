package com.dianping.cat.report.graph;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class PieChartDetailInfo {

	private List<Item> m_items = new LinkedList<Item>();

	public void add(Item item) {
		m_items.add(item);
	}

	public List<Item> getItems() {
		return m_items;
	}

	public List<Item> getSortedItems() {
		Collections.sort(m_items, new DetailInfoComparator());

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

	public static class DetailInfoComparator implements Comparator<Item> {

		@Override
		public int compare(Item o1, Item o2) {
			double sum2 = o2.getRequestSum();
			double sum1 = o1.getRequestSum();

			if (sum2 > sum1) {
				return 1;
			} else if (sum2 < sum1) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
