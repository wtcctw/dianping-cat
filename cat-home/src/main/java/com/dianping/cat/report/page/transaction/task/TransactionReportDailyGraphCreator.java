package com.dianping.cat.report.page.transaction.task;

import java.util.Date;

import org.codehaus.plexus.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.Graph;
import com.dianping.cat.consumer.transaction.model.entity.Graph2;
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

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			Date from = transactionReport.getStartTime();

			m_day = (int) ((from.getTime() - m_start.getTime()) / TimeHelper.ONE_DAY);
			super.visitTransactionReport(transactionReport);
		}

		@Override
		public void visitMachine(Machine machine) {
			String ip = machine.getIp();
			m_currentMachine = m_report.findOrCreateMachine(ip);
			super.visitMachine(machine);
		}

		@Override
		public void visitType(TransactionType type) {
			type.getGraph2s().clear();

			String typeId = type.getId();
			m_currentType = m_currentMachine.findOrCreateType(typeId);
			Graph2 graph = m_currentType.findOrCreateGraph2(m_duration);

			Long[] count = parseToInteger(graph.getCount());
			Long[] fails = parseToInteger(graph.getFails());
			Double[] sum = parseToDouble(graph.getSum());
			Double[] avg = parseToDouble(graph.getAvg());

			count[m_day] = type.getTotalCount();
			fails[m_day] = type.getFailCount();
			sum[m_day] = ((int) (type.getSum() * 100)) / 100.0;
			avg[m_day] = ((int) (type.getAvg() * 100)) / 100.0;

			graph.setCount(StringUtils.join(count, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setAvg(StringUtils.join(avg, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setSum(StringUtils.join(sum, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setFails(StringUtils.join(fails, TransactionReportMerger.GRAPH_SPLITTER));

			super.visitType(type);
		}

		@Override
		public void visitName(TransactionName name) {
			name.getGraphs().clear();

			String nameId = name.getId();
			m_currentName = m_currentType.findOrCreateName(nameId);
			Graph graph = m_currentName.findOrCreateGraph(m_duration);

			Long[] count = parseToInteger(graph.getCount());
			Long[] fails = parseToInteger(graph.getFails());
			Double[] sum = parseToDouble(graph.getSum());
			Double[] avg = parseToDouble(graph.getAvg());

			count[m_day] = name.getTotalCount();
			fails[m_day] = name.getFailCount();
			sum[m_day] = ((int) (name.getSum() * 100)) / 100.0;
			avg[m_day] = ((int) (name.getAvg() * 100)) / 100.0;

			graph.setCount(StringUtils.join(count, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setAvg(StringUtils.join(avg, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setSum(StringUtils.join(sum, TransactionReportMerger.GRAPH_SPLITTER));
			graph.setFails(StringUtils.join(fails, TransactionReportMerger.GRAPH_SPLITTER));

			super.visitName(name);
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
	}

}
