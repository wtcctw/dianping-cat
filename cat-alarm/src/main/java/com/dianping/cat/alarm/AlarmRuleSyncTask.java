package com.dianping.cat.alarm;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

public class AlarmRuleSyncTask implements Task {

	@Inject
	private ServerAlarmRuleService m_ruleService;

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "alarm-rule-sync-task";
	}

	@Override
	public void shutdown() {
	}

}
