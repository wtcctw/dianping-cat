package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppCommandDataDaily;
import com.dianping.cat.app.AppCommandDataDailyDao;
import com.dianping.cat.app.AppCommandDataDailyEntity;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.app.QueryType;

public class DailyReportService {

	@Inject
	private AppCommandDataDailyDao m_commandDailyDao;

	@Inject
	private AppConfigManager m_appConfigManager;

	public LineChart buildLineChart(DailyCommandQueryEntity entity, QueryType type) {
		Double[] graphData = buildGraphData(entity, type);

		LineChart lineChart = new LineChart();
		lineChart.setSize(graphData.length);
		lineChart.setStep(TimeHelper.ONE_DAY);
		lineChart.setStart(entity.getDate());
		lineChart.add(Constants.CURRENT_STR, graphData);

		if (QueryType.SUCCESS.equals(type)) {
			lineChart.setMaxYlabel(100D);
		}
		return lineChart;
	}

	private Double[] buildGraphData(DailyCommandQueryEntity entity, QueryType type) {
		List<AppCommandDataDaily> datas = queryData(entity, type);
		int size = (int) ((entity.getEndDate().getTime() - entity.getDate().getTime()) / TimeHelper.ONE_DAY + 1);
		Double[] graphData = new Double[size];

		switch (type) {
		case SUCCESS:
			Map<Long, List<AppCommandDataDaily>> dataMap = buildDataMap(datas);

			for (Entry<Long, List<AppCommandDataDaily>> entry : dataMap.entrySet()) {
				int index = (int) ((entry.getKey() - entity.getDate().getTime()) / TimeHelper.ONE_DAY);
				graphData[index] = computeSuccessRatio(entity.getId(), entry.getValue());
			}
			break;
		case REQUEST:
			for (AppCommandDataDaily data : datas) {
				int index = (int) ((data.getPeriod().getTime() - entity.getDate().getTime()) / TimeHelper.ONE_DAY);
				graphData[index] = (double) data.getAccessNumberSum();
			}
			break;
		case DELAY:
			for (AppCommandDataDaily data : datas) {
				int index = (int) ((data.getPeriod().getTime() - entity.getDate().getTime()) / TimeHelper.ONE_DAY);
				long accessSumNum = data.getAccessNumberSum();

				if (accessSumNum > 0) {
					graphData[index] = (double) (data.getResponseSumTimeSum() / accessSumNum);
				} else {
					graphData[index] = 0.0;
				}
			}
			break;
		default:
			throw new RuntimeException("unexpected query type, type:" + type);
		}
		return graphData;
	}

	private double computeSuccessRatio(int commandId, List<AppCommandDataDaily> datas) {
		long success = 0;
		long sum = 0;

		for (AppCommandDataDaily data : datas) {
			long number = data.getAccessNumberSum();

			if (m_appConfigManager.isSuccessCode(commandId, data.getCode())) {
				success += number;
			}
			sum += number;
		}
		return sum == 0 ? 0 : (double) success / sum * 100;
	}

	private Map<Long, List<AppCommandDataDaily>> buildDataMap(List<AppCommandDataDaily> datas) {
		Map<Long, List<AppCommandDataDaily>> dataMap = new LinkedHashMap<Long, List<AppCommandDataDaily>>();

		for (AppCommandDataDaily data : datas) {
			Long minute = data.getPeriod().getTime();
			List<AppCommandDataDaily> list = dataMap.get(minute);

			if (list == null) {
				list = new LinkedList<AppCommandDataDaily>();

				dataMap.put(minute, list);
			}
			list.add(data);
		}
		return dataMap;
	}

	private List<AppCommandDataDaily> queryData(DailyCommandQueryEntity entity, QueryType type) {
		int commandId = entity.getId();
		Date start = entity.getDate();
		Date end = entity.getEndDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int appVersion = entity.getVersion();
		int connnectType = entity.getConnectType();
		int code = entity.getCode();
		int platform = entity.getPlatfrom();
		List<AppCommandDataDaily> datas = new ArrayList<AppCommandDataDaily>();

		try {
			switch (type) {
			case SUCCESS:
				datas = m_commandDailyDao.findDataByPeriodCode(commandId, start, end, city, operator, network, appVersion,
				      connnectType, code, platform, AppCommandDataDailyEntity.READSET_SUCCESS_DATA);
				break;
			case REQUEST:
				datas = m_commandDailyDao.findDataByPeriod(commandId, start, end, city, operator, network, appVersion,
				      connnectType, code, platform, AppCommandDataDailyEntity.READSET_COUNT_DATA);
				break;
			case DELAY:
				datas = m_commandDailyDao.findDataByPeriod(commandId, start, end, city, operator, network, appVersion,
				      connnectType, code, platform, AppCommandDataDailyEntity.READSET_AVG_DATA);
				break;
			default:
				throw new RuntimeException("unexpected query type, type:" + type);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

}
