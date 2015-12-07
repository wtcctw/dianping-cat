package com.dianping.cat.report.alert.storage;

import com.dianping.cat.report.alert.spi.AlertType;

public class StorageRPCContactor extends AbstractStorageContactor {

	public static final String ID = AlertType.STORAGE_RPC.getName();

	@Override
	public String getId() {
		return ID;
	}

}
