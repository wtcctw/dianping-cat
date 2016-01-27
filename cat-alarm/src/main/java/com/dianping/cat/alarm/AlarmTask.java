package com.dianping.cat.alarm;

import com.dianping.cat.core.alarm.ServerAlarmRule;

public class AlarmTask {

	private ServerAlarmRule m_alarmRule;

	private long m_startTime;

	private String m_category;

	private boolean m_completed;

	public AlarmTask(ServerAlarmRule rule, String categroy) {
		m_alarmRule = rule;
		m_category = categroy;
	}

	public ServerAlarmRule getAlarmRule() {
		return m_alarmRule;
	}

	public String getCategory() {
		return m_category;
	}

	public long getStartTime() {
		return m_startTime;
	}

	public boolean isCompleted() {
		return m_completed;
	}

	public void setAlarmRule(ServerAlarmRule alarmRule) {
		m_alarmRule = alarmRule;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	public void setCompleted(boolean completed) {
		m_completed = completed;
	}

	public void setStartTime(long startTime) {
		m_startTime = startTime;
	}

}
