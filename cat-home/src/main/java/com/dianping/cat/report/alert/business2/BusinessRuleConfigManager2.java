package com.dianping.cat.report.alert.business2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.entity.SubCondition;
import com.dianping.cat.alarm.rule.transform.DefaultJsonParser;
import com.dianping.cat.alarm.rule.transform.DefaultSaxParser;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.config.BusinessConfigDao;
import com.dianping.cat.core.config.BusinessConfigEntity;
import com.dianping.cat.helper.MetricType;

@Named
public class BusinessRuleConfigManager2 implements Initializable {

	@Inject
	private BusinessConfigDao m_configDao;

	private static final String ALERT_CONFIG = "alert";

	private static final String TYPE = "type";

	private static final String SPLITTER = ":";

	Map<String, MonitorRules> m_rules = new ConcurrentHashMap<String, MonitorRules>();

	private Config buildDefaultConfig() {
		Config config = new Config();
		config.setStarttime("00:00");
		config.setEndtime("24:00");

		Condition condition = new Condition();
		SubCondition descPerSubcon = new SubCondition();
		SubCondition descValSubcon = new SubCondition();
		SubCondition flucPerSubcon = new SubCondition();

		descPerSubcon.setType("DescPer").setText("50");
		descValSubcon.setType("DescVal").setText("100");
		flucPerSubcon.setType("FluDescPer").setText("20");
		condition.addSubCondition(descPerSubcon).addSubCondition(descValSubcon).addSubCondition(flucPerSubcon);
		config.addCondition(condition);

		return config;
	}

	private String generateRuleId(String key, String type) {
		return new StringBuilder().append(key).append(SPLITTER).append(type).toString();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			List<BusinessConfig> configs = m_configDao.findByName(ALERT_CONFIG, BusinessConfigEntity.READSET_FULL);

			for (BusinessConfig config : configs) {
				try {
					String doamin = config.getDomain();
					MonitorRules rule = DefaultSaxParser.parse(config.getContent());
					m_rules.put(doamin, rule);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	public List<Config> queryConfigs(String domain, String key, MetricType type) {
		String typeName = type.getName();
		Rule rule = queryRule(domain, key, typeName);
		List<Config> configs = new ArrayList<Config>();

		if (rule != null && rule.getDynamicAttribute(TYPE).equals(typeName)) {
			configs.addAll(rule.getConfigs());
		}

		if (configs.size() == 0) {
			configs.add(buildDefaultConfig());
		}
		return configs;
	}

	public MonitorRules queryMonitorRules(String domain) {
		return m_rules.get(domain);
	}

	public Rule queryRule(String domain, String key, String type) {
		MonitorRules rule = m_rules.get(domain);

		if (rule != null) {
			return rule.findRule(generateRuleId(key, type));
		} else {
			return null;
		}
	}

	public void updateRule(String domain, String key, String configsStr, String type) {
		try {
			Rule rule = new Rule(generateRuleId(key, type));
			List<Config> configs = DefaultJsonParser.parseArray(Config.class, configsStr);

			for (Config config : configs) {
				rule.addConfig(config);
			}

			rule.setDynamicAttribute(TYPE, type);

			boolean isExist = true;
			MonitorRules domainRule = m_rules.get(domain);

			if (domainRule == null) {
				domainRule = new MonitorRules();
				m_rules.put(domain, domainRule);
				isExist = false;
			}

			domainRule.getRules().put(rule.getId(), rule);

			BusinessConfig proto = m_configDao.createLocal();
			proto.setDomain(domain);
			proto.setContent(domainRule.toString());
			proto.setName(ALERT_CONFIG);
			proto.setUpdatetime(new Date());

			if (isExist) {
				m_configDao.updateBaseConfigByDomain(proto, BusinessConfigEntity.UPDATESET_FULL);
			} else {
				m_configDao.insert(proto);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

	}
}
