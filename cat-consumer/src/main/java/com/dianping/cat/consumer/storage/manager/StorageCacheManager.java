package com.dianping.cat.consumer.storage.manager;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class StorageCacheManager extends AbstractStorageManager {

	private static StorageType m_storage = StorageType.CACHE;

	public final static String ID = m_storage.getName();

	@Override
	public StorageType getStorage() {
		return m_storage;
	}

	@Override
	public boolean isEligable(Transaction t) {
		String type = t.getType();

		return type != null && (type.startsWith("Cache.memcached") || type.startsWith("Squirrel."));
	}

	@Override
	public String queryId(Transaction t) {
		String type = t.getType();
		int index = type.indexOf(".");

		if (index > -1) {
			type = type.substring(index + 1);
		}
		return type;
	}

	@Override
	public String queryIp(Transaction t) {
		String ip = "default";

		for (Message message : t.getChildren()) {
			if (message instanceof Event) {
				String type = message.getType();

				if ("Cache.memcached.server".equals(type) || "Squirrel.server".equals(type)) {
					ip = message.getName();
					int index = ip.indexOf(":");

					if (index > -1) {
						ip = ip.substring(0, index);

						return ip;
					}
				}
			}
		}
		return ip;
	}

	@Override
	public String queryMethod(Transaction t) {
		String name = t.getName();
		String method = name.substring(name.lastIndexOf(":") + 1);

		return method;
	}

}
