package com.dianping.cat.report.page.app.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.config.app.AppCommandGroupConfigManager;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;

@Named
public class AppStatisticPiechartBuilder {

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppCommandGroupConfigManager m_commandGroupConfigManager;

	public static final int PIECHART_MAX_ITEM = 10;

	public Map<String, PieChart> buildCodePiecharts(List<String> codeKeys, DisplayCommands displayCommands) {
		Set<Integer> groupIds = buildGroupIds();
		Map<String, PieChart> results = new HashMap<String, PieChart>();
		Map<String, List<DistributionPiechartData>> dists = new HashMap<String, List<DistributionPiechartData>>();

		for (Entry<Integer, DisplayCommand> entry : displayCommands.getCommands().entrySet()) {
			int commandId = entry.getKey();

			if (!groupIds.contains(commandId)) {
				for (Entry<String, DisplayCode> code : entry.getValue().getCodes().entrySet()) {
					String codeKey = code.getKey();

					if (codeKeys.contains(codeKey)) {
						List<DistributionPiechartData> datas = dists.get(codeKey);

						if (datas == null) {
							datas = new ArrayList<DistributionPiechartData>();

							dists.put(codeKey, datas);
						}
						datas.add(new DistributionPiechartData(commandId, code.getValue().getCount()));
					}
				}
			}
		}

		SortHelper.sortMap(dists, new Comparator<Entry<String, List<DistributionPiechartData>>>() {

			@Override
			public int compare(Entry<String, List<DistributionPiechartData>> o1,
			      Entry<String, List<DistributionPiechartData>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		Map<String, List<DistributionPiechartData>> sorted = pruneDistributionDatas(dists, PIECHART_MAX_ITEM);

		for (Entry<String, List<DistributionPiechartData>> entry : sorted.entrySet()) {
			results.put(entry.getKey(), buildPieChart(entry.getKey(), entry.getValue()));
		}
		return results;
	}

	private Set<Integer> buildGroupIds() {
		Set<String> commands = m_commandGroupConfigManager.getConfig().getCommands().keySet();
		Set<Integer> ids = new HashSet<Integer>();

		for (String command : commands) {
			Command cmd = m_appConfigManager.getCommands().get(command);

			if (cmd != null) {
				ids.add(cmd.getId());
			}
		}
		return ids;
	}

	private PieChart buildPieChart(String title, List<DistributionPiechartData> datas) {
		PieChart pieChart = new PieChart().setMaxSize(Integer.MAX_VALUE);
		List<Item> items = new ArrayList<Item>();

		for (DistributionPiechartData data : datas) {
			Item item = new Item();
			int commandId = data.getCommand();
			Command command = m_appConfigManager.getRawCommands().get(commandId);
			String name = command.getTitle();

			if (StringUtils.isEmpty(name)) {
				name = command.getName();
			}
			item.setTitle(name);
			item.setId(data.getCommand());
			item.setNumber(data.getCount());
			items.add(item);
		}

		pieChart.setTitle(title);
		pieChart.addItems(items);
		return pieChart;
	}

	private Map<String, List<DistributionPiechartData>> pruneDistributionDatas(
	      Map<String, List<DistributionPiechartData>> dists, int max) {
		Map<String, List<DistributionPiechartData>> sorted = new HashMap<String, List<DistributionPiechartData>>();

		for (Entry<String, List<DistributionPiechartData>> entry : dists.entrySet()) {
			List<DistributionPiechartData> data = entry.getValue();

			Collections.sort(data, new Comparator<DistributionPiechartData>() {
				@Override
				public int compare(DistributionPiechartData o1, DistributionPiechartData o2) {
					if (o2.getCount() > o1.getCount()) {
						return 1;
					} else if (o2.getCount() < o1.getCount()) {
						return -1;
					} else {
						return 0;
					}
				}
			});
			int size = data.size();
			int index = size > max ? max : size;

			sorted.put(entry.getKey(), data.subList(0, index));
		}
		return sorted;
	}

	public static class DistributionPiechartData {
		private int m_command;

		private long m_count;

		public DistributionPiechartData(int command, long count) {
			m_command = command;
			m_count = count;
		}

		public int getCommand() {
			return m_command;
		}

		public long getCount() {
			return m_count;
		}
	}

}
