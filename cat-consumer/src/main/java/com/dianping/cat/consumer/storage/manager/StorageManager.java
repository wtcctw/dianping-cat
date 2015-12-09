package com.dianping.cat.consumer.storage.manager;

import com.dianping.cat.message.Transaction;

public interface StorageManager {

	public StorageType getStorage();

	public boolean isEligable(Transaction t);

	public String queryId(Transaction t);

	public String queryIp(Transaction t);

	public String queryMethod(Transaction t);

	public String queryReportId(String id);

	public int queryThreshold();

}
