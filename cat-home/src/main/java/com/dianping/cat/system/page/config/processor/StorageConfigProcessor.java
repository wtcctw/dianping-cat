package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;
import com.dianping.cat.report.alert.storage.cache.StorageCacheRuleConfigManager;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCRuleConfigManager;
import com.dianping.cat.report.alert.storage.sql.StorageSQLRuleConfigManager;
import com.dianping.cat.report.page.storage.StorageType;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class StorageConfigProcessor extends BaseProcesser {

	@Inject
	private StorageSQLRuleConfigManager m_sqlConfigManager;

	@Inject
	private StorageRPCRuleConfigManager m_rpcConfigManager;

	@Inject
	private StorageCacheRuleConfigManager m_cacheConfigManager;

	public void process(Action action, Payload payload, Model model) {
		StorageRuleConfigManager configManager = buildConfigManager(payload);

		switch (action) {
		case STORAGE_RULE:
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		case STORAGE_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getRuleId(), configManager, model);
			break;
		case STORAGE_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(configManager, payload.getRuleId(), "", payload.getConfigs()));
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		case STORAGE_RULE_DELETE:
			model.setOpState(deleteRule(configManager, payload.getRuleId()));
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	private StorageRuleConfigManager buildConfigManager(Payload payload) {
		StorageType storageType = StorageType.getByName(payload.getType(), StorageType.SQL);

		switch (storageType) {
		case SQL:
			return m_sqlConfigManager;
		case CACHE:
			return m_cacheConfigManager;
		case RPC:
			return m_rpcConfigManager;
		}
		return null;
	}
}
