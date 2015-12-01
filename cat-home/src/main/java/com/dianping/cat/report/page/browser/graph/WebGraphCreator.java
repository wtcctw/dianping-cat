package com.dianping.cat.report.page.browser.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.Code;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.browser.display.PieChartDetailInfos;
import com.dianping.cat.report.page.browser.display.PieChartDetailInfos.PieChartDetailInfo;
import com.dianping.cat.report.page.browser.service.AjaxDataField;
import com.dianping.cat.report.page.browser.service.AjaxDataQueryEntity;
import com.dianping.cat.report.page.browser.service.AjaxDataService;
import com.dianping.cat.report.page.browser.service.QueryType;
import com.dianping.cat.web.AjaxData;

public class WebGraphCreator {

	@Inject
	private AjaxDataService m_WebApiService;

	@Inject
	private WebConfigManager m_webConfigManager;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	public LineChart buildChartData(final List<Double[]> datas, QueryType type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle(type.getTitle());

		if (QueryType.SUCCESS.getType().equals(type)) {
			lineChart.setMinYlable(lineChart.queryMinYlable(datas));
			lineChart.setMaxYlabel(100D);
		}

		for (int i = 0; i < datas.size(); i++) {
			Double[] data = datas.get(i);

			if (i == 0) {
				lineChart.add("当前值", data);
			} else if (i == 1) {
				lineChart.add("对比值", data);
			}
		}
		return lineChart;
	}

	public LineChart buildLineChart(AjaxDataQueryEntity queryEntity1, AjaxDataQueryEntity queryEntity2, QueryType type) {
		List<Double[]> datas = new LinkedList<Double[]>();

		if (queryEntity1 != null) {
			Double[] data1 = m_WebApiService.queryValue(queryEntity1, type);

			datas.add(data1);
		}

		if (queryEntity2 != null) {
			Double[] values2 = m_WebApiService.queryValue(queryEntity2, type);
			datas.add(values2);
		}
		return buildChartData(datas, type);
	}

	public Pair<PieChart, PieChartDetailInfos> buildPieChart(AjaxDataQueryEntity entity, AjaxDataField field) {
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();
		List<AjaxData> datas = m_WebApiService.queryByField(entity, field);

		for (AjaxData data : datas) {
			items.add(buildPieChartItem(entity.getId(), data, field));
		}
		pieChart.setTitle(field.getName() + "访问情况");
		pieChart.addItems(items);

		PieChartDetailInfos infos = buildPieChartDetailInfo(items);

		return new Pair<PieChart, PieChartDetailInfos>(pieChart, infos);
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

	private PieChartDetailInfos buildPieChartDetailInfo(List<Item> items) {
		PieChartDetailInfos infos = new PieChartDetailInfos();
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
