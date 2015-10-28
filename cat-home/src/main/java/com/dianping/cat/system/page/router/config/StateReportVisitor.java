package com.dianping.cat.system.page.router.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.helper.SortHelper;

public class StateReportVisitor extends BaseVisitor {

	private RouterConfigManager m_routerConfigManager;

	private Map<String, Map<String, Long>> m_statistics = new HashMap<String, Map<String, Long>>();

	private Comparator<Entry<String, Long>> m_comparator = new Comparator<Map.Entry<String, Long>>() {

		@Override
		public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
			long value = o2.getValue() - o1.getValue();

			if (value > 0) {
				return 1;
			} else if (value < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	};

	public StateReportVisitor(RouterConfigManager routerConfigManager) {
		m_routerConfigManager = routerConfigManager;
	}

	private void generateStatistics(ProcessDomain processDomain, Map<String, Pair<Double, List<String>>> weights) {
		Set<String> ips = processDomain.getIps();
		int size = ips.size();

		for (Entry<String, Pair<Double, List<String>>> entry : weights.entrySet()) {
			String group = entry.getKey();
			double weight = entry.getValue().getKey() * entry.getValue().getValue().size() / size;
			Map<String, Long> datas = m_statistics.get(group);

			if (datas == null) {
				datas = new HashMap<String, Long>();

				m_statistics.put(group, datas);
			}
			String domain = processDomain.getName();
			Long value = datas.get(domain);

			if (value == null) {
				datas.put(domain, 0L);
			} else {
				datas.put(domain, value + (long) (processDomain.getTotal() * weight));
			}
		}
	}

	private Map<String, Pair<Double, List<String>>> generateWeights(ProcessDomain processDomain) {
		Map<String, Pair<Double, List<String>>> weights = new HashMap<String, Pair<Double, List<String>>>();

		for (String ip : processDomain.getIps()) {
			String group = m_routerConfigManager.queryServerGroupByIp(ip);
			Pair<Double, List<String>> pair = weights.get(group);

			if (pair == null) {
				pair = new Pair<Double, List<String>>(0.0, new ArrayList<String>());
				weights.put(group, pair);
			}

			pair.setKey(pair.getKey() + 1.0);
			pair.getValue().add(ip);
		}
		return weights;
	}

	public Map<String, Map<String, Long>> getStatistics() {
		Map<String, Map<String, Long>> datas = new HashMap<String, Map<String, Long>>();

		for (Entry<String, Map<String, Long>> entry : m_statistics.entrySet()) {
			Map<String, Long> ms = SortHelper.sortMap(entry.getValue(), m_comparator);
			datas.put(entry.getKey(), ms);
		}
		return datas;
	}

	@Override
	public void visitProcessDomain(ProcessDomain processDomain) {
		Map<String, Pair<Double, List<String>>> weights = generateWeights(processDomain);

		generateStatistics(processDomain, weights);
	}
}
