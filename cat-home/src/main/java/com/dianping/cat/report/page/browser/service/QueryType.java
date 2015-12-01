package com.dianping.cat.report.page.browser.service;

public enum QueryType {
	SUCCESS("success", "成功率（%/5分钟"),
	
	REQUEST("request", "请求数（个/5分钟"),
	
	DELAY("delay", "延时平均值（毫秒/5分钟"),
	
	REQUEST_PACKAGE("requestByte", "平均发包大小(byte)"),
	
	RESPONSE_PACKAGE("responseByte", "平均回包大小(byte)");
	
	private String m_type;
	
	private String m_title;
	
	private QueryType(String type, String title) {
		m_type = type;
		m_title = title;
	}

	public String getType() {
		return m_type;
	}

	public String getTitle() {
		return m_title;
	}
	
	public static QueryType findByType(String type) {
		for(QueryType queryType : QueryType.values()) {
			if(queryType.getType().equals(type)) {
				return queryType;
			}
		}
		return REQUEST;
	}

}
