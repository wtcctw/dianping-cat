package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.report.page.DataSequence;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;
import com.dianping.cat.report.page.browser.display.AjaxDataDetail;
import com.dianping.cat.web.AjaxData;
import com.dianping.cat.web.AjaxDataDao;
import com.dianping.cat.web.AjaxDataEntity;

public class AjaxDataService {

	@Inject
	private AjaxDataDao m_dao;

	@Inject
	private UrlPatternConfigManager m_urlConfigManager;

	public List<AjaxDataDetail> buildAjaxDataDetailInfos(AjaxDataQueryEntity entity, AjaxDataField groupByField) {
		List<AjaxDataDetail> infos = new LinkedList<AjaxDataDetail>();
		List<AjaxData> datas = queryByFieldCode(entity, groupByField);
		Map<Integer, List<AjaxData>> field2Datas = buildFields2Datas(datas, groupByField);

		for (Entry<Integer, List<AjaxData>> entry : field2Datas.entrySet()) {
			List<AjaxData> datalst = entry.getValue();
			AjaxDataDetail info = new AjaxDataDetail();
			double ratio = computeSuccessRatio(entity.getId(), datalst);

			info.setSuccessRatio(ratio);
			updateAjaxDataDetailInfo(info, entry, groupByField, entity);
			infos.add(info);
		}
		return infos;
	}

	private DataSequence<AjaxData> buildAppSequence(List<AjaxData> fromDatas, Date period) {
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
		int length = queryAjaxDataDuration(period, n);

		return new DataSequence<AjaxData>(length, dataMap);
	}

	private Map<Integer, List<AjaxData>> buildFields2Datas(List<AjaxData> datas, AjaxDataField field) {
		Map<Integer, List<AjaxData>> field2Datas = new HashMap<Integer, List<AjaxData>>();

		for (AjaxData data : datas) {
			int fieldValue = queryFieldValue(data, field);
			List<AjaxData> lst = field2Datas.get(fieldValue);

			if (lst == null) {
				lst = new ArrayList<AjaxData>();
				field2Datas.put(fieldValue, lst);
			}
			lst.add(data);
		}
		return field2Datas;
	}

	private long buildSumData(AjaxData data, QueryType type) {
		switch (type) {
		case DELAY:
			return data.getResponseSumTimeSum();
		case REQUEST_PACKAGE:
			return data.getRequestSumByteSum();
		case RESPONSE_PACKAGE:
			return data.getResponseSumByteSum();
		default:
			throw new RuntimeException("unexpected query type, type:" + type);
		}
	}

