package com.dianping.cat.config.web.js;

public enum Level {
	DEV(0, "dev"),

	INFO(1, "info"),

	WARN(2, "warn"),

	ERROR(3, "error");

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

}
