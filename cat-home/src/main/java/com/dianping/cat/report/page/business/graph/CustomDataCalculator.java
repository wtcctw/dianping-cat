package com.dianping.cat.report.page.business.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.unidal.tuple.Pair;

public class CustomDataCalculator {

	private static final String START = "${";

	private static final String END = "}";

	private static final String SPLITTER = ",";

	public double[] calculate(String pattern, Map<String, double[]> datas) {
		return null;
	}

	public Pair<Boolean, List<CustomInfo>> translatePattern(String pattern) {
		List<CustomInfo> infos = new ArrayList<CustomInfo>();
		boolean result = true;
		int length = pattern.length();
		int start = -1;
		int end = -1;

		do {
			start = pattern.indexOf(START, end + 1);
			end = pattern.indexOf(END, end + 1);

			if (start >= 0 && end > 0 && start < end) {
				CustomInfo customInfo = new CustomInfo();

				String subStr = pattern.substring(start + 2, end);
				String[] strs = subStr.split(SPLITTER);

				if (strs != null && strs.length == 3) {
					customInfo.setDomain(strs[0]);
					customInfo.setKey(strs[1]);
					customInfo.setType(strs[2]);
					customInfo.setStart(start);
					customInfo.setEnd(end);
					infos.add(customInfo);
				} else {
					result = false;
				}
			} else {
				if (!(start < 0 && end < 0)) {
					result = false;
				}
			}
		} while (end >= 0 && start >= 0 && end < length);

		return new Pair<Boolean, List<CustomInfo>>(result, infos);
	}

}
