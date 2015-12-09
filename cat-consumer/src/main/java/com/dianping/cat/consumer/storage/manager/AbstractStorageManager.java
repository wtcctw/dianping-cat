package com.dianping.cat.consumer.storage.manager;


public abstract class AbstractStorageManager implements StorageManager {

	@Override
	public String queryReportId(String id) {
		return id + "-" + getStorage().getName();
	}

	@Override
	public int queryThreshold() {
		return getStorage().getThreshold();
	}

}
