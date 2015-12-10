package com.dianping.cat.report.alert.storage.sql;

import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;

public class StorageSQLRuleConfigManager extends StorageRuleConfigManager {

	private static final String CONFIG_NAME = "storageSQLRule";

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

}
