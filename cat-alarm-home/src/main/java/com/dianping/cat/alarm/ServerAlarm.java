package com.dianping.cat.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.server.entity.Rule;
import com.dianping.cat.alarm.server.entity.ServerAlarmRuleConfig;
import com.dianping.cat.alarm.server.transform.DefaultSaxParser;
import com.dianping.cat.alarm.service.ServerAlarmRuleService;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.metric.MetricType;
import com.dianping.cat.metric.QueryParameter;

public abstract class ServerAlarm implements Task {

	@Inject
	private MetricService m_metricService;

	@Inject
	private ServerAlarmRuleService m_alarmRuleService;

	public abstract String getType();

	@Override
	public void run() {
		String type = getType();
		List<ServerAlarmRule> rules = m_alarmRuleService.queryRules(type);
		List<String> endPoints = m_metricService.queryEndPoints(type);

		for (String endpoint : endPoints) {
			ServerAlarmRule rule = findRule(rules);

			if (rule != null) {
				List<QueryParameter> queries = buildQueries(endpoint, rule);

				// m_metricService.query(queries);
			}
		}
	}

	private List<QueryParameter> buildQueries(String endPoint, ServerAlarmRule rule) {
		List<QueryParameter> queries = new ArrayList<QueryParameter>();

		try {
			ServerAlarmRuleConfig ruleConfig = DefaultSaxParser.parse(rule.getContent());
			List<Rule> rules = ruleConfig.getRules();

			for (Rule r : rules) {
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
					queries.add(parameter);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return queries;
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
	public void shutdown() {
	}

	@Override
	public String getName() {
		return getType() + "-task";
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
