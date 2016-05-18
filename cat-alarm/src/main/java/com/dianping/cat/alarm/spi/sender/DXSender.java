package com.dianping.cat.alarm.spi.sender;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.dx.api.PushUtil;

public class DXSender extends AbstractSender implements Initializable {

	public static final String ID = AlertChannel.DX.getName();

	public static final String APP_KEY = "0513021Rv2124712";

	public static final String APP_SECRET = "415be4e6561846323d5fc48b83fa7a2c";

	public static final String SENDER = "cat2281@meituan.com";

	public static final String URL = "http://xm-in.sankuai.com/api";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(SendMessageEntity message) {
		try {
			List<String> receivers = message.getReceivers();
			String content = message.getTitle() + "\n" + message.getContent();

			PushUtil.push(content, receivers);
			return true;
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		PushUtil.init(APP_KEY, APP_SECRET, SENDER, URL);
	}
}
