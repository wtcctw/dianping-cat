package com.dianping.cat.report.alert.spi.sender;

import com.dianping.cat.report.alert.spi.AlertMessageEntity;

public interface Sender {

	public String getId();

	public boolean send(AlertMessageEntity message);

}
