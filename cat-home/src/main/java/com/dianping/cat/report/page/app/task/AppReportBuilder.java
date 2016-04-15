package com.dianping.cat.report.page.app.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppCommandDataEntity;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.entity.Code;
import com.dianping.cat.home.app.entity.Transaction;
import com.dianping.cat.home.app.transform.DefaultNativeBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.alert.app.AppRuleConfigManager;
import com.dianping.cat.report.page.app.service.AppReportService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class AppReportBuilder implements TaskBuilder {

	@Inject
	private AppCommandDataDao m_dao;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	@Inject
	private AppReportService m_appReportService;

	@Inject
	private TransactionReportService m_transactionReportService;

	@Inject
	private CommandAutoCompleter m_autoCompleter;

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public static final String ID = Constants.APP;

	public static final int COMMAND_MIN_COUNT = 10;

	private void pruneAppCommand(AppReport appReport) {
		for (Entry<Integer, com.dianping.cat.home.app.entity.Command> command : appReport.getCommands().entrySet()) {
			if (command.getValue().getCount() < COMMAND_MIN_COUNT) {
				try {
					int id = command.getKey();
					String name = m_appConfigManager.getRawCommands().get(id).getName();
					boolean success = m_appConfigManager.deleteCommand(id);

					if (success) {
						Cat.logEvent("AppCommandPrune", id + ":" + name, Event.SUCCESS, command.toString());
						m_appRuleConfigManager.deleteByCommandId(id);
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
	}

	private AppReport buildDailyReport(String id, Date period) {
		AppReport report = m_appReportService.makeReport(id, period, TaskHelper.tomorrowZero(period));

		for (Command command : m_appConfigManager.queryCommands().values()) {
			processCommand(period, command, report);
		}
		return report;
	}

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			m_autoCompleter.autoCompleteDomain(period);
		} catch (Exception e) {
			Cat.logError(e);
		}

		try {
			AppReport appReport = buildDailyReport(domain, period);

			if (m_mobileConfigManager.shouldAutoPrune()) {
				pruneAppCommand(appReport);
			}

			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(appReport);

			return m_appReportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support weekly task");
	}

	private void processCommand(Date period, Command command, AppReport report) {
		int commandId = command.getId();
		List<AppCommandData> datas = new ArrayList<AppCommandData>();
		com.dianping.cat.home.app.entity.Command cmd = report.findOrCreateCommand(command.getId());

		cmd.setName(command.getName());

		try {
			datas = m_dao.findDailyDataByCode(commandId, period, AppCommandDataEntity.READSET_CODE_DATA);

			for (AppCommandData data : datas) {
				processRecord(commandId, cmd, data);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}

		String domain = command.getDomain();

		if (StringUtils.isNotEmpty(domain) && commandId != AppCommandConfigManager.ALL_COMMAND_ID) {
			processTransactionInfo(cmd, domain, period);
		}
	}

	private void processRecord(int commandId, com.dianping.cat.home.app.entity.Command cmd, AppCommandData data) {
		int codeId = data.getCode();
		boolean success = m_appConfigManager.isSuccessCode(commandId, codeId);
		long count = data.getAccessNumberSum();
		long responseTime = data.getResponseSumTimeSum();

		cmd.incCount(count).incSum(responseTime).incRequestSum(data.getRequestPackageSum())
		      .incResponseSum(data.getResponsePackageSum());

		Code code = cmd.findOrCreateCode(String.valueOf(codeId));

		code.incCount(count);
		code.incSum(responseTime);

		if (!success) {
			cmd.incErrors(count);
			code.incErrors(count);
		}
		long cmdCount = cmd.getCount();
		if (cmdCount > 0) {
			cmd.setAvg(cmd.getSum() / cmdCount);
			cmd.setSuccessRatio(100.0 - cmd.getErrors() * 100.0 / cmdCount);
			cmd.setRequestAvg(cmd.getRequestSum() * 1.0 / cmdCount);
			cmd.setResponseAvg(cmd.getResponseSum() * 1.0 / cmdCount);
		}
		long codeCount = code.getCount();
		if (codeCount > 0) {
			code.setAvg(code.getSum() / codeCount);
			code.setSuccessRatio(100.0 - code.getErrors() * 100.0 / codeCount);
		}
	}

	private void processTransactionInfo(com.dianping.cat.home.app.entity.Command command, String domain, Date period) {
		Date end = TimeHelper.addDays(period, 1);
		TransactionReport report = m_transactionReportService.queryDailyReport(domain, period, end);

		report = m_mergeHelper.mergeAllMachines(report, Constants.ALL);

		TransactionReportVisitor visitor = new TransactionReportVisitor(command.getName());

		visitor.visitTransactionReport(report);
		Transaction transaction = visitor.getTransaction();

		command.setTransaction(transaction);
	}

	public static class TransactionReportVisitor extends BaseVisitor {

		private String m_command;

		private Transaction m_transation;

		public TransactionReportVisitor(String command) {
			m_command = command;
		}

		public Transaction getTransaction() {
			return m_transation;
		}

		@Override
		public void visitMachine(Machine machine) {
			if (Constants.ALL.equals(machine.getIp())) {
				super.visitMachine(machine);
			}
		}

		@Override
		public void visitName(TransactionName name) {
			String id = name.getId();

			if (id.endsWith(m_command) && m_transation == null) {
				m_transation = new Transaction(id);

				m_transation.setCount(name.getTotalCount());
				m_transation.setAvg(name.getAvg());
			}
		}

		@Override
		public void visitType(TransactionType type) {
			if ("URL".equals(type.getId())) {
				super.visitType(type);
			}
		}
	}

}
