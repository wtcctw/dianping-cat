package com.dianping.cat.consumer.business;

import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.business.ConfigItem;
import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.consumer.config.ProductLineConfig;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.message.Metric;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;

public class BusinessAnalyzer extends AbstractMessageAnalyzer<BusinessReport> {
	public static final String ID = "business";

	@Inject(ID)
	private ReportManager<BusinessReport> m_reportManager;

	@Inject
	private BusinessConfigManager m_configManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Override
	public ReportManager<BusinessReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public BusinessReport getReport(String domain) {
		long period = getStartTime();
		return m_reportManager.getHourlyReport(period, domain, false);
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		BusinessReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
		List<Metric> metrics = tree.getMetrics();

		for (Metric metric : metrics) {
			processMetric(report, metric, domain);
		}
	}

	private void processMetric(BusinessReport report, Metric metric, String domain) {
		String name = metric.getName();
		String data = (String) metric.getData();
		String status = metric.getStatus();
		ProductLineConfig productLine = m_productLineConfigManager.queryProductLineConfig(metric.getType());

		if (ProductLineConfig.METRIC.equals(productLine)) {
			ConfigItem config = parseValue(status, data);

			if (config != null) {
				long current = metric.getTimestamp() / 1000 / 60;
				int min = (int) (current % 60);
				BusinessItem businessItem = report.findOrCreateBusinessItem(name);
				Segment seg = businessItem.findOrCreateSegment(min);

				businessItem.setType(status);
				seg.setCount(seg.getCount() + config.getCount());
				seg.setSum(seg.getSum() + config.getValue());
				seg.setAvg(seg.getSum() / seg.getCount());

				config.setTitle(name);

				boolean result = m_configManager.insertBusinessConfigIfNotExist(domain, name, config);

				if (!result) {
					m_logger.error(String.format("error when insert metric config info, domain %s, metricName %s", domain,
					      name));
				}
			}
		}
	}

	private ConfigItem parseValue(String status, String data) {
		ConfigItem config = new ConfigItem();

		if ("C".equals(status)) {
			if (StringUtils.isEmpty(data)) {
				data = "1";
			}
			int count = (int) Double.parseDouble(data);

			config.setCount(count);
			config.setValue((double) count);
			config.setShowCount(true);
		} else if ("T".equals(status)) {
			double duration = Double.parseDouble(data);

			config.setCount(1);
			config.setValue(duration);
			config.setShowAvg(true);
		} else if ("S".equals(status)) {
			double sum = Double.parseDouble(data);

			config.setCount(1);
			config.setValue(sum);
			config.setShowSum(true);
		} else if ("S,C".equals(status)) {
			String[] datas = data.split(",");

			config.setCount(Integer.parseInt(datas[0]));
			config.setValue(Double.parseDouble(datas[1]));
			config.setShowSum(true);
		} else {
			return null;
		}

		return config;
	}

}
