package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.browser.display.AjaxPieChartDetailInfos;
import com.dianping.cat.report.page.browser.display.AjaxPieChartDetailInfos.PieChartDetailInfo;
import com.dianping.cat.web.AjaxData;

public class AjaxGraphCreator {
	@Inject
	private AjaxDataBuilder m_dataBuilder;

	@Inject
	private AjaxDataService m_WebApiService;

	@Inject
	private WebConfigManager m_webConfigManager;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	public LineChart buildChartData(final Map<String, Double[]> datas, AjaxQueryType type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle(type.getTitle());

		if (AjaxQueryType.SUCCESS.getType().equals(type)) {
			lineChart.setMinYlable(lineChart.queryMinYlable(datas));
			lineChart.setMaxYlabel(100D);
		}

		for (Entry<String, Double[]> entry : datas.entrySet()) {
			Double[] data = entry.getValue();

			lineChart.add(entry.getKey(), data);
		}
		return lineChart;
	}

	public LineChart buildLineChart(AjaxDataQueryEntity queryEntity1, AjaxDataQueryEntity queryEntity2, AjaxQueryType type) {
		Map<String, Double[]> datas = new LinkedHashMap<String, Double[]>();

		if (queryEntity1 != null) {
			Double[] data = m_WebApiService.queryGraphValue(queryEntity1, type);

			datas.put(Constants.CURRENT_STR, data);
		}

		if (queryEntity2 != null) {
			Double[] data = m_WebApiService.queryGraphValue(queryEntity2, type);

			datas.put(Constants.COMPARISION_STR, data);
		}
		return buildChartData(datas, type);
	}

	public Pair<PieChart, AjaxPieChartDetailInfos> buildPieChart(AjaxDataQueryEntity entity, AjaxDataField field) {
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();
		List<AjaxData> datas = m_dataBuilder.queryByField(entity, field);

		for (AjaxData data : datas) {
			items.add(buildPieChartItem(entity.getId(), data, field));
		}
		pieChart.setTitle(field.getName() + "访问情况");
		pieChart.addItems(items);

		AjaxPieChartDetailInfos infos = buildPieChartDetailInfo(items);

		return new Pair<PieChart, AjaxPieChartDetailInfos>(pieChart, infos);
	}

	private Pair<Integer, String> buildPieChartFieldTitlePair(int command, AjaxData data, AjaxDataField field) {
		String title = "Unknown";
		int keyValue = -1;

		switch (field) {
		case OPERATOR:
			Map<Integer, com.dianping.cat.configuration.web.entity.Item> operators = m_webConfigManager
			      .queryConfigItem(WebConfigManager.OPERATOR);
			com.dianping.cat.configuration.web.entity.Item operator = null;
			keyValue = data.getOperator();

			if (operators != null && (operator = operators.get(keyValue)) != null) {
				title = operator.getName();
			}
			break;
		case CITY:
			Map<Integer, com.dianping.cat.configuration.web.entity.Item> cities = m_webConfigManager
			      .queryConfigItem(WebConfigManager.CITY);
			com.dianping.cat.configuration.web.entity.Item city = null;
			keyValue = data.getCity();

			if (cities != null && (city = cities.get(keyValue)) != null) {
				title = city.getName();
			}
			break;
		case NETWORK:
			Map<Integer, com.dianping.cat.configuration.web.entity.Item> networks = m_webConfigManager
			      .queryConfigItem(WebConfigManager.NETWORK);
			com.dianping.cat.configuration.web.entity.Item network = null;
			keyValue = data.getNetwork();

			if (networks != null && (network = networks.get(keyValue)) != null) {
				title = network.getName();
			}
			break;
		case CODE:
			Map<Integer, Code> codes = m_patternManager.queryCodes();
			Code code = null;
			keyValue = data.getCode();

			if (codes != null && (code = codes.get(keyValue)) != null) {
				title = code.getName();
				int status = code.getStatus();
				if (status == 0) {
					title = "<span class='text-success'>【成功】</span>" + title;
				} else {
					title = "<span class='text-error'>【失败】</span>" + title;
				}
			}
			break;
		}
		if ("Unknown".equals(title)) {
			title += " [ " + keyValue + " ]";
		}
		return new Pair<Integer, String>(keyValue, title);
	}

	private Item buildPieChartItem(int command, AjaxData data, AjaxDataField field) {
		Pair<Integer, String> pair = buildPieChartFieldTitlePair(command, data, field);
		Item item = new Item();

		item.setTitle(pair.getValue());
		item.setId(pair.getKey());
		item.setNumber(data.getAccessNumberSum());

		return item;
	}

	private AjaxPieChartDetailInfos buildPieChartDetailInfo(List<Item> items) {
		AjaxPieChartDetailInfos infos = new AjaxPieChartDetailInfos();
		double sum = 0;

		for (Item item : items) {
			sum += item.getNumber();
		}

		if (sum > 0) {
			for (Item item : items) {
				PieChartDetailInfo info = new PieChartDetailInfo();

				info.setId(item.getId()).setTitle(item.getTitle()).setRequestSum(item.getNumber());
				info.setSuccessRatio(item.getNumber() / sum);

				infos.addPieChartDetailInfo(info);
			}
		}
		return infos;
	}
}
