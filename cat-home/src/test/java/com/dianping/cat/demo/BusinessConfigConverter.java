package com.dianping.cat.demo;

import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MetricItem;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.consumer.metric.config.entity.MetricConfig;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.config.entity.Tag;
import com.dianping.cat.consumer.metric.config.transform.DefaultSaxParser;
import com.dianping.cat.home.business.entity.BusinessItem;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.config.BusinessConfigDao;

public class BusinessConfigConverter extends ComponentTestCase {

	@Test
	public void generateTagConfig() throws Exception {
		FileInputStream fis = new FileInputStream("/Users/ccn/Desktop/metric.xml");
		String metric = Files.forIO().readFrom(fis, "utf-8");
		MetricConfig metricConfig = DefaultSaxParser.parse(metric);
		Map<String, MetricItemConfig> configs = metricConfig.getMetricItemConfigs();
		BusinessTagConfig tagConfig = new BusinessTagConfig();

		for (MetricItemConfig config : configs.values()) {
			List<Tag> tags = config.getTags();

			if (tags != null) {
				for (Tag tag : tags) {
					String name = tag.getName();
					com.dianping.cat.home.business.entity.Tag newTag = tagConfig.findTag(name);

					if (newTag == null) {
						newTag = new com.dianping.cat.home.business.entity.Tag();
						newTag.setId(name);
						tagConfig.addTag(newTag);
					}

					BusinessItem businessItem = new BusinessItem();
					businessItem.setDomain(config.getDomain());
					businessItem.setItemId(config.getMetricKey());
					newTag.addBusinessItem(businessItem);
				}
			}
		}

		System.out.println(tagConfig.toString());
	}

	@Test
	public void generateBaseConfig() throws Exception {
		FileInputStream fis = new FileInputStream("/Users/ccn/Desktop/metric.xml");
		String metric = Files.forIO().readFrom(fis, "utf-8");
		MetricConfig metricConfig = DefaultSaxParser.parse(metric);
		Map<String, MetricItemConfig> configs = metricConfig.getMetricItemConfigs();
		BusinessConfigManager configManager = lookup(BusinessConfigManager.class);
		Map<String, BusinessReportConfig> newConfigs = new HashMap<String, BusinessReportConfig>();

		for (MetricItemConfig metricItemConfig : configs.values()) {
			String domain = metricItemConfig.getDomain();
			BusinessReportConfig reportConfig = newConfigs.get(domain);

			if (reportConfig == null) {
				reportConfig = new BusinessReportConfig();
				reportConfig.setId(domain);
				newConfigs.put(domain, reportConfig);
			}

			BusinessItemConfig itemConfig = new BusinessItemConfig();

			itemConfig.setId(metricItemConfig.getMetricKey());
			itemConfig.setViewOrder(metricItemConfig.getViewOrder());
			itemConfig.setTitle(metricItemConfig.getTitle());
			itemConfig.setShowCount(metricItemConfig.getShowCount());
			itemConfig.setShowAvg(metricItemConfig.getShowAvg());
			itemConfig.setShowSum(metricItemConfig.getShowSum());
			itemConfig.setAlarm(metricItemConfig.getAlarm());

			reportConfig.addBusinessItemConfig(itemConfig);
		}

		for (BusinessReportConfig config : newConfigs.values()) {
			configManager.insertConfigByDomain(config);
		}
	}

	@Test
	public void generateAlert() throws Exception {
		FileInputStream fis = new FileInputStream("/Users/ccn/Desktop/businessRule.xml");
		String alert = Files.forIO().readFrom(fis, "utf-8");
		MonitorRules monitorRules = com.dianping.cat.alarm.rule.transform.DefaultSaxParser.parse(alert);
		Map<String, Rule> rules = monitorRules.getRules();
		Map<String, MonitorRules> newRules = new HashMap<String, MonitorRules>();

		for (Rule originRule : rules.values()) {
			String id = originRule.getId();
			String[] results = id.split(":");
			String domain = results[0];
			MonitorRules monitorRule = newRules.get(domain);

			if (monitorRule == null) {
				monitorRule = new MonitorRules();
				newRules.put(domain, monitorRule);
			}

			String key = results[2];
			Rule newRule = new Rule();
			List<MetricItem> metricItems = originRule.getMetricItems();

			for (MetricItem metricItem : metricItems) {
				if (metricItem.isMonitorAvg()) {
					newRule.setId(key+":AVG");
					newRule.setDynamicAttribute("type", "AVG");
				} else if (metricItem.isMonitorSum()) {
					newRule.setId(key+":SUM");
					newRule.setDynamicAttribute("type", "SUM");
				} else {
					newRule.setId(key+":COUNT");
					newRule.setDynamicAttribute("type", "COUNT");
				}
			}

			for (Config config : originRule.getConfigs()) {
				newRule.addConfig(config);
			}
			monitorRule.addRule(newRule);
		}

//		for (Entry<String, MonitorRules> monitorRule : newRules.entrySet()) {
//			System.out.println(monitorRule.getValue().toString());
//		}

		BusinessConfigDao m_configDao = lookup(BusinessConfigDao.class);

		for (Entry<String, MonitorRules> monitorRule : newRules.entrySet()) {
			try {
				BusinessConfig proto = m_configDao.createLocal();
				proto.setDomain(monitorRule.getKey());
				proto.setContent(monitorRule.getValue().toString());
				proto.setName("alert");
				proto.setUpdatetime(new Date());

				m_configDao.insert(proto);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}