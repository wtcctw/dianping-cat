package com.dianping.cat.report.alert.storage.cache;

import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;


public class StorageCacheRuleConfigManager extends StorageRuleConfigManager {

	private static final String CONFIG_NAME = "storageCacheRule";

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

}
