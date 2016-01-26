package com.dianping.cat.report.page.statistics.task.service;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.service.client.entity.ClientReport;
import com.dianping.cat.home.service.client.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.service.ProjectService;

public class ClientReportBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_CLIENT;

	@Inject
	protected ClientReportService m_reportService;

	@Inject
	protected TransactionReportService m_transactionReportService;

	@Inject
	private ServerFilterConfigManager m_configManger;

	@Inject
	private ProjectService m_projectService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		ClientReport clientReport = buildClientReport(period);
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(clientReport);

		return m_reportService.insertDailyReport(report, binaryContent);
	}

	private ClientReport buildClientReport(Date startTime) {
		Date endTime = TimeHelper.addDays(startTime, 1);
		Set<String> domains = m_projectService.findAllDomains();
		ClientReportStatistics statistics = new ClientReportStatistics();

		for (String domain : domains) {
			try {
				if (m_configManger.validateDomain(domain)) {
					TransactionReport r = m_transactionReportService.queryReport(domain, startTime, endTime);

					if (r != null) {
						statistics.visitTransactionReport(r);
					}
				}
			} catch (Exception e) {
				Cat.logError(domain + " client report visitor error", e);
			}
		}
		return statistics.getClienReport();
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Service client report don't support hourly report!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Service client report don't support monthly report!");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("Service client report don't support weekly report!");
	}

}
