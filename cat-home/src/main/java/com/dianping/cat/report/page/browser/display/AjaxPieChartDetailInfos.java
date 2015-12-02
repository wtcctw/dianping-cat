package com.dianping.cat.report.page.browser.display;

import java.util.LinkedList;
import java.util.List;

public class AjaxPieChartDetailInfos {

	private List<PieChartDetailInfo> m_details = new LinkedList<PieChartDetailInfo>();

	public void addPieChartDetailInfo(PieChartDetailInfo info) {
		m_details.add(info);
	}
	
	public List<PieChartDetailInfo> getDetails() {
		return m_details;
	}

	public static class PieChartDetailInfo {
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

		public PieChartDetailInfo setId(int id) {
			m_id = id;
			return this;
		}

		public PieChartDetailInfo setRequestSum(double requestSum) {
			m_requestSum = requestSum;
			return this;
		}

		public void setSuccessRatio(double successRatio) {
			m_successRatio = successRatio;
		}

		public PieChartDetailInfo setTitle(String title) {
			m_title = title;
			return this;
		}
	}
}
