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
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.web.WebSpeedData;
import com.dianping.cat.web.WebSpeedDataDao;
import com.dianping.cat.web.WebSpeedDataEntity;

public class WebSpeedDataManager {

	@Inject
	private WebSpeedDataDao m_dao;
	
	@Inject 
	WebSpeedConfigManager m_config;

	@SuppressWarnings("unchecked")
	public List<WebSpeedData> queryValueByTime(SpeedQueryEntity entity) {
		int pageId = m_config.querySpeedId(entity.getPageId());
		
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
	public List<WebSpeedData> queryValueByCity(SpeedQueryEntity entity) {
		int pageId = m_config.querySpeedId(entity.getPageId());
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
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_CITY_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByCity(pageId, period, city, operator, network, platform, source, readset);

				enrichData(datas, stepId);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return datas;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedData> queryValueByPlatform(SpeedQueryEntity entity) {
		int pageId = m_config.querySpeedId(entity.getPageId());
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
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_PLATFORM_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByPlatform(pageId, period, city, operator, network, platform, source, readset);

				enrichData(datas, stepId);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return datas;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedData> queryValueByOperator(SpeedQueryEntity entity) {
		int pageId = m_config.querySpeedId(entity.getPageId());
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
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_OPERATOR_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByOperator(pageId, period, city, operator, network, platform, source, readset);

				enrichData(datas, stepId);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return datas;
	}

	@SuppressWarnings("unchecked")
	public List<WebSpeedData> queryValueBySource(SpeedQueryEntity entity) {
		int pageId = m_config.querySpeedId(entity.getPageId());
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
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_SOURCE_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataBySource(pageId, period, city, operator, network, platform, source, readset);

				enrichData(datas, stepId);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return datas;
	}
	
	@SuppressWarnings("unchecked")
	public List<WebSpeedData> queryValueByNetwork(SpeedQueryEntity entity) {
		int pageId = m_config.querySpeedId(entity.getPageId());
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
				Field field = webSpeedDataEntity.getClass().getDeclaredField("READSET_NETWORK_DATA" + stepId);
				Readset<WebSpeedData> readset = (Readset<WebSpeedData>) field.get(webSpeedDataEntity);
				datas = m_dao.findDataByNetwork(pageId, period, city, operator, network, platform, source, readset);

				enrichData(datas, stepId);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return datas;
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
