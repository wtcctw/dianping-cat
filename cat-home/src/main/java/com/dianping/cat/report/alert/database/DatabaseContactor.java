package com.dianping.cat.report.alert.database;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class DatabaseContactor extends ProjectContactor {

	public static final String ID = AlertType.DataBase.getName();

	@Override
	public String getId() {
		return ID;
	}

}
