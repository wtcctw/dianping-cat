package com.dianping.cat.alarm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;

public class AlarmTaskManager extends ContainerHolder implements Initializable {

	private int MAX_THREADS = 500;

	private Map<String, AlarmTask> m_tasks = new ConcurrentHashMap<String, AlarmTask>();

	@Override
	public void initialize() throws InitializationException {
		AlarmTaskConsumer consumerTask = lookup(AlarmTaskConsumer.class);

		Threads.forGroup("cat").start(consumerTask);
		Threads.forGroup("cat").start(new AlarmRuleSyncTask());
	}

}
