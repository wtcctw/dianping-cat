package com.dianping.cat.report.alert.thirdParty;

import com.dianping.cat.report.alert.spi.AlertType;
import com.dianping.cat.report.alert.spi.receiver.ProjectContactor;

public class ThirdpartyContactor extends ProjectContactor {

	public static final String ID = AlertType.ThirdParty.getName();

	@Override
	public String getId() {
		return ID;
	}

}