	public Double[] computeAvg(DataSequence<AjaxData> convertedData, QueryType type) {
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

	public Double[] computeRequestCount(DataSequence<AjaxData> convertedData) {
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

	public Double[] computeSuccessRatio(int commandId, DataSequence<AjaxData> convertedData) {
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

	private int queryAjaxDataDuration(Date period, int defaultValue) {
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

	private List<AjaxData> queryByFieldCode(AjaxDataQueryEntity entity, AjaxDataField groupByField) {
		List<AjaxData> datas = new ArrayList<AjaxData>();
		int commandId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int network = entity.getNetwork();
		int code = entity.getCode();
		int startMinuteOrder = entity.getStartMinuteOrder();
		int endMinuteOrder = entity.getEndMinuteOrder();

		try {
			switch (groupByField) {
			case OPERATOR:
				datas = m_dao.findDataByOperatorCode(commandId, period, city, operator, network, code,
				      AjaxDataEntity.READSET_OPERATOR_CODE_DATA);
				break;
			case NETWORK:
				datas = m_dao.findDataByNetworkCode(commandId, period, city, operator, network, code,
				      AjaxDataEntity.READSET_NETWORK_CODE_DATA);
				break;
			case CITY:
				datas = m_dao.findDataByCityCode(commandId, period, city, operator, network, code,
				      AjaxDataEntity.READSET_CITY_CODE_DATA);
				break;
			case CODE:
				datas = m_dao.findDataByCode(commandId, period, city, operator, code, network, startMinuteOrder,
				      endMinuteOrder, AjaxDataEntity.READSET_CODE_DATA);
				break;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return datas;
	}

	private int queryFieldValue(AjaxData data, AjaxDataField field) {
		switch (field) {
		case OPERATOR:
			return data.getOperator();
		case CITY:
			return data.getCity();
		case NETWORK:
			return data.getNetwork();
		case CODE:
		default:
			return CommandQueryEntity.DEFAULT_VALUE;
		}
	}

	public double queryOneDayDelayAvg(AjaxDataQueryEntity entity) {
		Double[] values = queryValue(entity, QueryType.DELAY);
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

	public Double[] queryValue(AjaxDataQueryEntity entity, QueryType type) {
		int apiId = entity.getId();
		Date period = entity.getDate();
		int city = entity.getCity();
		int operator = entity.getOperator();
		int code = entity.getCode();
		int network = entity.getNetwork();
		List<AjaxData> datas = new ArrayList<AjaxData>();
		DataSequence<AjaxData> s = null;
		try {
			switch (type) {
			case SUCCESS:
				datas = m_dao.findDataByMinuteCode(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_SUCCESS_DATA);
				s = buildAppSequence(datas, entity.getDate());

				return computeSuccessRatio(apiId, s);
			case REQUEST:
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_COUNT_DATA);
				s = buildAppSequence(datas, entity.getDate());

				return computeRequestCount(s);
			case DELAY:
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_AVG_DATA);
				s = buildAppSequence(datas, entity.getDate());

				return computeAvg(s, type);
			case REQUEST_PACKAGE:
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_REQUEST_BYTE_AVG_DATA);
				s = buildAppSequence(datas, entity.getDate());

				return computeAvg(s, type);
			case RESPONSE_PACKAGE:
				datas = m_dao.findDataByMinute(apiId, period, city, operator, code, network,
				      AjaxDataEntity.READSET_RESPONSE_BYTE_AVG_DATA);
				s = buildAppSequence(datas, entity.getDate());

				return computeAvg(s, type);

			}

		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	private void setFieldValue(AjaxDataDetail info, AjaxDataField field, int value) {
		switch (field) {
		case OPERATOR:
			info.setOperator(value);
			break;
		case CITY:
			info.setCity(value);
			break;
		case NETWORK:
			info.setNetwork(value);
			break;
		case CODE:
			break;
		}
	}

	private void updateAjaxDataDetailInfo(AjaxDataDetail info, Entry<Integer, List<AjaxData>> entry,
	      AjaxDataField field, AjaxDataQueryEntity entity) {
		int key = entry.getKey();
		List<AjaxData> datas = entry.getValue();
		long accessNumberSum = 0;
		long responseTimeSum = 0;
		long responsePackageSum = 0;
		long requestPackageSum = 0;

		for (AjaxData data : datas) {
			accessNumberSum += data.getAccessNumberSum();
			responseTimeSum += data.getResponseSumTimeSum();
			responsePackageSum += data.getResponseSumByteSum();
			requestPackageSum += data.getRequestSumByteSum();
		}
		double responseTimeAvg = accessNumberSum == 0 ? 0 : (double) responseTimeSum / accessNumberSum;
		double responsePackageAvg = accessNumberSum == 0 ? 0 : (double) responsePackageSum / accessNumberSum;
		double requestPackageAvg = accessNumberSum == 0 ? 0 : (double) requestPackageSum / accessNumberSum;

		info.setAccessNumberSum(accessNumberSum).setResponseTimeAvg(responseTimeAvg)
		      .setRequestPackageAvg(requestPackageAvg).setResponsePackageAvg(responsePackageAvg)
		      .setOperator(entity.getOperator()).setCity(entity.getCity()).setNetwork(entity.getNetwork());

		setFieldValue(info, field, key);
	}

}
