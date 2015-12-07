package com.dianping.cat.report.alert.network;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.spi.AlertEntity;
import com.dianping.cat.report.alert.spi.decorator.Decorator;

public class NetworkDecorator extends Decorator {

	public static final String ID = AlertType.Network.getName();

	@Override
   public String generateContent(AlertEntity alert) {
	   return alert.getContent();
   }

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[网络告警] [设备 ").append(alert.getGroup()).append("]");
		sb.append("[网络指标 ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
