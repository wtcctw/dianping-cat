package com.dianping.cat.report.page.transaction.transform;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Cat;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.transaction.Model;
import com.dianping.cat.report.page.transaction.Payload;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.Graph;
import com.dianping.cat.consumer.transaction.model.entity.Graph2;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.site.lookup.util.StringUtils;

public class TransactionTrendGraphBuilder {

	private boolean m_isOld = false;

	private TransactionReportService m_reportService;

	private int m_duration;

	public static final String COUNT = "count";

	public static final String FAIL = "fail";

	public static final String AVG = "avg";

	public TransactionTrendGraphBuilder(TransactionReportService reportService) {
		m_reportService = reportService;
	}

	public boolean buildTrendGraph(Model model, Payload payload) {
		String name = payload.getName();
		String domain = model.getDomain();
		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();
		String reportType = payload.getReportType();
		String ip = model.getIpAddress();
		String type = payload.getType();
		String display = name != null ? name : type;

		Map<String, double[]> data = getDatas(start, end, domain, ip, type, name);

		if (!m_isOld) {
			ReportType queryType = ReportType.findByName(reportType);
			long step = queryType.getStep() * m_duration;
			int size = (int) ((start.getTime() - end.getTime()) / step);

			LineChart fail = buildLineChart(start, end, step, size);
			LineChart count = buildLineChart(start, end, step, size);
			LineChart avg = buildLineChart(start, end, step, size);

			fail.setTitle(display + queryType.getFailTitle());
			count.setTitle(display + queryType.getSumTitle());
			avg.setTitle(display + queryType.getResponseTimeTitle());

			fail.addValue(data.get(FAIL));
			count.addValue(data.get(COUNT));
			avg.addValue(data.get(AVG));

			model.setErrorTrend(fail.getJsonString());
			model.setHitTrend(count.getJsonString());
			model.setResponseTrend(avg.getJsonString());
		}
		return m_isOld;
	}

	private LineChart buildLineChart(Date start, Date end, long step, int size) {
		LineChart item = new LineChart();

		item.setStart(start);
		item.setSize(size);
		item.setStep(step);
		item.setSubTitles(buildSubTitles(start, end));
		return item;
	}

	private List<String> buildSubTitles(Date start, Date end) {
		List<String> subTitles = new ArrayList<String>();

		subTitles.add(buildSubTitle(start, end));
		return subTitles;
	}

	private String buildSubTitle(Date start, Date end) {
		SimpleDateFormat from = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat to = new SimpleDateFormat("MM-dd");
		StringBuilder sb = new StringBuilder();

		sb.append(from.format(start)).append("~").append(to.format(end));
		return sb.toString();
	}

	private Map<String, double[]> getDatas(Date start, Date end, String domain, String ip, String type, String name) {
		TransactionReport report = m_reportService.queryReport(domain, start, end);
		report = new TransactionMergeHelper().mergeAllMachines(report, ip);
		TransactionReportVisitor visitor = new TransactionReportVisitor(ip, type, name);
		visitor.visitTransactionReport(report);

		return visitor.getDatas();
	}

	public enum ReportType {

		DAY("day", TimeHelper.ONE_MINUTE) {

			@Override
			String getFailTitle() {
				return " Error (count/min)";
			}

			@Override
			String getSumTitle() {
				return " Hits (count/min)";
			}

			@Override
			String getResponseTimeTitle() {
				return " Response Time (ms)";
			}

		},

		WEEK("week", TimeHelper.ONE_DAY) {

			@Override
			String getFailTitle() {
				return " Error (count/day)";
			}

			@Override
			String getSumTitle() {
				return " Hits (count/day)";
			}

			@Override
			String getResponseTimeTitle() {
				return " Response Time (ms)";
			}
		},

		MONTH("month", TimeHelper.ONE_DAY) {

			@Override
			String getFailTitle() {
				return " Error (count/day)";
			}

			@Override
			String getSumTitle() {
				return " Hits (count/day)";
			}

			@Override
			String getResponseTimeTitle() {
				return " Response Time (ms)";
			}
		};

		private String m_name;

		private long m_step;

		private ReportType(String name, long step) {
			m_name = name;
			m_step = step;
		}

		public String getName() {
			return m_name;
		}

		public long getStep() {
			return m_step;
		}

		public static ReportType findByName(String name) {
			for (ReportType type : ReportType.values()) {
				if (type.getName().equalsIgnoreCase(name)) {
					return type;
				}
			}
			throw new RuntimeException("Error graph query type");
		}

		abstract String getFailTitle();

		abstract String getSumTitle();

		abstract String getResponseTimeTitle();
	}

	public class TransactionReportVisitor extends BaseVisitor {

		private String m_ip;

		private String m_type;

		private String m_name;

		private boolean m_isThisIp;

		private boolean m_isThisType;

		private Map<String, double[]> m_datas;

		public TransactionReportVisitor(String ip, String type, String name) {
			m_ip = ip;
			m_type = type;
			m_name = name;
		}

		public Map<String, double[]> getDatas() {
			return m_datas;
		}

		@Override
		public void visitMachine(Machine machine) {
			if (machine.getIp().equalsIgnoreCase(m_ip)) {
				m_isThisIp = true;
			} else {
				m_isThisIp = false;
			}
			super.visitMachine(machine);
		}

		@Override
		public void visitType(TransactionType type) {
			String id = type.getId();

			if (id.equalsIgnoreCase(m_type)) {
				m_isThisType = true;
			} else {
				m_isThisType = false;
			}

			if (type.getGraph2s() == null || type.getGraph2s().size() == 0) {
				m_isOld = true;
			} else {
				if (m_isThisIp && m_isThisType && StringUtils.isEmpty(m_name)) {
					for (Graph2 graph : type.getGraph2s().values()) {
						m_duration = graph.getDuration();
						m_datas = new HashMap<String, double[]>();
						m_datas.put(AVG, parseToDouble(graph.getAvg()));
						m_datas.put(COUNT, parseToDouble(graph.getCount()));
						m_datas.put(FAIL, parseToDouble(graph.getFails()));
						break;
					}
				}
			}
			super.visitType(type);
		}

		@Override
		public void visitName(TransactionName name) {
			String id = name.getId();

			if (m_isThisIp && m_isThisType && StringUtils.isNotEmpty(id) && id.equalsIgnoreCase(m_name)) {
				for (Graph graph : name.getGraphs().values()) {
					m_duration = graph.getDuration();
					m_datas = new HashMap<String, double[]>();
					m_datas.put(AVG, parseToDouble(graph.getAvg()));
					m_datas.put(COUNT, parseToDouble(graph.getCount()));
					m_datas.put(FAIL, parseToDouble(graph.getFails()));
					break;
				}
			}
			super.visitName(name);
		}

		private double[] parseToDouble(String str) {
			if (StringUtils.isNotEmpty(str)) {
				String[] strs = str.split(TransactionReportMerger.GRAPH_SPLITTER);
				double[] result = new double[strs.length];

				for (int i = 0; i < strs.length; i++) {
					try {
						result[i] = Double.parseDouble(strs[i]);
					} catch (Exception e) {
						result[i] = 0.0;
						Cat.logError(e);
					}
				}
				return result;

			} else {
				return null;
			}
		}
	}

}
