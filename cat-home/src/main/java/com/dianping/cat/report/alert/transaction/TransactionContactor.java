package com.dianping.cat.report.alert.transaction;

import com.dianping.cat.report.alert.spi.AlertType;
import com.dianping.cat.report.alert.spi.receiver.ProjectContactor;

public class TransactionContactor extends ProjectContactor {

	public static final String ID = AlertType.Transaction.getName();

	@Override
	public String getId() {
		return ID;
	}

}
