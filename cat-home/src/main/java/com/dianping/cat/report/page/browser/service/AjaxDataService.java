package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.report.page.app.display.AppDataSequence;
import com.dianping.cat.web.AjaxData;
import com.dianping.cat.web.AjaxDataDao;
import com.dianping.cat.web.AjaxDataEntity;

public class AjaxDataService {

	@Inject
	private AjaxDataDao m_dao;

	@Inject
	private UrlPatternConfigManager m_urlConfigManager;

	public static final String SUCCESS = "success";

	public static final String REQUEST = "request";

	public static final String DELAY = "delay";

	public static final String REQUEST_PACKAGE = "requestByte";

	public static final String RESPONSE_PACKAGE = "responseByte";

	private AppDataSequence<AjaxData> buildAppSequence(List<AjaxData> fromDatas, Date period) {
		Map<Integer, List<AjaxData>> dataMap = new LinkedHashMap<Integer, List<AjaxData>>();
		int max = -5;

		for (AjaxData from : fromDatas) {
			int minute = from.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}
			List<AjaxData> data = dataMap.get(minute);

			if (data == null) {
				data = new LinkedList<AjaxData>();

				dataMap.put(minute, data);
			}
			data.add(from);
		}
		int n = max / 5 + 1;
		int length = queryAppDataDuration(period, n);

		return new AppDataSequence<AjaxData>(length, dataMap);
	}

	public Double[] computeAvg(AppDataSequence<AjaxData> convertedData, String type) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AjaxData>> entry : convertedData.getRecords().entrySet()) {
			for (AjaxData data : entry.getValue()) {
				long count = data.getAccessNumberSum();
				long sum = buildSumData(data, type);
				double avg = sum / count;
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = avg;
				}
			}
		}
		return value;
	}

	private long buildSumData(AjaxData data, String type) {
		if (DELAY.equals(type)) {
			return data.getResponseSumTimeSum();
		} else if (REQUEST_PACKAGE.equals(type)) {
			return data.getRequestSumByteSum();
		} else if (RESPONSE_PACKAGE.equals(type)) {
			return data.getResponseSumByteSum();
		}
		throw new RuntimeException("unexpected query type, type:" + type);
	}

	public Double[] computeRequestCount(AppDataSequence<AjaxData> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<AjaxData>> entry : convertedData.getRecords().entrySet()) {
			for (AjaxData data : entry.getValue()) {
				double count = data.getAccessNumberSum();
				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = count;
				}
			}
		}
		return value;
	}

	public Double[] computeSuccessRatio(int commandId, AppDataSequence<AjaxData> convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (int i = 0; i < n; i++) {
			value[i] = 100.0;
		}

		try {
			for (Entry<Integer, List<AjaxData>> entry : convertedData.getRecords().entrySet()) {
				int key = entry.getKey();
				int index = key / 5;

				if (index < n) {
					value[index] = computeSuccessRatio(commandId, entry.getValue());
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return value;
	}

	private double computeSuccessRatio(int commandId, List<AjaxData> datas) {
		long success = 0;
		long sum = 0;

		for (AjaxData data : datas) {
			long number = data.getAccessNumberSum();

			if (m_urlConfigManager.isSuccessCode(data.getCode())) {
				success += number;
			}
			sum += number;
		}
		return sum == 0 ? 0 : (double) success / sum * 100;
	}

	private int queryAppDataDuration(Date period, int defaultValue) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if (period.equals(cal.getTime())) {
			long start = cal.getTimeInMillis();
			long current = System.currentTimeMillis();
			int length = (int) (current - current % 300000 - start) / 300000 - 1;

			return length < 0 ? 0 : length;
		}
		return defaultValue;
	}

	public List<AjaxData> queryByField(AjaxDataQueryEntity entity, AjaxDataField groupByField) {
		List<AjaxData> datas = new ArrayList<AjaxData>();
		int apiId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int code = entity.getCode();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();
		int network = entity.getNetwork();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperator(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_OPERATOR_DATA);
				break;
			case CITY:
				datas = m_dao.findDataByCity(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_CITY_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_CODE_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetwork(apiId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_NETWORK_DATA);
				break;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	public double queryOneDayDelayAvg(AjaxDataQueryEntity entity) {
		Double[] values = queryValue(entity, AjaxDataService.DELAY);
		double delaySum = 0;
		int size = 0;

		for (int i = 0; i < values.length; i++) {
			if (values[i] != null) {
				delaySum += values[i];
				size++;
			}
		}
		return size > 0 ? delaySum / size : -1;
	}

	public Double[] queryValue(AjaxDataQueryEntity entity, String type) {
		int apiId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int code = entity.getCode();
		int network = entity.getNetwork();
		List<AjaxData> datas = new ArrayList<AjaxData>();

		try {
			if (SUCCESS.equals(type)) {
				datas = m_dao.findDataByMinuteCode(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_SUCCESS_DATA);
				AppDataSequence<AjaxData> s = buildAppSequence(datas, entity.getDate());

				return computeSuccessRatio(apiId, s);
			} else if (REQUEST.equals(type)) {
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_COUNT_DATA);
				AppDataSequence<AjaxData> s = buildAppSequence(datas, entity.getDate());

				return computeRequestCount(s);
			} else if (DELAY.equals(type)) {
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_AVG_DATA);
				AppDataSequence<AjaxData> s = buildAppSequence(datas, entity.getDate());

				return computeAvg(s, type);
			} else if (REQUEST_PACKAGE.equals(type)) {
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_REQUEST_BYTE_AVG_DATA);
				AppDataSequence<AjaxData> s = buildAppSequence(datas, entity.getDate());

				return computeAvg(s, type);
			} else if (RESPONSE_PACKAGE.equals(type)) {
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_RESPONSE_BYTE_AVG_DATA);
				AppDataSequence<AjaxData> s = buildAppSequence(datas, entity.getDate());

				return computeAvg(s, type);
			} else {
				throw new RuntimeException("unexpected query type, type:" + type);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

}
