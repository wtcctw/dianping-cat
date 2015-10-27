package com.dianping.cat.system.page.router.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.home.router.entity.Network;
import com.dianping.cat.home.router.entity.NetworkPolicy;
import com.dianping.cat.home.router.entity.RouterConfig;

public class SubnetInfoManager {

	private Map<String, List<SubnetInfo>> m_subNetInfos = new HashMap<String, List<SubnetInfo>>();

	private Map<String, String> m_mappingData = new HashMap<String, String>();

	Comparator<Entry<String, List<SubnetInfo>>> compator = new Comparator<Entry<String, List<SubnetInfo>>>() {

		@Override
		public int compare(Entry<String, List<SubnetInfo>> o1, Entry<String, List<SubnetInfo>> o2) {
			if (!o2.getValue().isEmpty() && o1.getValue().isEmpty()) {
				return 1;
			} else if (o2.getValue().isEmpty() && !o1.getValue().isEmpty()) {
				return -1;
			}

			return 0;
		}
	};

	public void cleanCache() {
		m_mappingData.clear();
	}

	public void initialize(RouterConfig routerConfig) {
		refreshNetInfo(routerConfig);
	}

	public String queryBySubnet(String ip) {
		for (Entry<String, List<SubnetInfo>> entry : m_subNetInfos.entrySet()) {
			try {
				List<SubnetInfo> subnetInfos = entry.getValue();
				String serverGroup = entry.getKey();

				if (!subnetInfos.isEmpty()) {
					for (SubnetInfo info : subnetInfos) {
						if (info.isInRange(ip)) {
							return serverGroup;
						}
					}
				} else {
					return serverGroup;
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return "default";
	}

	public String queryServerGroupByIp(String ip) {
		String group = m_mappingData.get(ip);

		if (group == null) {
			group = queryBySubnet(ip);

			m_mappingData.put(ip, group);
		}
		return group;
	}

	protected void refreshNetInfo(RouterConfig routerConfig) {
		Map<String, NetworkPolicy> networkPolicies = routerConfig.getNetworkPolicies();
		Map<String, List<SubnetInfo>> subNetInfos = new HashMap<String, List<SubnetInfo>>();

		for (Entry<String, NetworkPolicy> netPolicy : networkPolicies.entrySet()) {
			ArrayList<SubnetInfo> infos = new ArrayList<SubnetInfo>();

			for (Entry<String, Network> network : netPolicy.getValue().getNetworks().entrySet()) {
				SubnetUtils subnetUtils = new SubnetUtils(network.getValue().getId());
				SubnetInfo netInfo = subnetUtils.getInfo();

				infos.add(netInfo);
			}
			subNetInfos.put(netPolicy.getKey(), infos);
		}

		m_subNetInfos = SortHelper.sortMap(subNetInfos, compator);
	}
}
