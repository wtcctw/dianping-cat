package com.dianping.cat.report.page.business.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.entity.CustomConfig;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.MetricType;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.Tag;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;
import com.dianping.cat.report.page.business.service.CachedBusinessReportService;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

public class BusinessGraphCreator extends AbstractGraphCreator {

	@Inject
	private CachedBusinessReportService m_reportService;

	@Inject
	private BusinessConfigManager m_configManager;

	@Inject
	private BusinessDataFetcher m_dataFetcher;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private BusinessTagConfigManager m_tagManager;

	@Inject
	private BusinessKeyHelper m_keyHelper;

	@Inject
	private CustomDataCalculator m_customDataCalculator;

	public Map<String, LineChart> buildGraphByTag(Date start, Date end, String tag) {
		Tag tagConfig = m_tagManager.findTag(tag);

		if (tagConfig != null) {
			List<BusinessItem> items = tagConfig.getBusinessItems();
			Map<String, double[]> all = new LinkedHashMap<String, double[]>();
			Map<String, double[]> needed = new LinkedHashMap<String, double[]>();
			Map<String, BusinessReportConfig> configs = new HashMap<String, BusinessReportConfig>();
			Set<String> domains = new HashSet<String>();

			for (BusinessItem item : items) {
				domains.add(item.getDomain());
			}

			for (String domain : domains) {
				BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);

				if (config != null) {
					Map<String, double[]> datas = prepareBusinessItemDatas(start, end, domain, config);

					all.putAll(datas);
					configs.put(domain, config);
				}
			}

			for (BusinessItem item : items) {
				String domain = item.getDomain();
				String key = item.getItemId();

				for (MetricType metricType : MetricType.values()) {
					String id = m_keyHelper.generateKey(key, domain, metricType.getName());
					double[] data = all.get(id);

					if (data != null) {
						needed.put(id, data);
					}
				}
			}

			return buildChartData(needed, start, end, configs);
		} else {
			return new HashMap<String, LineChart>();
		}
	}

	public Map<String, LineChart> buildGraphByDomain(Date start, Date end, String domain) {
		Map<String, BusinessReportConfig> configs = new HashMap<String, BusinessReportConfig>();
		BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);

		if (config != null) {
			Map<String, double[]> datas = prepareBusinessItemDatas(start, end, domain, config);

			prepareCustomData(start, end, domain, config, datas);
			configs.put(domain, config);

			return buildChartData(datas, start, end, configs);
		} else {
			return new HashMap<String, LineChart>();
		}
	}

	private void prepareCustomData(Date start, Date end, String currentDomain, BusinessReportConfig currentConfig,
	      Map<String, double[]> datas) {
		Map<String, double[]> businessItemDataCache = new LinkedHashMap<String, double[]>();
		Map<String, CustomConfig> customConfigs = currentConfig.getCustomConfigs();
		Set<String> domains = new HashSet<String>();
		int totalSize = (int) ((end.getTime() - start.getTime()) / TimeHelper.ONE_MINUTE);

		domains.add(currentDomain);
		businessItemDataCache.putAll(datas);

		for (CustomConfig customConfig : customConfigs.values()) {
			String pattern = customConfig.getPattern();
			Pair<Boolean, List<CustomInfo>> translate = m_customDataCalculator.translatePattern(pattern);

			if (translate.getKey()) {
				List<CustomInfo> customInfos = translate.getValue();

				for (CustomInfo customInfo : customInfos) {
					String domain = customInfo.getDomain();

					if (!domains.contains(domain)) {
						BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);

						domains.add(domain);
						businessItemDataCache.putAll(prepareBusinessItemDatas(start, end, domain, config));
					}
				}
				double[] result = m_customDataCalculator.calculate(pattern, customInfos, businessItemDataCache, totalSize);
				String key = m_keyHelper.generateKey(customConfig.getId(), currentDomain, MetricType.AVG.getName());

				datas.put(key, result);
			} else {
				Cat.logEvent("BusinessPatternWrong", pattern);
			}
		}
	}

	private Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date start, Date end,
	      Map<String, BusinessReportConfig> configs) {
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(datas);
		Map<String, double[]> dataWithOutFutures = removeFutureData(end, allCurrentValues);

		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		List<AlertEntity> alertKeys = m_alertManager.queryLastestAlarmKey(5);
		int step = m_dataExtractor.getStep();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			try {
				String key = entry.getKey();
				double[] value = entry.getValue();
				LineChart lineChart = new LineChart();
				String domain = m_keyHelper.getDomain(key);
				BusinessReportConfig config = configs.get(domain);

				buildLineChartTitle(alertKeys, lineChart, key, config);
				lineChart.setStart(start);
				lineChart.setSize(value.length);
				lineChart.setStep(step * TimeHelper.ONE_MINUTE);
				double[] baselines = queryBaseline(BusinessAnalyzer.ID, key, start, end);
				Map<Long, Double> all = convertToMap(datas.get(key), start, 1);
				Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), start, step);

				addLastMinuteData(current, all, m_lastMinute, end);
				lineChart.add(Chinese.CURRENT_VALUE, current);
				lineChart.add(Chinese.BASELINE_VALUE, convertToMap(m_dataExtractor.extract(baselines), start, step));
				charts.put(key, lineChart);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return charts;
	}

	private Map<String, double[]> prepareBusinessItemDatas(Date startDate, Date endDate, String domain,
	      BusinessReportConfig config) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeHelper.ONE_MINUTE);
		Map<String, double[]> oldCurrentValues = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeHelper.ONE_HOUR) {
			BusinessReport report = m_reportService.queryBusinessReport(domain, new Date(start));
			Map<String, double[]> currentValues = buildGraphData(report, config);

			mergeMap(oldCurrentValues, currentValues, totalSize, index);
			index++;
		}
		return oldCurrentValues;
	}

	private Map<String, double[]> buildGraphData(BusinessReport report, BusinessReportConfig config) {
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();
		Map<String, double[]> datas = m_dataFetcher.buildGraphData(report);
		Map<String, BusinessItemConfig> businessItemConfigs = config.getBusinessItemConfigs();
		List<BusinessItemConfig> items = new ArrayList<BusinessItemConfig>(businessItemConfigs.values());

		Collections.sort(items, new Comparator<BusinessItemConfig>() {

			@Override
			public int compare(BusinessItemConfig m1, BusinessItemConfig m2) {
				return (int) ((m1.getViewOrder() - m2.getViewOrder()) * 100);
			}
		});

		for (BusinessItemConfig itemConfig : items) {
			String key = itemConfig.getId();

			if (itemConfig.getShowAvg()) {
				String avgKey = m_keyHelper.generateKey(key, report.getDomain(), MetricType.AVG.name());
				putKey(datas, values, avgKey);
			}
			if (itemConfig.getShowCount()) {
				String countKey = m_keyHelper.generateKey(key, report.getDomain(), MetricType.COUNT.name());
				putKey(datas, values, countKey);
			}
			if (itemConfig.getShowSum()) {
				String sumKey = m_keyHelper.generateKey(key, report.getDomain(), MetricType.SUM.name());
				putKey(datas, values, sumKey);
			}
		}

		return values;
	}

	private void buildLineChartTitle(List<AlertEntity> alertKeys, LineChart chart, String key,
	      BusinessReportConfig businessReportConfig) {
		String itemId = m_keyHelper.getBusinessItemId(key);
		String type = m_keyHelper.getType(key);
		String title = buidlTitle(businessReportConfig, itemId, type);

		chart.setTitle(title);
		chart.setId(key);

		if (containMetric(alertKeys, itemId)) {
			String domain = businessReportConfig.getId();
			String contactInfo = buildContactInfo(domain);

			chart.setHtmlTitle("<span style='color:red'>" + title + "<br><small>" + contactInfo + "</small></span>");
		} else {
			chart.setHtmlTitle(title);
		}
	}

	private String buidlTitle(BusinessReportConfig businessReportConfig, String itemId, String type) {
		String title = null;
		String des = MetricType.getDesByName(type);
		BusinessItemConfig config = businessReportConfig.findBusinessItemConfig(itemId);

		if (config != null) {
			title = config.getTitle() + des;
		} else {
			CustomConfig customConfig = businessReportConfig.findCustomConfig(itemId);

			if (customConfig != null) {
				title = customConfig.getTitle() + des;
			}
		}
		return title;
	}

	private boolean containMetric(List<AlertEntity> alertKeys, String metricId) {
		for (AlertEntity alertMetric : alertKeys) {
			if (alertMetric.getRealMetricId().equals(metricId)) {
				return true;
			}
		}
		return false;
	}

	protected String buildContactInfo(String domainName) {
		try {
			Project project = m_projectService.findByDomain(domainName);

			if (project != null) {
				String owners = project.getOwner();
				String phones = project.getPhone();
				StringBuilder builder = new StringBuilder();

				builder.append("[项目: ").append(domainName);
				if (!StringUtils.isEmpty(owners)) {
					builder.append(" 负责人: ").append(owners);
				}
				if (!StringUtils.isEmpty(phones)) {
					builder.append(" 手机: ").append(phones).append(" ]");
				}
				return builder.toString();
			}
		} catch (Exception ex) {
			Cat.logError("build contact info error for doamin: " + domainName, ex);
		}

		return null;
	}

}
