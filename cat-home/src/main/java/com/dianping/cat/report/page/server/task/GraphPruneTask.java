package com.dianping.cat.report.page.server.task;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;

public class GraphPruneTask implements Task {

	public static final long DURATION = TimeHelper.ONE_DAY;

	@Override
	public String getName() {
		return "graph-prune-task";
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			Transaction t = Cat.newTransaction("GraphPrune", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				// TODO
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
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
	public void shutdown() {
	}

}