package com.dianping.cat.alarm;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.alarm.ServerAlarmRule;
import com.dianping.cat.message.Transaction;

public class AlarmTaskConsumer implements Task, Initializable {

	@Inject
	private ServerAlarmRuleService m_ruleService;

	private ThreadPoolExecutor m_executors;

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			Set<Entry<String, List<ServerAlarmRule>>> rules = m_ruleService.queryAllRules().entrySet();

			for (Entry<String, List<ServerAlarmRule>> task : rules) {
				Transaction t = Cat.newTransaction("Alert", task.getValue().getCategory());

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
	public String getName() {
		return "alert-task-consumer";
	}

	@Override
	public void shutdown() {

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

}
