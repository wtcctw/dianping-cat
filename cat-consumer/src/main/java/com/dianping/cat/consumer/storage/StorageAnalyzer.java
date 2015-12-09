package com.dianping.cat.consumer.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.storage.StorageReportUpdater.StorageUpdateParam;
import com.dianping.cat.consumer.storage.manager.StorageManager;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

public class StorageAnalyzer extends AbstractMessageAnalyzer<StorageReport> implements LogEnabled, Initializable {

	@Inject
	private StorageDelegate m_storageDelegate;

	@Inject(ID)
	private ReportManager<StorageReport> m_reportManager;

	@Inject
	private StorageDBParser m_databaseParser;

	@Inject
	private StorageReportUpdater m_updater;

	private Map<String, StorageManager> m_storageManagers;

	public static final String ID = "storage";

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
			m_databaseParser.showErrorCon();
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public int getAnanlyzerCount() {
		return 2;
	}

	@Override
	public StorageReport getReport(String id) {
		long period = getStartTime();
		StorageReport report = m_reportManager.getHourlyReport(period, id, false);

		m_updater.updateStorageIds(id, m_reportManager.getDomains(period), report);
		return report;
	}

	@Override
	public ReportManager<StorageReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public void initialize() throws InitializationException {
		m_storageManagers = lookupMap(StorageManager.class);
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	protected void process(MessageTree tree) {
		List<Transaction> transactions = tree.getTransactions();

		for (Transaction t : transactions) {
			String domain = tree.getDomain();
			Collection<StorageManager> managers = m_storageManagers.values();

			for (StorageManager manager : managers) {
				if (manager.isEligable(t)) {
					String id = manager.queryId(t);

					if (StringUtils.isNotEmpty(id)) {
						String ip = manager.queryIp(t);
						String method = manager.queryMethod(t);
						String reportId = manager.queryReportId(id);
						int threshold = manager.queryThreshold();

						StorageReport report = m_reportManager.getHourlyReport(getStartTime(), reportId, true);
						StorageUpdateParam param = new StorageUpdateParam();

						param.setDomain(domain).setIp(ip).setMethod(method).setTransaction(t).setThreshold(threshold);
						m_updater.updateStorageReport(report, param);
					}
				}
			}
		}
	}
}
