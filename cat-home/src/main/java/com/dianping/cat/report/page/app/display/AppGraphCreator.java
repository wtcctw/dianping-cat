package com.dianping.cat.report.page.app.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.report.graph.DistributeDetailInfo.DistributeDetail;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.graph.DistributeDetailInfo;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.service.AppDataField;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;

public class AppGraphCreator {

	@Inject
	private AppDataService m_AppDataService;

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
			Double[] data = m_AppDataService.queryGraphValue(queryEntity1, type);

			datas.put(Constants.CURRENT_STR, data);
		}

		if (queryEntity2 != null) {
			Double[] data = m_AppDataService.queryGraphValue(queryEntity2, type);

			datas.put(Constants.COMPARISION_STR, data);
		}
		return buildChartData(datas, type);
	}

	public AppCommandDisplayInfo buildCommandDistributeChart(CommandQueryEntity entity, AppDataField field) {
		DistributeDetailInfo detailInfos = buildCommandDistributeDetails(entity, field);
		AppCommandDisplayInfo displayInfo = new AppCommandDisplayInfo();

		displayInfo.setDistributeDetails(detailInfos);
		displayInfo.setPieChart(buildPieChart(detailInfos));
		displayInfo.setBarChart(buildBarChart(detailInfos, field));
		return displayInfo;
	}

	private BarChart buildBarChart(DistributeDetailInfo detailInfos, AppDataField field) {
		BarChart barChart = new BarChart();
		barChart.setTitle("加载时间分布");
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName(field.getName());
		List<DistributeDetail> datas = detailInfos.getItems();

		Collections.sort(datas, new Comparator<DistributeDetail>() {
			@Override
			public int compare(DistributeDetail o1, DistributeDetail o2) {
				return (int) (o2.getDelayAvg() - o1.getDelayAvg());
			}
		});

		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (DistributeDetail data : datas) {
			if (field == AppDataField.CODE || field == AppDataField.APP_VERSION) {
				itemList.add(String.valueOf(data.getId()));
			} else {
				itemList.add(data.getTitle());
			}
			dataList.add(data.getDelayAvg());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
		return barChart;
	}

	private PieChart buildPieChart(DistributeDetailInfo detailInfos) {
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();

		for (DistributeDetail detail : detailInfos.getItems()) {
			Item item = new Item();

			item.setTitle(detail.getTitle());
			item.setId(detail.getId());
			item.setNumber(detail.getRequestSum());
			items.add(item);
		}

		pieChart.setTitle("请求量分布");
		pieChart.addItems(items);
		return pieChart;
	}

	public DistributeDetailInfo buildCommandDistributeDetails(CommandQueryEntity entity, AppDataField field) {
		List<AppCommandData> datas = m_AppDataService.queryByField(entity, field);

		DistributeDetailInfo detailInfos = new DistributeDetailInfo();

		for (AppCommandData data : datas) {
			DistributeDetail info = new DistributeDetail();

			Pair<Integer, String> pair = buildPieChartFieldTitlePair(entity.getId(), data, field);
			info.setId(pair.getKey()).setTitle(pair.getValue());

			long requestSum = data.getAccessNumberSum();
			info.setRequestSum(requestSum);

			if (requestSum > 0) {
				info.setDelayAvg(data.getResponseSumTimeSum() / requestSum);
			}

			detailInfos.add(info);
		}

		double sum = 0;

		for (DistributeDetail detail : detailInfos.getItems()) {
			sum += detail.getRequestSum();
		}

		if (sum > 0) {
			for (DistributeDetail detail : detailInfos.getItems()) {
				detail.setRatio(detail.getRequestSum() / sum);
			}
		}
		return detailInfos;
	}

	public Pair<Integer, String> buildPieChartFieldTitlePair(int command, AppCommandData data, AppDataField field) {
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

}
