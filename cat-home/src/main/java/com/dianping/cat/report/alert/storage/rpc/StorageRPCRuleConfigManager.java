package com.dianping.cat.report.alert.storage.rpc;

import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;

public class StorageRPCRuleConfigManager extends StorageRuleConfigManager {

	private static final String CONFIG_NAME = "storageRPCRule";

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

}
