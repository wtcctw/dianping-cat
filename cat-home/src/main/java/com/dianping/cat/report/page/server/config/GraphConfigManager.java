package com.dianping.cat.report.page.server.config;

import java.util.Date;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.screen.entity.Graph;
import com.dianping.cat.home.screen.transform.DefaultSaxParser;
import com.dianping.cat.message.Transaction;

public class GraphConfigManager implements Initializable {

	@Inject
	private ServerConfigManager m_serverConfigManager;

	public Graph queryById(int id) {
		Graph graph = new Graph();
		String xml = "<graph id=\"graph1\" title=\"test\">"
		      + "<item id= \"machine1\" title=\"test\">"
		      + " <segment id=\"cpu.idle\" title=\"cpu.idle\" measure=\"cpu.idle\" tags=\"domain=cat\" endpoint=\"machine1\" type=\"mean\"/>"
		      + " <segment id=\"cpu.sys\" title=\"cpu.sys\" measure=\"cpu.idle\" tags=\"domain=cat\" endpoint=\"machine1\" type=\"mean\"/>"
		      + " <segment id=\"cpu.total\" title=\"cpu.total\" measure=\"cpu.idle\" tags=\"domain=cat\" endpoint=\"machine1\" type=\"mean\"/></item>"
		      + "</graph>";
		try {
			graph = DefaultSaxParser.parseEntity(Graph.class, xml);

			return graph;
		} catch (Exception e) {
			return new Graph();
		}
	}

	public boolean insert(Graph graph) {

		return true;
	}

	public boolean deleteById(int id) {

		return true;
	}

	public boolean deleteBeforeDate(Date date) {

		return true;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_serverConfigManager.isJobMachine()) {
			Threads.forGroup("cat").start(new GraphPruneTask());
		}
	}

	public static class GraphPruneTask implements Task {

		public static final long DURATION = TimeHelper.ONE_DAY;

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
		public String getName() {
			return "graph-prune-task";
		}

		@Override
		public void shutdown() {
		}

	}

}
