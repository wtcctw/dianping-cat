package com.dianping.cat.report.alert.network;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.receiver.ProjectContactor;

public class NetworkContactor extends ProjectContactor {

	public static final String ID = AlertType.Network.getName();

	@Override
	public String getId() {
		return ID;
	}

}
