package com.dianping.cat.report.alert.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.app.AppAlarmRuleParam;
import com.dianping.cat.alarm.app.AppAlarmRuleParamBuilder;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.service.AppAlarmRuleInfo;
import com.dianping.cat.alarm.service.AppAlarmRuleService;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.app.QueryType;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;

@Named
public class AppAlert implements Task {

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AlertManager m_sendManager;

	@Inject
	private AppAlarmRuleService m_appAlarmService;

	@Inject
	private DataChecker m_dataChecker;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	@Inject
	private AppAlarmRuleParamBuilder m_alarmRuleParamBuilder;

	private static final long DURATION = TimeHelper.ONE_MINUTE * 5;

	private static final int DATA_AREADY_MINUTE = 10;

	private Long buildMillsByString(String time) throws Exception {
		String[] times = time.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		long result = hour * 60 * 60 * 1000 + minute * 60 * 1000;

		return result;
	}

	private Pair<Date, Integer> buildTimePair(long time) {
		Pair<Date, Integer> dayAndMinute = new Pair<Date, Integer>();
		Date day = TimeHelper.getCurrentDay(time);
		int minute = (int) ((time - day.getTime()) / TimeHelper.ONE_MINUTE);

		dayAndMinute.setKey(day);
		dayAndMinute.setValue(minute);
		return dayAndMinute;
	}

	private double[] fetchDatas(AppAlarmRuleParam param, QueryType type, int minute) {
		long currentTime = System.currentTimeMillis();
		long endTime = currentTime - DATA_AREADY_MINUTE * TimeHelper.ONE_MINUTE;
		long startTime = endTime - minute * TimeHelper.ONE_MINUTE;
		double[] datas = null;

		Pair<Date, Integer> end = buildTimePair(endTime);
		Pair<Date, Integer> start = buildTimePair(startTime);

		if (end.getKey().getTime() == start.getKey().getTime()) {
			CommandQueryEntity queryEntity = new CommandQueryEntity(end.getKey(), param, start.getValue(),
			      end.getValue() - 1);
			datas = m_appDataService.queryAlertValue(queryEntity, type, minute);
		} else {
			CommandQueryEntity endQueryEntity = new CommandQueryEntity(end.getKey(), param,
			      CommandQueryEntity.DEFAULT_VALUE, end.getValue() - 1);
			CommandQueryEntity startQueryEntity = new CommandQueryEntity(start.getKey(), param, start.getValue(),
			      CommandQueryEntity.DEFAULT_VALUE);
			double[] endDatas = m_appDataService.queryAlertValue(endQueryEntity, type, minute);
			double[] startDatas = m_appDataService.queryAlertValue(startQueryEntity, type, minute);

			datas = mergerArray(endDatas, startDatas);
		}

		return datas;
	}

	@Override
	public String getName() {
		return AlertType.App.getName();
	}

	private boolean judgeCurrentInConfigRange(Config config) {
		long ruleStartTime;
		long ruleEndTime;
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int nowTime = hour * 60 * 60 * 1000 + minute * 60 * 1000;

		try {
			ruleStartTime = buildMillsByString(config.getStarttime());
			ruleEndTime = buildMillsByString(config.getEndtime());
		} catch (Exception ex) {
			ruleStartTime = 0L;
			ruleEndTime = 86400000L;
		}

		if (nowTime < ruleStartTime || nowTime > ruleEndTime) {
			return false;
		}

		return true;
	}

	protected double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	private void processRule(Rule rule) {
		List<AppAlarmRuleParam> params = m_alarmRuleParamBuilder.build(rule);

		for (AppAlarmRuleParam param : params) {
			QueryType queryType = QueryType.findByName(param.getMetric());
			int command = param.getCommand();
			Pair<Integer, List<Condition>> pair = queryCheckMinuteAndConditions(rule.getConfigs());
			double[] datas = fetchDatas(param, queryType, pair.getKey());

			if (datas != null && datas.length > 0) {
				List<Condition> checkedConditions = pair.getValue();
				List<DataCheckEntity> alertResults = m_dataChecker.checkDataForApp(datas, checkedConditions);
				String commandName = queryCommand(command);

				for (DataCheckEntity alertResult : alertResults) {
					Map<String, Object> par = new HashMap<String, Object>();
					par.put("param", param);
					AlertEntity entity = new AlertEntity();

					entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
					      .setLevel(alertResult.getAlertLevel());
					entity.setMetric(queryType.getTitle()).setType(getName()).setGroup(commandName).setParas(par);
					m_sendManager.addAlert(entity);
				}
			}
		}
	}

	private Pair<Integer, List<Condition>> queryCheckMinuteAndConditions(List<Config> configs) {
		int maxMinute = 0;
		List<Condition> conditions = new ArrayList<Condition>();
		Iterator<Config> iterator = configs.iterator();

		while (iterator.hasNext()) {
			Config config = iterator.next();

			if (judgeCurrentInConfigRange(config)) {
				List<Condition> tmpConditions = config.getConditions();
				conditions.addAll(tmpConditions);

				for (Condition con : tmpConditions) {
					int tmpMinute = con.getMinute();

					if (tmpMinute > maxMinute) {
						maxMinute = tmpMinute;
					}
				}
			}
		}
		return new Pair<Integer, List<Condition>>(maxMinute, conditions);
	}

	private String queryCommand(int command) {
		Map<Integer, Command> commands = m_appConfigManager.getRawCommands();
		Command value = commands.get(command);

		if (value != null) {
			return value.getName();
		} else {
			throw new RuntimeException("Error config in command code: " + command);
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("AlertApp", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				Map<String, List<AppAlarmRuleInfo>> alarmRules = m_appAlarmService.queryAllRules();

				for (List<AppAlarmRuleInfo> rules : alarmRules.values()) {
					for (AppAlarmRuleInfo rule : rules) {
						try {
							processRule(rule.getRule());
						} catch (Exception e) {
							Cat.logError(e);
						}
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

	@Override
	public void shutdown() {
	}
}
