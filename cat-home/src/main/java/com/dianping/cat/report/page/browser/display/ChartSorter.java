package com.dianping.cat.report.page.browser.display;

import java.util.Comparator;

import com.dianping.cat.report.graph.PieChartDetailInfo;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.browser.service.AjaxDataService;

public class ChartSorter {

	private String m_sortBy;

	public ChartSorter() {
		this(AppDataService.REQUEST);
	}

	public ChartSorter(String sortBy) {
		m_sortBy = sortBy;
	}

	public Comparator<AjaxDataDetail> buildLineChartInfoComparator() {

		return new LineChartDetailInfoComparator();
	}

	public Comparator<PieChartDetailInfo> buildPieChartInfoComparator() {

		return new PieChartDetailInfoComparator();
	}

	public class LineChartDetailInfoComparator implements Comparator<AjaxDataDetail> {

		@Override
		public int compare(AjaxDataDetail o1, AjaxDataDetail o2) {
			if (AjaxDataService.SUCCESS.equals(m_sortBy)) {
				return (int) ((o2.getSuccessRatio() - o1.getSuccessRatio()) * 1000);
			} else if (AjaxDataService.REQUEST.equals(m_sortBy)) {
				return (int) (o2.getAccessNumberSum() - o1.getAccessNumberSum());
			} else if (AjaxDataService.DELAY.equals(m_sortBy)) {
				return (int) ((o2.getResponseTimeAvg() - o1.getResponseTimeAvg()) * 1000);
			} else if (AjaxDataService.REQUEST_PACKAGE.equals(m_sortBy)) {
				return (int) ((o2.getRequestPackageAvg() - o1.getRequestPackageAvg()) * 1000);
			} else if (AjaxDataService.RESPONSE_PACKAGE.equals(m_sortBy)) {
				return (int) ((o2.getResponsePackageAvg() - o1.getResponsePackageAvg()) * 1000);
			} else {
				return 0;
			}
		}
	}

	public class PieChartDetailInfoComparator implements Comparator<PieChartDetailInfo> {

		@Override
		public int compare(PieChartDetailInfo o1, PieChartDetailInfo o2) {
			return (int) (o2.getRequestSum() - o1.getRequestSum());
		}
	}

}
