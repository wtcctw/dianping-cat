package com.dianping.cat.report.page.browser.display;

import java.util.LinkedList;
import java.util.List;

public class AjaxDistributeDetails {

	private List<DistributeDetail> m_details = new LinkedList<DistributeDetail>();

	public void addPieChartDetailInfo(DistributeDetail info) {
		m_details.add(info);
	}

	public List<DistributeDetail> getDetails() {
		return m_details;
	}

	public static class DistributeDetail {
		private int m_id;

		private String m_title;

		private double m_requestSum;

		private double m_delayAvg;

		private double m_ratio;

		public int getId() {
			return m_id;
		}

		public double getRequestSum() {
			return m_requestSum;
		}

		public double getRatio() {
			return m_ratio;
		}

		public double getDelayAvg() {
			return m_delayAvg;
		}

		public String getTitle() {
			return m_title;
		}

		public DistributeDetail setId(int id) {
			m_id = id;
			return this;
		}

		public DistributeDetail setRequestSum(double requestSum) {
			m_requestSum = requestSum;
			return this;
		}

		public void setRatio(double ratio) {
			m_ratio = ratio;
		}

		public void setDelayAvg(double delayAvg) {
			m_delayAvg = delayAvg;
		}

		public DistributeDetail setTitle(String title) {
			m_title = title;
			return this;
		}
	}
}
