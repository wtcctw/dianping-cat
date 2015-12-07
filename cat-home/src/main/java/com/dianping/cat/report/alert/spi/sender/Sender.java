package com.dianping.cat.report.alert.spi.sender;


public interface Sender {

	public String getId();

	public boolean send(SendMessageEntity message);

}
