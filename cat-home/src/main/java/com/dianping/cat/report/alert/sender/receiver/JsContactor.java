package com.dianping.cat.report.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.home.js.entity.ExceptionLimit;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.browser.JsRuleConfigManager;
import com.dianping.cat.report.alert.sender.config.AlertConfigManager;

public class JsContactor extends DefaultContactor implements Contactor {

	public static final String ID = AlertType.JS.getName();

	@Inject
	protected AlertConfigManager m_alertConfigManager;

	@Inject
	protected JsRuleConfigManager m_jsRuleConfigManager;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			String[] domainAndLevel = id.split(JsRuleConfigManager.SPLITTER);

			if (domainAndLevel.length > 1) {
				ExceptionLimit rule = m_jsRuleConfigManager.queryExceptionLimit(domainAndLevel[0], domainAndLevel[1]);

				if (rule != null) {
					mailReceivers.addAll(split(rule.getEmails()));
				}
			}

			return mailReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));

			return weixinReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));

			return smsReceivers;
		}
	}

}
