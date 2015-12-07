package com.dianping.cat.report.alert.database;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.spi.receiver.ProjectContactor;

public class DatabaseContactor extends ProjectContactor {

	public static final String ID = AlertType.DataBase.getName();

	@Override
	public String getId() {
		return ID;
	}

}
