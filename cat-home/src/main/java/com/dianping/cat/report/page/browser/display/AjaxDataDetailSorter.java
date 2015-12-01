package com.dianping.cat.report.page.browser.display;

import java.util.Comparator;

import com.dianping.cat.report.page.browser.service.QueryType;

public class AjaxDataDetailSorter implements Comparator<AjaxDataDetail> {

	private QueryType m_sortBy;

	public AjaxDataDetailSorter(QueryType sortBy) {
		m_sortBy = sortBy;
	}

	@Override
	public int compare(AjaxDataDetail o1, AjaxDataDetail o2) {
		switch (m_sortBy) {
		case SUCCESS:
			return (int) ((o2.getSuccessRatio() - o1.getSuccessRatio()) * 1000);
		case REQUEST:
			return (int) (o2.getAccessNumberSum() - o1.getAccessNumberSum());
		case DELAY:
			return (int) ((o2.getResponseTimeAvg() - o1.getResponseTimeAvg()) * 1000);
		case REQUEST_PACKAGE:
			return (int) ((o2.getRequestPackageAvg() - o1.getRequestPackageAvg()) * 1000);
		case RESPONSE_PACKAGE:
			return (int) ((o2.getResponsePackageAvg() - o1.getResponsePackageAvg()) * 1000);
		}
		return 0;
	}
}
