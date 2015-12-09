package com.dianping.cat.consumer.storage.manager;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.storage.DatabaseParser;
import com.dianping.cat.consumer.storage.DatabaseParser.Database;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class StorageSQLManager extends AbstractStorageManager {

	@Inject
	private DatabaseParser m_databaseParser;

	private static StorageType m_storage = StorageType.SQL;

	public final static String ID = m_storage.getName();

	@Override
	public StorageType getStorage() {
		return m_storage;
	}

	@Override
	public boolean isEligable(Transaction t) {
		return "SQL".equals(t.getType());
	}

	@Override
	public String queryId(Transaction t) {
		for (Message message : t.getChildren()) {
			if (message instanceof Event) {
				String type = message.getType();

				if ("SQL.Database".equals(type)) {
					Database database = m_databaseParser.queryDatabaseName(message.getName());

					if (database != null) {
						String databaseName = database.getName();

						return databaseName;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String queryIp(Transaction t) {
		for (Message message : t.getChildren()) {
			if (message instanceof Event) {
				String type = message.getType();

				if ("SQL.Database".equals(type)) {
					Database database = m_databaseParser.queryDatabaseName(message.getName());

					if (database != null) {
						String ip = database.getIp();

						return ip;
					}
				}
			}
		}
		return "default";
	}

	@Override
	public String queryMethod(Transaction t) {
		for (Message message : t.getChildren()) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL.Method")) {
					String method = message.getName().toLowerCase();

					return method;
				}
			}
		}
		return "select";
	}
}
