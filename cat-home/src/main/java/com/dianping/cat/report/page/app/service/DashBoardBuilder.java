package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.MapChart;
import com.dianping.cat.report.graph.MapChart.Item;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppGraphCreator;
import com.dianping.cat.report.page.app.display.Area;
import com.dianping.cat.report.page.app.display.DashBoardInfo;

public class DashBoardBuilder {

	@Inject
	private JsonBuilder m_jsonBuilder;

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AppGraphCreator m_appGraphCreator;

	@Inject
	private AppConfigManager m_appConfigManager;

	public DashBoardInfo buildDashBoard(CommandQueryEntity entity) {
		DashBoardInfo dashboard = new DashBoardInfo();

		List<AppDataDetail> cities = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.CITY);
		dashboard.setMapChart(buildResponseMapChart(cities));
		dashboard.setSuccessMapChart(buildSuccessMapChart(cities));

		List<AppDataDetail> operators = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.OPERATOR);
		dashboard.setOperatorChart(buildResponseBarChart(operators, AppDataField.OPERATOR));
		dashboard.setOperatorSuccessChart(buildSuccessRatioBarChart(operators, AppDataField.OPERATOR));

		List<AppDataDetail> version = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.APP_VERSION);
		dashboard.setVersionChart(buildResponseBarChart(version, AppDataField.APP_VERSION));
		dashboard.setVersionSuccessChart(buildSuccessRatioBarChart(version, AppDataField.APP_VERSION));

		List<AppDataDetail> platform = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.PLATFORM);
		dashboard.setPlatformChart(buildResponseBarChart(platform, AppDataField.PLATFORM));
		dashboard.setPlatformSuccessChart(buildSuccessRatioBarChart(platform, AppDataField.PLATFORM));

		LineChart lineChart = m_appGraphCreator.buildLineChart(entity, null, QueryType.DELAY);
		dashboard.setLineChart(lineChart);

		return dashboard;
	}

	private BarChart buildSuccessRatioBarChart(List<AppDataDetail> datas, AppDataField field) {
		BarChart barChart = new BarChart();
		barChart.setyAxis("成功率");
		barChart.setSerieName(field.getName());
		Collections.sort(datas, new Comparator<AppDataDetail>() {
			@Override
			public int compare(AppDataDetail o1, AppDataDetail o2) {
				return (int) (o2.getSuccessRatio() - o1.getSuccessRatio());
			}
		});
		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (AppDataDetail data : datas) {
			itemList.add(queryItemName(data, field));
			dataList.add(data.getSuccessRatio());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
		return barChart;
	}

	private BarChart buildResponseBarChart(List<AppDataDetail> datas, AppDataField field) {
		BarChart barChart = new BarChart();
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName(field.getName());
		Collections.sort(datas, new Comparator<AppDataDetail>() {
			@Override
			public int compare(AppDataDetail o1, AppDataDetail o2) {
				return (int) (o2.getResponseTimeAvg() - o1.getResponseTimeAvg());
			}
		});
		List<String> itemList = new ArrayList<String>();
		List<Double> dataList = new ArrayList<Double>();

		for (AppDataDetail data : datas) {
			itemList.add(queryItemName(data, field));
			dataList.add(data.getResponseTimeAvg());
		}

		barChart.setxAxis(itemList);
		barChart.setValues(dataList);
		return barChart;
	}

	private String queryItemName(AppDataDetail data, AppDataField field) {
		String title = null;
		int value = 0;

		switch (field) {
		case OPERATOR:
			value = data.getOperator();
			break;
		case APP_VERSION:
			value = data.getAppVersion();
			break;
		case PLATFORM:
			value = data.getPlatform();
			break;
		default:
			throw new RuntimeException("Unsupported AppDataField.");
		}
		com.dianping.cat.configuration.app.entity.Item item = m_appConfigManager.queryItem(field.getTitle(), value);

		if (item != null) {
			title = item.getName();
		} else {
			title = String.valueOf(value);
		}
		return title;
	}

	private MapChart buildResponseMapChart(List<AppDataDetail> cities) {
		List<Item> relayItems = new ArrayList<Item>();

		for (AppDataDetail appDataDetail : cities) {
			String province = Area.CHINA_PROVINCE.get(appDataDetail.getCity());
			relayItems.add(new Item(province, appDataDetail.getResponseTimeAvg()));
		}
		return buildMapChart(relayItems, "", 0, 3000);
	}

	private MapChart buildSuccessMapChart(List<AppDataDetail> cities) {
		List<Item> relayItems = new ArrayList<Item>();

		for (AppDataDetail appDataDetail : cities) {
			String province = Area.CHINA_PROVINCE.get(appDataDetail.getCity());
			relayItems.add(new Item(province, appDataDetail.getSuccessRatio()));
		}
		return buildMapChart(relayItems, "", 98, 100);
	}

	private MapChart buildMapChart(List<Item> requestItems, String title, int min, int max) {
		MapChart mapChart = new MapChart();
		mapChart.setTitle(title);
		mapChart.setMax(max);
		mapChart.setMin(min);
		mapChart.setDataSeries(requestItems);
		mapChart.setData(m_jsonBuilder.toJson(requestItems));
		return mapChart;
	}
}
