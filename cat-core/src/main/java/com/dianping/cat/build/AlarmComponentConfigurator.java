package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.ServerAlarmRuleServiceImpl;
import com.dianping.cat.core.alarm.ServerAlarmRuleDao;

class AlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(ServerAlarmRuleServiceImpl.class).req(ServerAlarmRuleDao.class));

		return all;
	}
}
