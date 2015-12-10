package com.dianping.cat.report.alert.network;

import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;
import com.dianping.cat.report.alert.spi.AlertType;
import com.dianping.cat.report.alert.spi.BaseAlert;

public class NetworkAlert extends BaseAlert {

	@Inject
	protected NetworkRuleConfigManager m_ruleConfigManager;

	@Override
	public String getName() {
		return AlertType.Network.getName();
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		return m_productLineConfigManager.queryNetworkProductLines();
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}

}