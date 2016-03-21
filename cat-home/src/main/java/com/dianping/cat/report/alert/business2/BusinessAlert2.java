package com.dianping.cat.report.alert.business2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.spi.AlarmRule;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

public class BusinessAlert2 implements Task {

	@Inject
	private BusinessRuleConfigManager2 m_alertConfigManager;

	@Inject
	private BusinessConfigManager m_configManager;

	@Inject
	private BusinessTagConfigManager m_tagConfigManager;

	@Inject
	private BusinessReportGroupService m_service;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private AlertManager m_sendManager;

	@Inject
	private BusinessKeyHelper m_keyHelper;

	@Inject
	private BaselineService m_baselineService;

	@Inject
	private DataChecker m_dataChecker;

	@Inject
	protected BaseRuleHelper m_baseRuleHelper;

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	private static final int DATA_AREADY_MINUTE = 1;

	public static final String DEFAULT_TAG = "业务大盘";

	private AlarmRule buildMonitorConfigs(String domain, BusinessReportConfig businessReportConfig) {
		Map<String, Map<MetricType, List<Config>>> monitorConfigs = new HashMap<String, Map<MetricType, List<Config>>>();
		Map<String, BusinessItemConfig> itemConfigs = businessReportConfig.getBusinessItemConfigs();

		for (BusinessItemConfig config : itemConfigs.values()) {
			Map<MetricType, List<Config>> monitorConfigsByItem = new HashMap<MetricType, List<Config>>();

			if (needAlert(config, domain)) {
				String key = config.getId();

				if (config.isShowAvg()) {
					List<Config> tmpConfigs = m_alertConfigManager.queryConfigs(domain, key, MetricType.AVG);

					monitorConfigsByItem.put(MetricType.AVG, tmpConfigs);
				}
				if (config.isShowCount()) {
					List<Config> tmpConfigs = m_alertConfigManager.queryConfigs(domain, key, MetricType.COUNT);

					monitorConfigsByItem.put(MetricType.COUNT, tmpConfigs);
				}
				if (config.isShowSum()) {
					List<Config> tmpConfigs = m_alertConfigManager.queryConfigs(domain, key, MetricType.SUM);

					monitorConfigsByItem.put(MetricType.SUM, tmpConfigs);
				}
				monitorConfigs.put(key, monitorConfigsByItem);
			}
		}
		return new AlarmRule(monitorConfigs);
	}

	private int calAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_AREADY_MINUTE;

		return minute;
	}

	@Override
	public String getName() {
		return AlertType.Business2.getName();
	}

	private boolean needAlert(BusinessItemConfig config, String domain) {
		if (config.isAlarm()) {
			return true;
		}
		Set<String> tags = m_tagConfigManager.findTagByDomain(domain).get(config.getId());

		if (tags != null && tags.contains(DEFAULT_TAG)) {
			return true;
		} else {
			return false;
		}
	}

	private List<DataCheckEntity> processBusinessItem(BusinessReportGroup reportGroup,
	      Map<MetricType, List<Config>> alertConfig, BusinessItemConfig config, int minute, String domain) {
		List<DataCheckEntity> results = new ArrayList<DataCheckEntity>();
		String id = config.getId();

		if (config.isShowAvg()) {
			String metricKey = m_keyHelper.generateKey(id, domain, MetricType.AVG.getName());
			List<DataCheckEntity> tmpResults = processMetricType(minute, alertConfig.get(MetricType.AVG), reportGroup,
			      metricKey, MetricType.AVG);

			results.addAll(tmpResults);
		}
		if (config.isShowCount()) {
			String metricKey = m_keyHelper.generateKey(id, domain, MetricType.COUNT.getName());
			List<DataCheckEntity> tmpResults = processMetricType(minute, alertConfig.get(MetricType.COUNT), reportGroup,
			      metricKey, MetricType.COUNT);

			results.addAll(tmpResults);
		}
		if (config.isShowSum()) {
			String metricKey = m_keyHelper.generateKey(id, domain, MetricType.SUM.getName());
			List<DataCheckEntity> tmpResults = processMetricType(minute, alertConfig.get(MetricType.SUM), reportGroup,
			      metricKey, MetricType.SUM);

			results.addAll(tmpResults);
		}

		return results;
	}

	private void processDomain(String domain) {
		BusinessReportConfig businessReportConfig = m_configManager.queryConfigByDomain(domain);
		AlarmRule monitorConfigs = buildMonitorConfigs(domain, businessReportConfig);
		int minute = calAlreadyMinute();
		int maxDuration = monitorConfigs.calMaxRuleMinute();

		if (maxDuration > 0) {
			BusinessReportGroup reportGroup = m_service.prepareDatas(domain, minute, maxDuration);

			if (reportGroup.isDataReady()) {
				Collection<BusinessItemConfig> configs = businessReportConfig.getBusinessItemConfigs().values();

				for (BusinessItemConfig itemConfig : configs) {
					Map<MetricType, List<Config>> alertConfig = monitorConfigs.getConfigs().get(itemConfig.getId());

					if (alertConfig != null) {
						List<DataCheckEntity> results = processBusinessItem(reportGroup, alertConfig, itemConfig, minute,
						      domain);
						sendBusinessAlerts(domain, itemConfig.getId(), results);
					}
				}
			}
		}
	}

	private List<DataCheckEntity> processMetricType(int minute, List<Config> configs, BusinessReportGroup reportGroup,
	      String metricKey, MetricType type) {
		Pair<Integer, List<Condition>> conditionPair = m_baseRuleHelper.convertConditions(configs);

		if (conditionPair != null) {
			int ruleMinute = conditionPair.getKey();
			double[] value = reportGroup.extractData(minute, ruleMinute, m_keyHelper.getBusinessItemId(metricKey), type);
			double[] baseline = m_baselineService.queryBaseline(minute, ruleMinute, metricKey, BusinessAnalyzer.ID);
			List<Condition> conditions = conditionPair.getValue();

			return m_dataChecker.checkData(value, baseline, conditions);
		} else {
			return new ArrayList<DataCheckEntity>();
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("AlertBusiness2", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Set<String> domains = m_projectService.findAllDomains();

				for (String domain : domains) {
					try {
						processDomain(domain);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}

			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	private void sendBusinessAlerts(String domain, String metricName, List<DataCheckEntity> alertResults) {
		for (DataCheckEntity alertResult : alertResults) {
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
			      .setLevel(alertResult.getAlertLevel());
			entity.setMetric(metricName).setType(getName()).setDomain(domain).setGroup(domain);
			entity.setContactGroup(domain);
			m_sendManager.addAlert(entity);
		}
	}

	@Override
	public void shutdown() {
	}

}
