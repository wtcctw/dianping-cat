package com.dianping.cat.report.page.business.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.report.page.business.task.BusinessKeyHelper;

public class CustomDataCalculator {

	private static final JexlEngine jexl = new JexlBuilder().cache(512).strict(true).silent(false).create();

	@Inject
	private BusinessKeyHelper m_keyHelper;

	private static final String START = "${";

	private static final String END = "}";

	private static final String SPLITTER = ",";

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
					customInfo.setType(strs[2].toUpperCase());
					customInfo.setPattern(pattern.substring(start, end + 1));

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

	public double[] calculate(String pattern, List<CustomInfo> customInfos, Map<String, double[]> businessItemDataCache,
	      int totalSize) {
		double[] result = new double[totalSize];

		for (int i = 0; i < totalSize; i++) {
			String expression = pattern;

			for (CustomInfo customInfo : customInfos) {
				String customPattern = customInfo.getPattern();
				String itemId = m_keyHelper.generateKey(customInfo.getKey(), customInfo.getDomain(), customInfo.getType());
				double[] sourceData = businessItemDataCache.get(itemId);

				if (sourceData != null) {
					expression = expression.replace(customPattern, Double.toString(sourceData[i]));
				}
			}
			
			result[i] = calculate(expression);
		}
		return result;
	}

	private double calculate(String pattern) {
		JexlExpression e = jexl.createExpression(pattern);
		Number result = (Number) e.evaluate(null);
		double aa = result.doubleValue();
		return aa;
	}
	
}
