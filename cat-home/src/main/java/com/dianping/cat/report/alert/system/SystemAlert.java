package com.dianping.cat.report.alert.system;

import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.alert.spi.BaseAlert;
import com.dianping.cat.report.alert.spi.config.BaseRuleConfigManager;
import com.dianping.cat.alarm.spi.AlertType;

@Named
public class SystemAlert extends BaseAlert {

	@Inject
	protected SystemRuleConfigManager m_ruleConfigManager;

	@Override
	public String getName() {
		return AlertType.System.getName();
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		return m_productLineConfigManager.querySystemProductLines();
	}
}