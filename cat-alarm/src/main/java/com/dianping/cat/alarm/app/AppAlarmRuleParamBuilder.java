package com.dianping.cat.alarm.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.app.MobileConstants;
import com.dianping.cat.configuration.mobile.entity.Item;

@Named
public class AppAlarmRuleParamBuilder {

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public static final String COMMAND = "command";

	public static final String COMMAND_NAME = "commandName";

	public static final String CODE = "code";

	public static final String NETWORK = MobileConstants.NETWORK;

	public static final String VERSION = MobileConstants.VERSION;

	public static final String CONNECT_TYPE = MobileConstants.CONNECT_TYPE;

	public static final String PLATFORM = MobileConstants.PLATFORM;

	public static final String CITY = MobileConstants.CITY;

	public static final String OPERATOR = MobileConstants.OPERATOR;

	public static final String METRIC = "metric";

	public List<AppAlarmRuleParam> build(Rule rule) {
		List<AppAlarmRuleParam> results = new ArrayList<AppAlarmRuleParam>();
		Map<String, String> dynamicAttrs = rule.getDynamicAttributes();
		List<String> keys = new ArrayList<String>();

		for (Entry<String, String> entry : dynamicAttrs.entrySet()) {
			if (entry.getValue().equals("*")) {
				keys.add(entry.getKey());
			}
		}

		if (keys.isEmpty()) {
			results.add(buildParam(dynamicAttrs));
		} else {
			for (String key : keys) {
				try {
					Map<String, String> attributes = new LinkedHashMap<String, String>(dynamicAttrs);
					List<AppAlarmRuleParam> params = buildParams(keys, key, attributes);

					results.addAll(params);
				} catch (Exception e) {
					Cat.logError(rule.toString(), e);
				}
			}
		}
		return results;
	}

	private AppAlarmRuleParam buildParam(Map<String, String> attrs) throws NumberFormatException {
		int command = Integer.parseInt(attrs.get(COMMAND));
		String commandName = attrs.get(COMMAND_NAME);
		int code = Integer.parseInt(attrs.get(CODE));
		int network = Integer.parseInt(attrs.get(NETWORK));
		int version = Integer.parseInt(attrs.get(VERSION));
		int connectType = Integer.parseInt(attrs.get(CONNECT_TYPE));
		int platform = Integer.parseInt(attrs.get(PLATFORM));
		int city = Integer.parseInt(attrs.get(CITY));
		int operator = Integer.parseInt(attrs.get(OPERATOR));
		String metric = attrs.get(METRIC);

		AppAlarmRuleParam param = new AppAlarmRuleParam();

		param.setCommand(command);
		param.setCommandName(commandName);
		param.setCode(code);
		param.setNetwork(network);
		param.setVersion(version);
		param.setConnectType(connectType);
		param.setPlatform(platform);
		param.setCity(city);
		param.setOperator(operator);
		param.setMetric(metric);
		return param;
	}

	private List<AppAlarmRuleParam> buildParams(List<String> keys, String key, Map<String, String> attributes) {
		List<AppAlarmRuleParam> results = new ArrayList<AppAlarmRuleParam>();

		for (String k : keys) {
			if (!k.equals(key)) {
				attributes.put(k, "-1");
			}
		}

		for (Entry<Integer, Item> entry : m_mobileConfigManager.queryConstantItem(key).entrySet()) {
			attributes.put(key, String.valueOf(entry.getKey()));
			AppAlarmRuleParam param = buildParam(attributes);

			results.add(param);
		}
		return results;
	}

	public void setMobileConfigManager(MobileConfigManager manager) {
		m_mobileConfigManager = manager;
	}
}
