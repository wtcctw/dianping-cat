package com.dianping.cat.alarm.server;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class ServerContactor extends ProjectContactor {

	public static final String ID = AlertType.SERVER.getName();

	@Override
	public String getId() {
		return ID;
	}

}
