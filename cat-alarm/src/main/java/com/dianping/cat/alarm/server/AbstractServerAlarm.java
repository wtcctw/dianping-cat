package com.dianping.cat.alarm.server;

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

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.alarm.server.AlarmTask.AlarmParameter;
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

public abstract class AbstractServerAlarm extends ContainerHolder implements ServerAlarm {

	@Inject
	private ServerAlarmRuleService m_ruleService;

	@Inject
	private MetricService m_metricService;

	private Map<Integer, Long> m_times = new ConcurrentHashMap<Integer, Long>();

	private final static long DURATION = TimeHelper.ONE_SECOND;

	private final static int MAX_THREADS = 100;

	private final static int QUEUE_SIZE = 50;

	private static ThreadPoolExecutor s_threadPool = new ThreadPoolExecutor(MAX_THREADS, MAX_THREADS, 10,
	      TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(QUEUE_SIZE), new RejectedExecutionHandler() {

		      @Override
		      public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			      Cat.logEvent("AlarmDiscards", this.getClass().getSimpleName());
		      }
	      });

	private List<AlarmTask> buildAlarmTasks(Map<Integer, RuleEndPointEntity> ruleEndpoints) {
		long current = System.currentTimeMillis();
		List<AlarmTask> tasks = new ArrayList<AlarmTask>();

		for (Entry<Integer, RuleEndPointEntity> entry : ruleEndpoints.entrySet()) {
			ServerAlarmRule rule = entry.getValue().getRule();
			int ruleId = rule.getId();
			Long meta = m_times.get(ruleId);

			if (meta != null) {
				if (meta <= current) {
					try {
						ServerAlarmRuleConfig ruleConfig = DefaultSaxParser.parse(rule.getContent());
						Pair<Long, List<Rule>> pair = buildDuration(ruleConfig);
						List<String> endpoints = entry.getValue().getEndpoints();
						AlarmTask task = lookup(AlarmTask.class);

						task.setCategory(getCategory());
						task.setAlarmId(getID());
						buildQueries(endpoints, rule, task, pair.getValue());
						tasks.add(task);
						m_times.put(ruleId, current + pair.getKey());
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			} else {
				m_times.put(ruleId, current);
			}
		}

		return tasks;
	}

	private Pair<Long, List<Rule>> buildDuration(ServerAlarmRuleConfig ruleConfig) {
		List<Rule> rules = ruleConfig.getRules();
		List<Rule> rets = new ArrayList<Rule>();
		long sleeptime = Long.MAX_VALUE;

		for (Rule r : rules) {
			if (checkTime(r)) {
				for (Condition c : r.getConditions()) {
					Interval interval = Interval.findByInterval(c.getInterval());
					long time = interval.getTime();

					if (time < sleeptime) {
						sleeptime = time;
					}
				}
				rets.add(r);
			}
		}
		return new Pair<Long, List<Rule>>(sleeptime, rets);
	}

	private void buildQueries(List<String> endPoints, ServerAlarmRule rule, AlarmTask task, List<Rule> rules) {
		for (Rule r : rules) {
			List<Condition> conditions = r.getConditions();
			AlarmParameter alarmParameter = new AlarmParameter(conditions);

			for (Condition condition : conditions) {
				Date end = new Date();
				int duration = condition.getDuration();
				String intval = condition.getInterval();
				Interval interval = Interval.findByInterval(intval);
				Date start = new Date(end.getTime() - interval.getTime() * duration);
				MetricType metricType = MetricType.getByName(rule.getType(), MetricType.AVG);

				for (String ep : endPoints) {
					String tags = "endPoint='" + ep + "';" + rule.getTags();
					QueryParameter parameter = new QueryParameter();

					parameter.setCategory(rule.getCategory()).setType(metricType).setTags(tags).setInterval(intval)
					      .setStart(start).setEnd(end).setMeasurement(rule.getMeasurement());
					alarmParameter.addParameter(parameter);
				}
			}
			task.addParameter(alarmParameter);
		}
	}

	private Map<Integer, RuleEndPointEntity> buildRuleEndpoints(String category) {
		Map<Integer, RuleEndPointEntity> results = new HashMap<Integer, RuleEndPointEntity>();
		List<ServerAlarmRule> rules = m_ruleService.queryRules(category);

		if (rules != null) {
			for (String endpoint : m_metricService.queryEndPoints(category)) {
				ServerAlarmRule rule = findRule(endpoint, rules);

				if (rule != null) {
					int ruleId = rule.getId();
					RuleEndPointEntity entity = results.get(ruleId);

					if (entity == null) {
						entity = new RuleEndPointEntity(rule);

						results.put(ruleId, entity);
					}
					entity.addEndpoint(endpoint);
				}
			}
		}
		return results;
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

	private ServerAlarmRule findRule(String endPoint, List<ServerAlarmRule> rules) {
		int min = 0;
		ServerAlarmRule rule = null;

		for (ServerAlarmRule r : rules) {
			String dp = r.getEndPoint();
			int ret = validateRegex(dp, endPoint);

			if (ret > min) {
				min = ret;
				rule = r;
			}
		}
		return rule;
	}

	private Pair<Integer, Integer> parseHourMinute(String startTime) {
		String[] times = startTime.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);

		return new Pair<Integer, Integer>(hour, minute);
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long current = System.currentTimeMillis();

			try {
				Map<Integer, RuleEndPointEntity> ruleEndpoints = buildRuleEndpoints(getCategory());
				List<AlarmTask> tasks = buildAlarmTasks(ruleEndpoints);

				for (AlarmTask task : tasks) {
					Transaction t = Cat.newTransaction("Alert", task.getCategory());

					try {
						s_threadPool.submit(task);
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						Cat.logError(e);
						t.setStatus(e);
					} finally {
						t.complete();
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
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
	public String getName() {
		return getID() + "-Alarm";
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

	public static class RuleEndPointEntity {

		private ServerAlarmRule m_rule;

		private List<String> m_endpoints = new ArrayList<String>();

		public RuleEndPointEntity(ServerAlarmRule rule) {
			m_rule = rule;
		}

		public void addEndpoint(String endpiont) {
			m_endpoints.add(endpiont);
		}

		public List<String> getEndpoints() {
			return m_endpoints;
		}

		public ServerAlarmRule getRule() {
			return m_rule;
		}

	}

}
