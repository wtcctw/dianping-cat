package com.dianping.cat.report.page.business;

public enum Type {
	Domain("domain"),

	Tag("tag");

	private String m_type;

	private Type(String type) {
		m_type = type;
	}

	public static Type getType(String str) {
		for (Type type : Type.values()) {
			if (type.m_type.equalsIgnoreCase(str)) {
				return type;
			}
		}

		throw new RuntimeException("Illegal business search type");
	}
}
