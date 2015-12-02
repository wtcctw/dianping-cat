package com.dianping.cat.report.page.app.display;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.app.AppConnectionData;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.graph.PieChartDetailInfo;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.service.AppConnectionService;
import com.dianping.cat.report.page.app.service.AppDataField;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;

public class AppConnectionGraphCreator {

	@Inject
	private AppConnectionService m_AppConnectionService;

	@Inject
	private AppConfigManager m_appConfigManager;

	public LineChart buildChartData(final Map<String, Double[]> datas, QueryType type) {
		LineChart lineChart = new LineChart();
		lineChart.setId("app");
		lineChart.setUnit("");
		lineChart.setHtmlTitle(type.getTitle());

		if (QueryType.SUCCESS.equals(type)) {
			lineChart.setMinYlable(lineChart.queryMinYlable(datas));
			lineChart.setMaxYlabel(100D);
		}

		for (Entry<String, Double[]> entry : datas.entrySet()) {
			Double[] data = entry.getValue();

			lineChart.add(entry.getKey(), data);
		}
		return lineChart;
	}

	public LineChart buildLineChart(CommandQueryEntity queryEntity1, CommandQueryEntity queryEntity2, QueryType type) {
		Map<String, Double[]> datas = new LinkedHashMap<String, Double[]>();

		if (queryEntity1 != null) {
			Double[] data = m_AppConnectionService.queryValue(queryEntity1, type);

			datas.put(Constants.CURRENT_STR, data);
		}

		if (queryEntity2 != null) {
			Double[] data = m_AppConnectionService.queryValue(queryEntity2, type);

			datas.put(Constants.COMPARISION_STR, data);
		}
		return buildChartData(datas, type);
	}

	public Pair<PieChart, PieChartDetailInfo> buildPieChart(CommandQueryEntity entity, AppDataField field) {
		PieChartDetailInfo info = new PieChartDetailInfo();
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();
		List<AppConnectionData> datas = m_AppConnectionService.queryByField(entity, field);

		for (AppConnectionData data : datas) {
			Pair<Integer, Item> pair = buildPieChartItem(entity.getId(), data, field);
			Item item = pair.getValue();
			com.dianping.cat.report.graph.PieChartDetailInfo.Item infoItem = new com.dianping.cat.report.graph.PieChartDetailInfo.Item();

			infoItem.setId(pair.getKey()).setTitle(item.getTitle()).setRequestSum(item.getNumber());
			info.add(infoItem);
			items.add(item);
		}
		pieChart.setTitle(field.getName() + "访问情况");
		pieChart.addItems(items);
		updatePieChartDetailInfo(info);

		return new Pair<PieChart, PieChartDetailInfo>(pieChart, info);
	}

	private Pair<Integer, String> buildPieChartFieldTitlePair(int command, AppConnectionData data, AppDataField field) {
		String title = "Unknown";
		int keyValue = -1;

		switch (field) {
		case OPERATOR:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> operators = m_appConfigManager
			      .queryConfigItem(AppConfigManager.OPERATOR);
			com.dianping.cat.configuration.app.entity.Item operator = null;
			keyValue = data.getOperator();

			if (operators != null && (operator = operators.get(keyValue)) != null) {
				title = operator.getName();
			}
			break;
		case APP_VERSION:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> appVersions = m_appConfigManager
			      .queryConfigItem(AppConfigManager.VERSION);
			com.dianping.cat.configuration.app.entity.Item appVersion = null;
			keyValue = data.getAppVersion();

			if (appVersions != null && (appVersion = appVersions.get(keyValue)) != null) {
				title = appVersion.getName();
			}
			break;
		case CITY:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> cities = m_appConfigManager
			      .queryConfigItem(AppConfigManager.CITY);
			com.dianping.cat.configuration.app.entity.Item city = null;
			keyValue = data.getCity();

			if (cities != null && (city = cities.get(keyValue)) != null) {
				title = city.getName();
			}
			break;
		case CONNECT_TYPE:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> connectTypes = m_appConfigManager
			      .queryConfigItem(AppConfigManager.CONNECT_TYPE);
			com.dianping.cat.configuration.app.entity.Item connectType = null;
			keyValue = data.getConnectType();

			if (connectTypes != null && (connectType = connectTypes.get(keyValue)) != null) {
				title = connectType.getName();
			}
			break;
		case NETWORK:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> networks = m_appConfigManager
			      .queryConfigItem(AppConfigManager.NETWORK);
			com.dianping.cat.configuration.app.entity.Item network = null;
			keyValue = data.getNetwork();

			if (networks != null && (network = networks.get(keyValue)) != null) {
				title = network.getName();
			}
			break;
		case PLATFORM:
			Map<Integer, com.dianping.cat.configuration.app.entity.Item> platforms = m_appConfigManager
			      .queryConfigItem(AppConfigManager.PLATFORM);
			com.dianping.cat.configuration.app.entity.Item platform = null;
			keyValue = data.getPlatform();

			if (platforms != null && (platform = platforms.get(keyValue)) != null) {
				title = platform.getName();
			}
			break;
		case CODE:
			Map<Integer, Code> codes = m_appConfigManager.queryCodeByCommand(command);
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
		default:
			throw new RuntimeException("Unrecognized groupby field: " + field);
		}
		if ("Unknown".equals(title)) {
			title += " [ " + keyValue + " ]";
		}
		return new Pair<Integer, String>(keyValue, title);
	}

	private Pair<Integer, Item> buildPieChartItem(int command, AppConnectionData data, AppDataField field) {
		Item item = new Item();
		Pair<Integer, String> pair = buildPieChartFieldTitlePair(command, data, field);

		item.setTitle(pair.getValue());
		item.setId(pair.getKey());
		item.setNumber(data.getAccessNumberSum());
		return new Pair<Integer, Item>(pair.getKey(), item);
	}

	private void updatePieChartDetailInfo(PieChartDetailInfo items) {
		double sum = 0;

		for (com.dianping.cat.report.graph.PieChartDetailInfo.Item item : items.getItems()) {
			sum += item.getRequestSum();
		}

		if (sum > 0) {
			for (com.dianping.cat.report.graph.PieChartDetailInfo.Item item : items.getItems()) {
				item.setSuccessRatio(item.getRequestSum() / sum);
			}
		}
	}
}
