package com.dianping.cat.consumer.storage.manager;

import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class StorageRPCManager extends AbstractStorageManager {

	private static StorageType m_storage = StorageType.RPC;

	public final static String ID = m_storage.getName();

	@Override
	public StorageType getStorage() {
		return m_storage;
	}

	@Override
	public boolean isEligable(Transaction t) {
		String type = t.getType();

		return "PigeonCall".equals(type) || "Call".equals(type);
	}

	@Override
	public String queryId(Transaction t) {
		for (Message message : t.getChildren()) {
			String type = message.getType();

			if ("PigeonCall.app".equals(type)) {
				String id = message.getName();

				if (id != null) {
					return id.trim();
				}
			}
		}
		return null;
	}

	@Override
	public String queryIp(Transaction t) {
		String ip = "default";

		for (Message message : t.getChildren()) {
			if (message instanceof Event) {
				String type = message.getType();

				if ("PigeonCall.server".equals(type)) {
					ip = message.getName();
					int index = ip.indexOf(':');

					if (index > -1) {
						ip = ip.substring(0, index);
					}
					return ip;
				}
			}
		}
		return ip;
	}

	@Override
	public String queryMethod(Transaction t) {
		return "call";
	}
}
