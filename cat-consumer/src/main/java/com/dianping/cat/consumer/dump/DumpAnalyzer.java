package com.dianping.cat.consumer.dump;

import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;
import org.unidal.cat.message.storage.MessageFinderManager;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.statistic.ServerStatisticManager;

@Named(type = MessageAnalyzer.class, value = DumpAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements LogEnabled {
	public static final String ID = "dump";

	@Inject
	private ServerStatisticManager m_serverStateManager;

	@Inject
	private MessageDumperManager m_dumperManager;

	@Inject
	private MessageFinderManager m_finderManager;

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private MessageBucketManager m_bucketManager;

	private boolean m_useNewStorage;

	private Logger m_logger;

	private void closeStorage() {
		Transaction t = Cat.newTransaction("Dumper", "Storage");
		try {
			int hour = (int) TimeUnit.MILLISECONDS.toHours(m_startTime);

			m_dumperManager.close(hour);
			m_finderManager.close(hour);
			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd) {
			Threads.forGroup("cat").start(new Runnable() {
				@Override
				public void run() {
					closeStorage();
				}

			});
		} else {
			closeStorage();
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
	public ReportManager<?> getReportManager() {
		return null;
	}

	@Override
	public void initialize(long startTime, long duration, long extraTime) {
		super.initialize(startTime, duration, extraTime);

		int hour = (int) TimeUnit.MILLISECONDS.toHours(startTime);
		m_useNewStorage = m_serverConfigManager.isUseNewStorage();

		System.err.println("use storage " + m_useNewStorage);
		if (m_useNewStorage) {
			m_dumperManager.findOrCreate(hour);
		}
	}

	private boolean invalid(String domain) {
		return "PhoenixAgent".equals(domain) || "phoenix-agent".equals(domain) || "UNKNOWN".equals(domain);
	}

	@Override
	protected void loadReports() {
		// do nothing
	}

	@Override
	public void process(MessageTree tree) {
		MessageId messageId = MessageId.parse(tree.getMessageId());
		int hour = messageId.getHour();
		String domain = messageId.getDomain();

		if (invalid(domain)) {
			return;
		} else {
			if (m_useNewStorage) {
				processWithStorage(tree, messageId, hour);
			} else {
				processWithBucket(tree, messageId);
			}
		}
	}

	private void processWithBucket(MessageTree tree, MessageId messageId) {
	   long time = tree.getMessage().getTimestamp();
	   long fixedTime = time - time % (TimeHelper.ONE_HOUR);
	   long idTime = messageId.getTimestamp();
	   long duration = fixedTime - idTime;

	   if (duration == 0 || duration == ONE_HOUR || duration == -ONE_HOUR) {
	   	m_bucketManager.storeMessage(tree, messageId);
	   } else {
	   	m_serverStateManager.addPigeonTimeError(1);
	   }
   }

	private void processWithStorage(MessageTree tree, MessageId messageId, int hour) {
		MessageDumper dumper = m_dumperManager.find(hour);

		tree.setFormatMessageId(messageId);

		if (dumper != null) {
			dumper.process(tree);
		} else {
			m_serverStateManager.addPigeonTimeError(1);
		}
	}

	public void setServerStateManager(ServerStatisticManager serverStateManager) {
		m_serverStateManager = serverStateManager;
	}

}
