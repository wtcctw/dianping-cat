package com.dianping.cat.report.page.browser.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.Readset;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.configuration.web.entity.Item;
import com.dianping.cat.report.page.browser.display.WebSpeedDetail;
import com.dianping.cat.web.WebSpeedData;
import com.dianping.cat.web.WebSpeedDataDao;
import com.dianping.cat.web.WebSpeedDataEntity;

public class WebSpeedDataManager {

	@Inject
	private WebSpeedDataDao m_dao;

	@Inject
	WebSpeedConfigManager m_speedConfig;

	@Inject
	private WebConfigManager m_webConfig;
	
	private WebSpeedDetail buildWebSpeedDetail(WebSpeedData data) {
	   WebSpeedDetail detail = new WebSpeedDetail();
	   double avg = 0.0;
	   long accessNumberSum = data.getAccessNumberSum();
	   
	   if (accessNumberSum > 0) {
	   	avg = data.getResponseSumTimeSum() / accessNumberSum;
	   }
	   detail.setAccessNumberSum(accessNumberSum);
	   detail.setResponseTimeAvg(avg);
	   
	   return detail;
   }

	@SuppressWarnings("unchecked")
	public List<WebSpeedData> queryValueByTime(SpeedQueryEntity entity) {
		int pageId = m_speedConfig.querySpeedId(entity.getPageId());

		int stepId = entity.getStepId();
		List<WebSpeedData> datas = new ArrayList<WebSpeedData>();

		if (pageId >= 0 && stepId > 0) {
			Date period = entity.getDate();
			int city = entity.getCity();
			int operator = entity.getOperator();
			int network = entity.getNetwork();
			int platform = entity.getPlatform();
			int source = entity.getSource();

			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_AVG_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByMinute(pageId, period, city, operator, network, platform, source, readset);

				enrichData(datas, stepId);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return datas;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedDetail> queryValueByCity(SpeedQueryEntity entity) {
		List<WebSpeedDetail> details = new ArrayList<WebSpeedDetail>();
		int pageId = m_speedConfig.querySpeedId(entity.getPageId());
		int stepId = entity.getStepId();

		if (pageId >= 0 && stepId > 0) {
			Date period = entity.getDate();
			int city = entity.getCity();
			int operator = entity.getOperator();
			int network = entity.getNetwork();
			int platform = entity.getPlatform();
			int source = entity.getSource();
			int start = entity.getStartMinuteOrder();
			int end = entity.getEndMinuteOrder();

			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_CITY_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				List<WebSpeedData> datas = m_dao.findDataByCity(pageId, period, city, operator, network, platform, source,
				      start, end, readset);

				enrichData(datas, stepId);

				for (WebSpeedData data : datas) {
					WebSpeedDetail detail = buildWebSpeedDetail(data);

					Item item = m_webConfig.queryItem(WebConfigManager.CITY, data.getCity());
					detail.setItemName(item.getName());
					
					details.add(detail);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return details;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedDetail> queryValueByPlatform(SpeedQueryEntity entity) {
		List<WebSpeedDetail> details = new ArrayList<WebSpeedDetail>();

		int pageId = m_speedConfig.querySpeedId(entity.getPageId());
		int stepId = entity.getStepId();
		List<WebSpeedData> datas = new ArrayList<WebSpeedData>();

		if (pageId >= 0 && stepId > 0) {
			Date period = entity.getDate();
			int city = entity.getCity();
			int operator = entity.getOperator();
			int network = entity.getNetwork();
			int platform = entity.getPlatform();
			int source = entity.getSource();
			int start = entity.getStartMinuteOrder();
			int end = entity.getEndMinuteOrder();

			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_PLATFORM_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByPlatform(pageId, period, city, operator, network, platform, source, start, end,
				      readset);

				enrichData(datas, stepId);
				
				for (WebSpeedData data : datas) {
					WebSpeedDetail detail = buildWebSpeedDetail(data);

					Item item = m_webConfig.queryItem(WebConfigManager.PLATFORM, data.getPlatform());
					detail.setItemName(item.getName());
					
					details.add(detail);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return details;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedDetail> queryValueByOperator(SpeedQueryEntity entity) {
		List<WebSpeedDetail> details = new ArrayList<WebSpeedDetail>();
		int pageId = m_speedConfig.querySpeedId(entity.getPageId());
		int stepId = entity.getStepId();
		List<WebSpeedData> datas = new ArrayList<WebSpeedData>();

		if (pageId >= 0 && stepId > 0) {
			Date period = entity.getDate();
			int city = entity.getCity();
			int operator = entity.getOperator();
			int network = entity.getNetwork();
			int platform = entity.getPlatform();
			int source = entity.getSource();
			int start = entity.getStartMinuteOrder();
			int end = entity.getEndMinuteOrder();

			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_OPERATOR_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByOperator(pageId, period, city, operator, network, platform, source, start, end,
				      readset);

				enrichData(datas, stepId);
				
				for (WebSpeedData data : datas) {
					WebSpeedDetail detail = buildWebSpeedDetail(data);

					Item item = m_webConfig.queryItem(WebConfigManager.OPERATOR, data.getOperator());
					detail.setItemName(item.getName());
					
					details.add(detail);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return details;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedDetail> queryValueBySource(SpeedQueryEntity entity) {
		List<WebSpeedDetail> details = new ArrayList<WebSpeedDetail>();
		int pageId = m_speedConfig.querySpeedId(entity.getPageId());
		int stepId = entity.getStepId();
		List<WebSpeedData> datas = new ArrayList<WebSpeedData>();

		if (pageId >= 0 && stepId > 0) {
			Date period = entity.getDate();
			int city = entity.getCity();
			int operator = entity.getOperator();
			int network = entity.getNetwork();
			int platform = entity.getPlatform();
			int source = entity.getSource();
			int start = entity.getStartMinuteOrder();
			int end = entity.getEndMinuteOrder();

			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_SOURCE_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataBySource(pageId, period, city, operator, network, platform, source, start, end,
				      readset);

				enrichData(datas, stepId);
				
				for (WebSpeedData data : datas) {
					WebSpeedDetail detail = buildWebSpeedDetail(data);

					Item item = m_webConfig.queryItem(WebConfigManager.SOURCE, data.getSource());
					detail.setItemName(item.getName());
					
					details.add(detail);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return details;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedDetail> queryValueByNetwork(SpeedQueryEntity entity) {
		List<WebSpeedDetail> details = new ArrayList<WebSpeedDetail>();
		int pageId = m_speedConfig.querySpeedId(entity.getPageId());
		int stepId = entity.getStepId();
		List<WebSpeedData> datas = new ArrayList<WebSpeedData>();

		if (pageId >= 0 && stepId > 0) {
			Date period = entity.getDate();
			int city = entity.getCity();
			int operator = entity.getOperator();
			int network = entity.getNetwork();
			int platform = entity.getPlatform();
			int source = entity.getSource();
			int start = entity.getStartMinuteOrder();
			int end = entity.getEndMinuteOrder();

			try {
				WebSpeedDataEntity webSpeedDataEntity = (WebSpeedDataEntity) Class.forName(
				      "com.dianping.cat.web.WebSpeedDataEntity").newInstance();
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_NETWORK_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByNetwork(pageId, period, city, operator, network, platform, source, start, end,
				      readset);

				enrichData(datas, stepId);
				
				for (WebSpeedData data : datas) {
					WebSpeedDetail detail = buildWebSpeedDetail(data);

					Item item = m_webConfig.queryItem(WebConfigManager.NETWORK, data.getNetwork());
					detail.setItemName(item.getName());
					
					details.add(detail);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return details;
	}

	private void enrichData(List<WebSpeedData> datas, int stepId) throws NoSuchMethodException, IllegalAccessException,
	      InvocationTargetException {
		for (WebSpeedData webSpeedData : datas) {
			Method getResponseSumTimeSum = webSpeedData.getClass().getMethod("getResponseSumTimeSum" + stepId);
			long responseSumTimeSum = (Long) getResponseSumTimeSum.invoke(webSpeedData);

			Method getAccessNumberSum = webSpeedData.getClass().getMethod("getAccessNumberSum" + stepId);
			long accessNumberSum = (Long) getAccessNumberSum.invoke(webSpeedData);

			webSpeedData.setAccessNumberSum(accessNumberSum);
			webSpeedData.setResponseSumTimeSum(responseSumTimeSum);
		}
	}
}
