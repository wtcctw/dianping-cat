package com.dianping.cat.report.task;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Named
public class RemoteServersUpdater implements Task {

	@Inject(StateAnalyzer.ID)
	protected ReportManager<StateReport> m_reportManager;

	@Inject(type = ModelService.class, value = StateAnalyzer.ID)
	private ModelService<StateReport> m_service;

	@Inject
	private RemoteServersManager m_manager;

	public static final long DURATION = TimeHelper.ONE_MINUTE;

	public StateReport queryStateReport(String domain, long time) {
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(domain, time);

			if (m_service.isEligable(request)) {
				ModelResponse<StateReport> response = m_service.invoke(request);
				StateReport report = response.getModel();

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable state report service registered for " + request
				      + "!");
			}
		} else {
			throw new RuntimeException("Domain server update period is not right: " + period + ", time is: "
			      + new Date(time));
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("ReloadTask", "DomainServer");
			long current = System.currentTimeMillis();

			try {
				long currentHour = TimeHelper.getCurrentHour().getTime();
				long lastHour = currentHour - TimeHelper.ONE_HOUR;

				Map<String, Set<String>> currentServers = buildServers(currentHour);
				Map<String, Set<String>> lastServers = buildServers(lastHour);

				m_manager.setCurrentServers(currentServers);
				m_manager.setLastServers(lastServers);
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

	private Map<String, Set<String>> buildServers(long hour) {
		StateReport currentReport = queryStateReport(Constants.CAT, hour);
		StateReportVisitor visitor = new StateReportVisitor();

		visitor.visitStateReport(currentReport);
		return visitor.getServers();
	}

	@Override
	public String getName() {
		return "domain-server-update-task";
	}

	@Override
	public void shutdown() {
	}

}
