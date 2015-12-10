package com.dianping.cat.report.alert.spi.spliter;

import java.util.regex.Pattern;

import com.dianping.cat.report.alert.spi.AlertChannel;

public class WeixinSpliter implements Spliter {

	public static final String ID = AlertChannel.WEIXIN.getName();

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String process(String content) {
		String weixinContent = content.replaceAll("<br/>", "\n");
		return Pattern.compile("<div.*(?=</div>)</div>", Pattern.DOTALL).matcher(weixinContent).replaceAll("");
	}

}
