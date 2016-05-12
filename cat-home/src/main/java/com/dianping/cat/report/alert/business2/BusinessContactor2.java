package com.dianping.cat.report.alert.business2;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class BusinessContactor2 extends ProjectContactor  {

	public static final String ID = AlertType.Business2.getName();

	@Override
	public String getId() {
		return ID;
	}

}
