package com.dianping.cat.alarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.alarm.ServerAlarmRule;
import com.dianping.cat.core.alarm.ServerAlarmRuleDao;
import com.dianping.cat.core.alarm.ServerAlarmRuleEntity;

public class ServerAlarmRuleServiceImpl implements ServerAlarmRuleService, Initializable {

	@Inject
	private ServerAlarmRuleDao m_dao;

	private Map<String, List<ServerAlarmRule>> m_alarmRules = new HashMap<String, List<ServerAlarmRule>>();

	@Override
	public boolean delete(ServerAlarmRule rule) {
		try {
			m_dao.deleteByPK(rule);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	private List<ServerAlarmRule> findOrCreate(String category) {
		List<ServerAlarmRule> rules = m_alarmRules.get(category);

		if (rules == null) {
			rules = new ArrayList<ServerAlarmRule>();

			m_alarmRules.put(category, rules);
		}
		return rules;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			List<ServerAlarmRule> entities = m_dao.findAll(ServerAlarmRuleEntity.READSET_FULL);

			for (ServerAlarmRule entity : entities) {
				String category = entity.getCategory();
				List<ServerAlarmRule> rules = findOrCreate(category);

				rules.add(entity);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	@Override
	public boolean insert(ServerAlarmRule rule) {
		try {
			int count = m_dao.insert(rule);

			if (count > 0) {
				List<ServerAlarmRule> rules = findOrCreate(rule.getCategory());

				rules.add(rule);
				return true;
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	@Override
	public Map<String, List<ServerAlarmRule>> queryAllRules() {
		return m_alarmRules;
	}

	@Override
	public ServerAlarmRule queryById(int id) {
		try {
			return m_dao.findByPK(id, ServerAlarmRuleEntity.READSET_FULL);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public void refresh() {
		// TODO
	}

	@Override
	public boolean update(ServerAlarmRule rule) {
		try {
			int count = m_dao.updateByPK(rule, ServerAlarmRuleEntity.UPDATESET_FULL);

			if (count > 0) {
				List<ServerAlarmRule> rules = findOrCreate(rule.getCategory());

				for (ServerAlarmRule r : rules) {
					if (r.getId() == rule.getId()) {
						r.setCategory(rule.getCategory());
						r.setMeasurement(rule.getMeasurement());
						r.setContent(rule.getContent());
						r.setEndPoint(rule.getEndPoint());
						r.setType(rule.getType());

						break;
					}
				}
			}
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

}
