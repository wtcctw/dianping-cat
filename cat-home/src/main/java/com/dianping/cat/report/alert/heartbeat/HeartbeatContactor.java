package com.dianping.cat.report.alert.heartbeat;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.receiver.ProjectContactor;

public class HeartbeatContactor extends ProjectContactor {

	public static final String ID = AlertType.HeartBeat.getName();

	@Override
	public String getId() {
		return ID;
	}

}
