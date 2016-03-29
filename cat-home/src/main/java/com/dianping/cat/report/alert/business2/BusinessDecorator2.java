package com.dianping.cat.report.alert.business2;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.decorator.ProjectDecorator;

public class BusinessDecorator2 extends ProjectDecorator {

	public static final String ID = AlertType.Business2.getName();

	@Override
	public String generateContent(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append(alert.getContent());
		sb.append(buildContactInfo(alert.getDomain()));

		return sb.toString();
	}
	
	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[业务告警] [应用名 ").append(alert.getDomain()).append("]");
		sb.append("[业务指标 ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
