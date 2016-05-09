package com.dianping.cat.report.task;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;

public class StateReportVisitor extends BaseVisitor {

	private Map<String, Set<String>> m_servers = new ConcurrentHashMap<String, Set<String>>();

	private String m_ip;

	public Map<String, Set<String>> getServers() {
		return m_servers;
	}

	@Override
	public void visitMachine(Machine machine) {
		m_ip = machine.getIp();
		super.visitMachine(machine);
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		String domain = processDomain.getName();
		Set<String> servers = m_servers.get(domain);

		if (servers == null) {
			servers = new HashSet<String>();

			m_servers.put(domain, servers);
		}
		servers.add(m_ip);
	}
}
