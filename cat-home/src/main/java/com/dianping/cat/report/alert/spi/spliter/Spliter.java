package com.dianping.cat.report.alert.spi.spliter;

public interface Spliter {

	public String process(String content);

	public String getID();

}
