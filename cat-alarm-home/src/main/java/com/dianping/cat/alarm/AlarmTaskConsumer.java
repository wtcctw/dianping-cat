package com.dianping.cat.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.server.entity.Rule;
import com.dianping.cat.alarm.server.entity.ServerAlarmRuleConfig;
import com.dianping.cat.alarm.server.transform.DefaultSaxParser;
import com.dianping.cat.alarm.service.ServerAlarmRuleService;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.metric.MetricType;
import com.dianping.cat.metric.QueryParameter;

public class AlarmTaskConsumer implements Task, Initializable {

	@Inject
	private ServerAlarmRuleService m_ruleService;

	@Inject
	private MetricService m_metricService;

	@Inject
	private AlarmSchedular m_schedular;

	private ThreadPoolExecutor m_executors;

	private Map<Integer, Pair<Long, Boolean>> m_runTimes = new ConcurrentHashMap<Integer, Pair<Long, Boolean>>();

	private Map<Integer, AlarmTask> buildAlarmTasks(Map<String, List<ServerAlarmRule>> rules) {
		long current = System.currentTimeMillis();
		Map<Integer, AlarmTask> tasks = new HashMap<Integer, AlarmTask>();

		for (Entry<String, List<ServerAlarmRule>> entry : rules.entrySet()) {
			String category = entry.getKey();
			List<String> endPoints = m_metricService.queryEndPoints(category);

			for (String endpoint : endPoints) {
				ServerAlarmRule rule = findRule(entry.getValue());

				if (rule != null) {
					int ruleId = rule.getId();
					Pair<Long, Boolean> pair = m_runTimes.get(ruleId);

					if (pair == null) {
						AlarmTask task = tasks.get(ruleId);

						if (task == null) {
							try {
								ServerAlarmRuleConfig ruleConfig = DefaultSaxParser.parse(rule.getContent());
								task = new AlarmTask(m_metricService, rule, category);
								Pair<List<Rule>, Long> p = buildDuration(ruleConfig);

								task.addRules(p.getKey());
								task.setStartTime(current);
								task.setInterval(p.getValue());
							} catch (Exception e) {
								Cat.logError(e);
							}
						} else {
							buildQueries(endpoint, task);
						}
					} else {
						m_runTimes.put(ruleId, new Pair<Long, Boolean>(current, false));
					}
				}
			}
		}
		return tasks;
	}

	private Pair<List<Rule>, Long> buildDuration(ServerAlarmRuleConfig ruleConfig) {
		List<Rule> rules = ruleConfig.getRules();
		long sleeptime = Long.MAX_VALUE;
		List<Rule> rets = new ArrayList<Rule>();

		for (Rule r : rules) {
			if (checkTime(r)) {
				for (Condition c : r.getConditions()) {
					Interval interval = Interval.findByInterval(c.getInterval());
					long time = interval.getTime();

					if (time < sleeptime) {
						sleeptime = time;
					}
				}
			}
		}
		return new Pair<List<Rule>, Long>(rets, sleeptime);
	}

	private void buildQueries(String endPoint, AlarmTask task) {
		ServerAlarmRule rule = task.getRule();

		try {
			for (Rule r : task.getRuleConfigs()) {
				for (Condition condition : r.getConditions()) {
					QueryParameter parameter = new QueryParameter();
					Date end = new Date();
					int duration = condition.getDuration();
					String intval = condition.getInterval();
					Interval interval = Interval.findByInterval(intval);
					Date start = new Date(end.getTime() - interval.getTime() * duration);
					MetricType metricType = MetricType.getByName(rule.getType(), MetricType.AVG);
					String tags = "endPoint=" + endPoint + ";" + rule.getTags();

					parameter.setCategory(rule.getCategory()).setType(metricType).setTags(tags).setInterval(intval)
					      .setStart(start).setEnd(end).setMeasurement(rule.getMeasurement());

					task.addParameter(condition, parameter);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private boolean checkTime(Rule r) {
		try {
			Pair<Integer, Integer> startTime = parseHourMinute(r.getStartTime());
			Pair<Integer, Integer> endTime = parseHourMinute(r.getEndTime());
			long current = System.currentTimeMillis();
			long day = TimeHelper.getCurrentDay().getTime();
			long start = day + startTime.getKey() * TimeHelper.ONE_HOUR + endTime.getValue() * TimeHelper.ONE_MINUTE;
			long end = day + endTime.getKey() * TimeHelper.ONE_HOUR + endTime.getValue() * TimeHelper.ONE_MINUTE;

			return current >= start && current <= end;
		} catch (Exception e) {
			Cat.logError(r.toString(), e);
			return false;
		}
	}

	private Pair<Integer, Integer> parseHourMinute(String startTime) {
		String[] times = startTime.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);

		return new Pair<Integer, Integer>(hour, minute);
	}

	private ServerAlarmRule findRule(List<ServerAlarmRule> rules) {
		int min = 0;
		ServerAlarmRule rule = null;

		for (ServerAlarmRule r : rules) {
			String endPoint = r.getEndPoint();
			int ret = validateRegex(endPoint, endPoint);

			if (ret > min) {
				min = ret;
				rule = r;
			}
		}
		return rule;
	}

	@Override
	public String getName() {
		return "alert-task-consumer";
	}

	@Override
	public void initialize() throws InitializationException {
		m_executors = new ThreadPoolExecutor(100, 100, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(5000),
		      new RejectedExecutionHandler() {

			      @Override
			      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				      Cat.logEvent("AlarmDiscards", this.getClass().getSimpleName());
			      }
		      });
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			Map<String, List<ServerAlarmRule>> rules = m_ruleService.queryAllRules();
			Map<Integer, AlarmTask> tasks = buildAlarmTasks(rules);

			for (AlarmTask task : tasks.values()) {
				Transaction t = Cat.newTransaction("Alert", task.getCategory());

				try {

					t.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					Cat.logError(e);
					t.setStatus(e);
				} finally {
					t.complete();
				}
			}
		}
	}

	@Override
	public void shutdown() {

	}

	/**
	 * @return 0: not match; 1: global match; 2: regex match; 3: full match
	 */
	public int validateRegex(String regexText, String text) {
		if (StringUtils.isEmpty(regexText) || "*".equals(regexText)) {
			return 1;
		} else if (regexText.equalsIgnoreCase(text)) {
			return 3;
		} else {
			Pattern p = Pattern.compile(regexText);
			Matcher m = p.matcher(text);

			if (m.find()) {
				return 2;
			} else {
				return 0;
			}
		}
	}

}
