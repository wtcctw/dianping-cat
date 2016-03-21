package com.dianping.cat.report.page.business.task;

public class BusinessKeyHelper {
	
	public final String SPLITTER = ":";

	public String getType(String key) {
		int index = key.lastIndexOf(SPLITTER);
		return key.substring(index + 1);
	}

	public String getBusinessItemId(String key) {
		int index = key.indexOf(SPLITTER);
		return key.substring(0, index);
	}

	public String getDomain(String key) {
		int first = key.indexOf(SPLITTER);
		int last = key.lastIndexOf(SPLITTER);
		return key.substring(first, last - 1);
	}

	public String generateKey(String id, String domain, String type) {
		StringBuilder sb = new StringBuilder();

		sb.append(id);
		sb.append(SPLITTER);
		sb.append(domain);
		sb.append(SPLITTER);
		sb.append(type);

		return sb.toString();
	}
}
