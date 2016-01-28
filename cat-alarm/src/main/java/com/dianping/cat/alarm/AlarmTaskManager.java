package com.dianping.cat.alarm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;

public class AlarmTaskManager extends ContainerHolder implements Initializable {

	private Map<Integer, Long> m_runTimes = new ConcurrentHashMap<Integer, Long>();

	@Override
	public void initialize() throws InitializationException {
		AlarmTaskConsumer consumerTask = lookup(AlarmTaskConsumer.class);

		Threads.forGroup("cat").start(consumerTask);
		Threads.forGroup("cat").start(new AlarmSchedular(m_runTimes));
	}

	public Map<Integer, Long> getRunTimes() {
		return m_runTimes;
	}

}
