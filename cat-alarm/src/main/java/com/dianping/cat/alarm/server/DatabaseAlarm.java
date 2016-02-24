package com.dianping.cat.alarm.server;

public class DatabaseAlarm extends AbstractServerAlarm {

	public static final String ID = "database";

	@Override
	public String getCategory() {
		return ID;
	}

}
