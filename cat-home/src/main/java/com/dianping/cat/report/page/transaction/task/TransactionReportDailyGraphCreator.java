package com.dianping.cat.report.page.transaction.task;

import java.util.Date;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.GraphTrend;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;

public class TransactionReportDailyGraphCreator {

	private TransactionReport m_report;

	private int m_length;

	private int m_duration = 1;

	private Date m_start;

	public TransactionReportDailyGraphCreator(TransactionReport transactionReport, int length, Date start) {
		m_report = transactionReport;
		m_length = length;
		m_start = start;
	}

	public void creatorGraph(TransactionReport from) {
		new TransactionReportVisitor().visitTransactionReport(from);
	}

	class TransactionReportVisitor extends BaseVisitor {

		private Machine m_currentMachine;

		private TransactionType m_currentType;

		private TransactionName m_currentName;

		private int m_day;

		private void buildGraphTrend(GraphTrend graph, long totalCount, long failCount, double sumValue, double avgValue) {
			Long[] count = parseToInteger(graph.getCount());
			Long[] fails = parseToInteger(graph.getFails());
			Double[] sum = parseToDouble(graph.getSum());
			Double[] avg = parseToDouble(graph.getAvg());

			count[m_day] = totalCount;
			fails[m_day] = failCount;
			sum[m_day] = ((int) (sumValue * 100)) / 100.0;
			avg[m_day] = ((int) (avgValue * 100)) / 100.0;

			graph.setCount(StringUtils.join(count, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setAvg(StringUtils.join(avg, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setSum(StringUtils.join(sum, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setFails(StringUtils.join(fails, TransactionReportMerger.GRAPH_SPLITTER));
		}

		private Double[] parseToDouble(String str) {
			Double[] result = new Double[m_length];

			if (StringUtils.isNotBlank(str)) {
				String[] strs = str.split(TransactionReportMerger.GRAPH_SPLITTER);

				for (int i = 0; i < m_length; i++) {
					try {
						result[i] = Double.parseDouble(strs[i]);
					} catch (Exception e) {
						result[i] = 0.0;
						Cat.logError(e);
					}
				}
			} else {
				for (int i = 0; i < m_length; i++) {
					result[i] = 0.0;
				}
			}
			return result;
		}

		private Long[] parseToInteger(String str) {
			Long[] result = new Long[m_length];

			if (StringUtils.isNotBlank(str)) {
				String[] strs = str.split(TransactionReportMerger.GRAPH_SPLITTER);

				for (int i = 0; i < m_length; i++) {
					try {
						result[i] = Long.parseLong(strs[i]);
					} catch (Exception e) {
						result[i] = 0L;
						Cat.logError(e);
					}
				}
			} else {
				for (int i = 0; i < m_length; i++) {
					result[i] = 0L;
				}
			}
			return result;
		}

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();
			m_currentMachine = m_report.findOrCreateMachine(ip);
			super.visitMachine(machine);
		}

		@Override
		public void visitName(TransactionName name) {
			name.setGraphTrend(null);
			
			String nameId = name.getId();
			m_currentName = m_currentType.findOrCreateName(nameId);

			GraphTrend graph = m_currentName.getGraphTrend();

			if (graph == null) {
				graph = new GraphTrend();
				graph.setDuration(m_duration);
				m_currentName.setGraphTrend(graph);
			}
			buildGraphTrend(graph, name.getTotalCount(), name.getFailCount(), name.getSum(), name.getAvg());

			super.visitName(name);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			Date from = transactionReport.getStartTime();

			m_day = (int) ((from.getTime() - m_start.getTime()) / TimeHelper.ONE_DAY);
			super.visitTransactionReport(transactionReport);
		}

		@Override
		public void visitType(TransactionType type) {
			type.setGraphTrend(null);
			
			String typeId = type.getId();
			m_currentType = m_currentMachine.findOrCreateType(typeId);

			GraphTrend graph = m_currentType.getGraphTrend();

			if (graph == null) {
				graph = new GraphTrend();
				graph.setDuration(m_duration);
				m_currentType.setGraphTrend(graph);
			}
			buildGraphTrend(graph, type.getTotalCount(), type.getFailCount(), type.getSum(), type.getAvg());

			super.visitType(type);
		}
	}

}
