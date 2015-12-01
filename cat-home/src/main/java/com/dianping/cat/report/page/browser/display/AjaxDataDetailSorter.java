package com.dianping.cat.report.page.browser.display;

import java.util.Comparator;

import com.dianping.cat.report.page.browser.service.AjaxDataService;

public class AjaxDataDetailSorter implements Comparator<AjaxDataDetail> {

	private String m_sortBy;

	public AjaxDataDetailSorter(String sortBy) {
		m_sortBy = sortBy;
	}

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
