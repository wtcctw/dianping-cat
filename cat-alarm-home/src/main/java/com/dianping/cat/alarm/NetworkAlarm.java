package com.dianping.cat.alarm;

public class NetworkAlarm extends ServerAlarm {

	public static final String ID = "network";

	@Override
	public String getType() {
		return ID;
	}

}
