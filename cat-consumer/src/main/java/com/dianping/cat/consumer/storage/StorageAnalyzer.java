package com.dianping.cat.consumer.storage;

import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.storage.DatabaseParser.Database;
import com.dianping.cat.consumer.storage.StorageReportUpdater.StorageUpdateParam;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

public class StorageAnalyzer extends AbstractMessageAnalyzer<StorageReport> implements LogEnabled {

	@Inject
	private StorageDelegate m_storageDelegate;

	@Inject(ID)
	private ReportManager<StorageReport> m_reportManager;

	@Inject
	private DatabaseParser m_databaseParser;

	@Inject
	private StorageReportUpdater m_updater;

	public static final String ID = "storage";

	private static final long LONG_RPC_THRESHOLD = 100;

	private static final long LONG_SQL_THRESHOLD = 1000;

	private static final long LONG_CACHE_THRESHOLD = 50;

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

	private boolean isNotBlank(String serverId) {
		return serverId != null && !serverId.trim().isEmpty();
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	protected void process(MessageTree tree) {
		List<Transaction> transactions = tree.getTransactions();

		for (Transaction t : transactions) {
			processTransaction(tree, t);
		}
	}

	private void processCacheTransaction(MessageTree tree, Transaction t) {
		String ip = "Default";
		String domain = tree.getDomain();
		String cacheType = t.getType();
		int index = cacheType.indexOf(".");

		if (index > -1) {
			cacheType = cacheType.substring(index + 1);
		}
		String name = t.getName();
		String method = name.substring(name.lastIndexOf(":") + 1);
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("Cache.memcached.server") || type.equals("Squirrel.server")) {
					ip = message.getName();
					index = ip.indexOf(":");

					if (index > -1) {
						ip = ip.substring(0, index);
					}
				}
			}
		}
		String id = queryCacheId(cacheType);
		StorageReport report = m_reportManager.getHourlyReport(getStartTime(), id, true);
		StorageUpdateParam param = new StorageUpdateParam();

		param.setDomain(domain).setIp(ip).setMethod(method).setTransaction(t).setThreshold(LONG_CACHE_THRESHOLD);
		m_updater.updateStorageReport(report, param);
	}

	private void processRPCTransaction(MessageTree tree, Transaction t) {
		String serverId = null;
		String domain = tree.getDomain();
		String ip = "default";
		String method = "call";
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("PigeonCall.app")) {
					serverId = message.getName();
				}

				if (type.equals("PigeonCall.server")) {
					ip = message.getName();
					int index = ip.indexOf(':');

					if (index > -1) {
						ip = ip.substring(0, index);
					}
				}
			}
		}

		if (isNotBlank(serverId)) {
			String id = queryRPCId(serverId);
			StorageReport report = m_reportManager.getHourlyReport(getStartTime(), id, true);
			StorageUpdateParam param = new StorageUpdateParam();

			param.setDomain(domain).setIp(ip).setMethod(method).setTransaction(t).setThreshold(LONG_RPC_THRESHOLD);
			m_updater.updateStorageReport(report, param);
		}
	}

	private void processSQLTransaction(MessageTree tree, Transaction t) {
		String databaseName = null;
		String method = "select";
		String ip = null;
		String domain = tree.getDomain();
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL.Method")) {
					method = message.getName().toLowerCase();
				}
				if (type.equals("SQL.Database")) {
					Database database = m_databaseParser.queryDatabaseName(message.getName());

					if (database != null) {
						ip = database.getIp();
						databaseName = database.getName();
					}
				}
			}
		}
		if (databaseName != null && ip != null) {
			String id = querySQLId(databaseName);
			StorageReport report = m_reportManager.getHourlyReport(getStartTime(), id, true);
			StorageUpdateParam param = new StorageUpdateParam();

			param.setDomain(domain).setIp(ip).setMethod(method).setTransaction(t).setThreshold(LONG_SQL_THRESHOLD);
			m_updater.updateStorageReport(report, param);
		}
	}

	private void processTransaction(MessageTree tree, Transaction t) {
		String type = t.getType();

		if (m_serverConfigManager.isCacheTransaction(type)) {
			processCacheTransaction(tree, t);
		} else if (m_serverConfigManager.isSQLTransaction(type)) {
			processSQLTransaction(tree, t);
		} else if (m_serverConfigManager.isRpcClient(type)) {
			processRPCTransaction(tree, t);
		}
	}

	private String queryCacheId(String name) {
		return name + "-Cache";
	}

	private String queryRPCId(String name) {
		return name + "-RPC";
	}

	private String querySQLId(String name) {
		return name + "-SQL";
	}

}
