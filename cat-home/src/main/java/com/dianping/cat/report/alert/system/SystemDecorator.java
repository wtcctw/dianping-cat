package com.dianping.cat.report.alert.system;

import com.dianping.cat.report.alert.spi.AlertEntity;
import com.dianping.cat.report.alert.spi.AlertType;
import com.dianping.cat.report.alert.spi.decorator.Decorator;

public class SystemDecorator extends Decorator {

	public static final String ID = AlertType.System.getName();

	@Override
   public String generateContent(AlertEntity alert) {
	   return alert.getContent();
   }

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[系统告警] [产品线 ").append(alert.getGroup()).append("]");
		sb.append("[系统指标 ").append(alert.getMetric()).append("]");
		
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}
}
