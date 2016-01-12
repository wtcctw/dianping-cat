package com.dianping.cat.alarm;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.core.alarm.ServerAlarmRule;
import com.dianping.cat.metric.MetricService;

public abstract class ServerAlarm implements Task {

	@Inject
	private MetricService m_metricService;

	@Inject
	private ServerAlarmRuleService m_alarmRuleService;

	public abstract String getType();

	@Override
	public void run() {
		Map<String, List<ServerAlarmRule>> rules = m_alarmRuleService.queryAllRules();

		for (Entry<String, List<ServerAlarmRule>> entry : rules.entrySet()) {

		}
	}

	@Override
	public void shutdown() {
	}

	@Override
	public String getName() {
		return getType() + "-task";
	}

}
