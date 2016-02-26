package com.dianping.cat.alarm.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ArrayUtils;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.server.entity.Condition;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.metric.QueryParameter;

public class AlarmTask implements Task {

	@Inject
	private MetricService m_metricService;

	@Inject
	private ServerDataChecker m_dataChecker;

	@Inject
	protected AlertManager m_sendManager;

	private List<AlarmParameter> m_paramters = new ArrayList<AlarmParameter>();

	private String m_category;

	private String m_alarmId;

	public void addParameter(AlarmParameter parameter) {
		m_paramters.add(parameter);
	}

	public String getAlarmId() {
		return m_alarmId;
	}

	public String getCategory() {
		return m_category;
	}

	@Override
	public String getName() {
		return "alarm-task";
	}

	public List<AlarmParameter> getParamters() {
		return m_paramters;
	}

	@Override
	public void run() {
		for (AlarmParameter parameter : m_paramters) {
			List<Condition> conditions = parameter.getCondition();

			for (QueryParameter query : parameter.getQueries()) {
				Map<Long, Double> results = m_metricService.queryFillNone(query);

				if (!results.isEmpty()) {
					SortHelper.sortMap(results, new Comparator<Entry<Long, Double>>() {
						@Override
						public int compare(Entry<Long, Double> o1, Entry<Long, Double> o2) {
							if (o1.getKey() > o2.getKey()) {
								return 1;
							} else if (o1.getKey() < o2.getKey()) {
								return -1;
							} else {
								return 0;
							}
						}
					});
					Double[] values = new Double[results.size()];

					results.values().toArray(values);

					List<DataCheckEntity> alertResults = m_dataChecker.checkData(ArrayUtils.toPrimitive(values), conditions);

					for (DataCheckEntity alertResult : alertResults) {
						AlertEntity entity = new AlertEntity();

						entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
						      .setLevel(alertResult.getAlertLevel());
						entity.setMetric(query.getMeasurement()).setType(m_alarmId).setGroup(query.getTags());
						m_sendManager.addAlert(entity);
					}
				}
			}
		}
	}

	public void setAlarmId(String alarmId) {
		m_alarmId = alarmId;
	}

	public void setCategory(String category) {
		m_category = category;
	}

	@Override
	public void shutdown() {
	}

	public static class AlarmParameter {

		private List<QueryParameter> m_queries = new ArrayList<QueryParameter>();

		private List<Condition> m_conditions = new ArrayList<Condition>();

		public AlarmParameter(List<Condition> conditions) {
			m_conditions = conditions;
		}

		public void addParameter(QueryParameter query) {
			m_queries.add(query);
		}

		public List<Condition> getCondition() {
			return m_conditions;
		}

		public List<QueryParameter> getQueries() {
			return m_queries;
		}
	}

}
