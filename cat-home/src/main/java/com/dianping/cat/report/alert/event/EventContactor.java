package com.dianping.cat.report.alert.event;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.receiver.ProjectContactor;

public class EventContactor extends ProjectContactor {

	public static final String ID = AlertType.Event.getName();

	@Override
	public String getId() {
		return ID;
	}

}
