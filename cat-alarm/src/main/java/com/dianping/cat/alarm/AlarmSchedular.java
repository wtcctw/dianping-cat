package com.dianping.cat.alarm;

import java.util.Map;

import org.unidal.helper.Threads.Task;

public class AlarmSchedular implements Task {

	private Map<Integer, Long> m_runTimes;

	public AlarmSchedular(Map<Integer, Long> runTimes) {
		m_runTimes = runTimes;
	}

	public Map<Integer, Long> getRunTimes() {
		return m_runTimes;
	}

	@Override
	public void run() {

	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void shutdown() {

	}

}
