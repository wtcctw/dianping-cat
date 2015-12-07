package com.dianping.cat.report.alert.exception;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.spi.receiver.ProjectContactor;

public class ExceptionContactor extends ProjectContactor {

	public static final String ID = AlertType.Exception.getName();

	@Override
	public String getId() {
		return ID;
	}

}
