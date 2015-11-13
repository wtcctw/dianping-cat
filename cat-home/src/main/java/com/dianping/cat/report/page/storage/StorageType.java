package com.dianping.cat.report.page.storage;

import java.util.Arrays;
import java.util.List;

public enum StorageType {
	SQL("SQL", Arrays.asList("select", "delete", "insert", "update")),

	CACHE("Cache", Arrays.asList("add", "get", "mGet", "remove")),

	RPC("RPC", Arrays.asList("call"));

	private String m_name;

	private List<String> m_defaultMethods;

	public static StorageType getByName(String name, StorageType defaultStorage) {
		for (StorageType action : StorageType.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultStorage;
	}

	private StorageType(String name, List<String> defaultMethods) {
		m_name = name;
		m_defaultMethods = defaultMethods;
	}

	public List<String> getDefaultMethods() {
		return m_defaultMethods;
	}

	public String getName() {
		return m_name;
	}

	public void setDefaultMethods(List<String> defaultMethods) {
		m_defaultMethods = defaultMethods;
	}
}
