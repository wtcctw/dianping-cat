package com.dianping.cat.report.alert.storage;

import com.dianping.cat.report.alert.spi.AlertEntity;
import com.dianping.cat.report.alert.spi.AlertType;
import com.dianping.cat.report.alert.spi.decorator.Decorator;

public class StorageCacheDecorator extends Decorator {

	public static final String ID = AlertType.STORAGE_CACHE.getName();

	@Override
	public String generateContent(AlertEntity alert) {
		return alert.getContent();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT 缓存访问告警] [缓存集群: ").append(alert.getGroup()).append("] [监控项: ").append(alert.getMetric())
		      .append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
