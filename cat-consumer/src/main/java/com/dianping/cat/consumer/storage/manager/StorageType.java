package com.dianping.cat.consumer.storage.manager;

import java.util.Arrays;
import java.util.List;

public enum StorageType {

	SQL("SQL", 1000, Arrays.asList("select", "delete", "insert", "update")),

	CACHE("Cache", 50, Arrays.asList("add", "get", "mGet", "remove")),

	RPC("RPC", 100, Arrays.asList("call"));

	private String m_name;

	private int m_threshold;

	private List<String> m_defaultMethods;

	public static StorageType findByName(String name, StorageType defaultStorage) {
		for (StorageType action : values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultStorage;
	}

	private StorageType(String name, int threshold, List<String> defaultMethods) {
		m_name = name;
		m_threshold = threshold;
		m_defaultMethods = defaultMethods;
	}

	public List<String> getDefaultMethods() {
		return m_defaultMethods;
	}

	public String getName() {
		return m_name;
	}

	public int getThreshold() {
		return m_threshold;
	}
}
