package com.dianping.cat.alarm.server;

public class SystemAlarm extends AbstractServerAlarm {

	public static final String ID = "system";

	@Override
	public String getCategory() {
		return ID;
	}

}
