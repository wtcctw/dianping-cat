package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.report.graph.MapChart;
import com.dianping.cat.report.graph.MapChart.Item;
import com.dianping.cat.report.page.app.display.AppCommandDisplayInfo;
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

	public DashBoardInfo buildDashBoard(CommandQueryEntity entity) {
		DashBoardInfo dashboard = new DashBoardInfo();
		dashboard.setMapChart(buildMapChart(entity));
		
		AppCommandDisplayInfo opeartor = m_appGraphCreator.buildCommandDistributeChart(entity, AppDataField.OPERATOR);
		dashboard.setOperatorChart(opeartor.getBarChart());
		
		return dashboard;
	}

	private MapChart buildMapChart(CommandQueryEntity entity) {
		List<AppCommandData> appCommandDatas = m_appDataService.queryByField(entity, AppDataField.CITY);
		List<Item> relayItems = new ArrayList<Item>();

		for (AppCommandData appCommandData : appCommandDatas) {
			String province = Area.CHINA_PROVINCE.get(appCommandData.getCity());

			if (appCommandData.getAccessNumberSum() > 0) {
				double relay = appCommandData.getResponseSumTimeSum() / appCommandData.getAccessNumberSum();
				relayItems.add(new Item(province, relay));
			}
		}

		return buildMapChart(relayItems, "平均加载时间(ms)", 0, 3000);
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
