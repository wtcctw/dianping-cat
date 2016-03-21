package com.dianping.cat.report.alert.network;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class NetworkContactor extends ProjectContactor {

	public static final String ID = AlertType.Network.getName();

	@Override
	public String getId() {
		return ID;
	}

}
