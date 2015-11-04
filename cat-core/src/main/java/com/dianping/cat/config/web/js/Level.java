package com.dianping.cat.config.web.js;

import java.util.Arrays;
import java.util.List;

public enum Level {
	DEV(0, "DEV"),

	INFO(1, "INFO"),

	WARN(2, "WARN"),

	ERROR(3, "ERROR");

	private static List<String> m_levels = Arrays.asList("DEV", "INFO", "WARN", "ERROR");

	private int m_code;

	private String m_name;

	private Level(int code, String name) {
		m_code = code;
		m_name = name;
	}

	public int getCode() {
		return m_code;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public static int getCodeByName(String name) {
		for (Level level : Level.values()) {
			if (level.getName().equals(name)) {
				return level.getCode();
			}
		}
		throw new RuntimeException("Invalid level");
	}
	
	public static String getNameByCode(int code) {
		for (Level level : Level.values()) {
			if (level.getCode() == code) {
				return level.getName();
			}
		}
		throw new RuntimeException("Invalid level");
	}

	public static List<String> getLevels() {
		return m_levels;
	}

}
