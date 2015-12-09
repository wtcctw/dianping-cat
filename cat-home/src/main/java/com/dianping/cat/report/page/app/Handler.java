package com.dianping.cat.report.page.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.alert.app.AppRuleConfigManager;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChartDetailInfo;
import com.dianping.cat.report.graph.PieChartDetailInfo.Item;
import com.dianping.cat.report.page.app.display.AppCommandsSorter;
import com.dianping.cat.report.page.app.display.AppConnectionGraphCreator;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppGraphCreator;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;
import com.dianping.cat.report.page.app.display.CodeDisplayVisitor;
import com.dianping.cat.report.page.app.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.app.display.CrashLogDisplayInfo;
import com.dianping.cat.report.page.app.display.DisplayCommands;
import com.dianping.cat.report.page.app.processor.CrashLogProcessor;
import com.dianping.cat.report.page.app.service.AppConnectionService;
import com.dianping.cat.report.page.app.service.AppDataField;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.AppReportService;
import com.dianping.cat.report.page.app.service.AppSpeedService;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;
import com.dianping.cat.report.page.app.service.CrashLogQueryEntity;
import com.dianping.cat.report.page.app.service.CrashLogService;
import com.dianping.cat.report.page.app.service.SpeedQueryEntity;
import com.dianping.cat.service.ProjectService;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppSpeedConfigManager m_appSpeedConfigManager;

	@Inject
	private AppGraphCreator m_appGraphCreator;

	@Inject
	private AppDataService m_appDataService;

	@Inject
	private AppSpeedService m_appSpeedService;

	@Inject
	private AppConnectionGraphCreator m_appConnectionGraphCreator;

	@Inject
	private AppConnectionService m_appConnectionService;

	@Inject
	private AppRuleConfigManager m_appRuleConfigManager;

	@Inject
	private CrashLogProcessor m_crashLogProcessor;

	@Inject
	private CrashLogService m_crashLogService;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private AppReportService m_appReportService;

	@Inject
	private ProjectService m_projectService;

	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	private List<AppDataDetail> buildAppDataDetails(Payload payload) {
		List<AppDataDetail> appDetails = new ArrayList<AppDataDetail>();

		try {
			appDetails = m_appDataService.buildAppDataDetailInfos(payload.getQueryEntity1(), payload.getGroupByField());
			Collections.sort(appDetails, new LineChartDetailInfoComparator(payload.getSort()));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return appDetails;
	}

	public List<String> buildCodeDistributions(DisplayCommands displayCommands) {
		List<String> ids = new LinkedList<String>();
		Set<String> orgIds = displayCommands.findOrCreateCommand(AppConfigManager.ALL_COMMAND_ID).getCodes().keySet();

		for (String id : orgIds) {
			if (id.contains("XX") || CodeDisplayVisitor.STANDALONES.contains(Integer.valueOf(id))) {
				ids.add(id);
			}
		}
		Collections.sort(ids, new CodeDistributionComparator());
		return ids;
	}

	private AppDataDetail buildComparisonInfo(CommandQueryEntity entity) {
		AppDataDetail appDetail = null;

		try {
			List<AppDataDetail> appDetails = m_appDataService.buildAppDataDetailInfos(entity, AppDataField.CODE);

			if (appDetails.size() >= 1) {
				appDetail = appDetails.iterator().next();
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return appDetail;
	}

	private Map<String, AppDataDetail> buildComparisonInfo(Payload payload) {
		CommandQueryEntity currentEntity = payload.getQueryEntity1();
		CommandQueryEntity comparisonEntity = payload.getQueryEntity2();
		Map<String, AppDataDetail> result = new HashMap<String, AppDataDetail>();

		if (currentEntity != null) {
			AppDataDetail detail = buildComparisonInfo(currentEntity);

			if (detail != null) {
				result.put(Constants.CURRENT_STR, detail);
			}
		}

		if (comparisonEntity != null) {
			AppDataDetail detail = buildComparisonInfo(comparisonEntity);

			if (detail != null) {
				result.put(Constants.COMPARISION_STR, detail);
			}
		}

		return result;
	}

	private Pair<LineChart, List<AppDataDetail>> buildConnLineChart(Model model, Payload payload) {
		CommandQueryEntity entity1 = payload.getQueryEntity1();
		CommandQueryEntity entity2 = payload.getQueryEntity2();
		QueryType type = payload.getQueryType();
		LineChart lineChart = new LineChart();
		List<AppDataDetail> appDetails = new ArrayList<AppDataDetail>();

		try {
			lineChart = m_appConnectionGraphCreator.buildLineChart(entity1, entity2, type);
			appDetails = m_appConnectionService.buildAppDataDetailInfos(entity1, payload.getGroupByField());
			Collections.sort(appDetails, new LineChartDetailInfoComparator(payload.getSort()));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Pair<LineChart, List<AppDataDetail>>(lineChart, appDetails);

	}

	private Pair<PieChart, PieChartDetailInfo> buildConnPieChart(Payload payload) {
		try {
			Pair<PieChart, PieChartDetailInfo> pair = m_appConnectionGraphCreator.buildPieChart(payload.getQueryEntity1(),
			      payload.getGroupByField());
			PieChartDetailInfo info = pair.getValue();

			Collections.sort(info.getItems(), new PieChartDetailInfoComparator());
			return pair;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	private DisplayCommands buildDisplayCommands(AppReport report, String sort) throws IOException {
		CodeDisplayVisitor distributionVisitor = new CodeDisplayVisitor(m_projectService, m_appConfigManager);

		distributionVisitor.visitAppReport(report);
		DisplayCommands displayCommands = distributionVisitor.getCommands();

		AppCommandsSorter sorter = new AppCommandsSorter(displayCommands, sort);
		displayCommands = sorter.getSortedCommands();
		return displayCommands;
	}

	private LineChart buildLineChart(Payload payload) {
		CommandQueryEntity entity1 = payload.getQueryEntity1();
		CommandQueryEntity entity2 = payload.getQueryEntity2();
		QueryType type = payload.getQueryType();
		LineChart lineChart = new LineChart();

		try {
			lineChart = m_appGraphCreator.buildLineChart(entity1, entity2, type);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return lineChart;
	}

	private Map<String, List<Speed>> buildPageStepInfo() {
		Map<String, List<Speed>> page2Steps = new HashMap<String, List<Speed>>();

		for (Speed speed : m_appSpeedConfigManager.getConfig().getSpeeds().values()) {
			String page = speed.getPage();
			if (StringUtils.isEmpty(page)) {
				page = "default";
			}
			List<Speed> steps = page2Steps.get(page);
			if (steps == null) {
				steps = new ArrayList<Speed>();
				page2Steps.put(page, steps);
			}
			steps.add(speed);
		}
		for (Entry<String, List<Speed>> entry : page2Steps.entrySet()) {
			List<Speed> speeds = entry.getValue();
			Collections.sort(speeds, new Comparator<Speed>() {

				@Override
				public int compare(Speed o1, Speed o2) {
					return o1.getStep() - o2.getStep();
				}
			});
		}
		return page2Steps;
	}

	private Pair<PieChart, PieChartDetailInfo> buildPieChart(Payload payload) {
		try {
			Pair<PieChart, PieChartDetailInfo> pair = m_appGraphCreator.buildPieChart(payload.getQueryEntity1(),
			      payload.getGroupByField());
			PieChartDetailInfo info = pair.getValue();
			Collections.sort(info.getItems(), new PieChartDetailInfoComparator());

			return pair;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <T> T fetchTaskResult(List<FutureTask> tasks, int i) {
		T data = null;
		FutureTask task = tasks.get(i);

		try {
			data = (T) task.get(10L, TimeUnit.SECONDS);
		} catch (Exception e) {
			task.cancel(true);
			Cat.logError(e);
		}
		return data;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "app")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "app")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		Map<String, Object> jsonObjs = new HashMap<String, Object>();

		normalize(model, payload);

		switch (action) {
		case LINECHART:
			parallelBuildLineChart(model, payload);
			break;
		case PIECHART:
			Pair<PieChart, PieChartDetailInfo> pieChartPair = buildPieChart(payload);

			if (pieChartPair != null) {
				model.setPieChart(pieChartPair.getKey());
				model.setPieChartDetailInfo(pieChartPair.getValue());
			}
			int commandId = payload.getQueryEntity1().getId();

			model.setCommandId(commandId);
			model.setCodes(m_appConfigManager.queryInternalCodes(commandId));
			break;
		case LINECHART_JSON:
			parallelBuildLineChart(model, payload);
			Map<String, Object> lineChartObjs = new HashMap<String, Object>();

			lineChartObjs.put("lineCharts", model.getLineChart());
			lineChartObjs.put("lineChartDailyInfo", model.getComparisonAppDetails());
			lineChartObjs.put("lineChartDetails", model.getAppDataDetailInfos());
			model.setFetchData(m_jsonBuilder.toJson(lineChartObjs));
			break;
		case PIECHART_JSON:
			Pair<PieChart, PieChartDetailInfo> pieChartJsonPair = buildPieChart(payload);

			if (pieChartJsonPair != null) {
				Map<String, Object> pieChartObjs = new HashMap<String, Object>();

				pieChartObjs.put("pieCharts", pieChartJsonPair.getKey());
				pieChartObjs.put("pieChartDetails", pieChartJsonPair.getValue());
				model.setFetchData(m_jsonBuilder.toJson(pieChartObjs));
			}
			break;
		case APP_ADD:
			String domain = payload.getDomain();
			String name = payload.getName();
			String title = payload.getTitle();

			if (StringUtils.isEmpty(name)) {
				setUpdateResult(model, 0);
			} else {
				if (m_appConfigManager.isNameDuplicate(name)) {
					setUpdateResult(model, 3);
				} else {
					try {
						Command command = new Command();

						command.setDomain(domain).setTitle(title).setName(name);

						Pair<Boolean, Integer> addCommandResult = m_appConfigManager.addCommand(command);

						if (addCommandResult.getKey()) {
							setUpdateResult(model, 1);
							m_appRuleConfigManager.addDefultRule(name, addCommandResult.getValue());
						} else {
							setUpdateResult(model, 2);
						}
					} catch (Exception e) {
						setUpdateResult(model, 2);
					}
				}
			}
			break;
		case APP_DELETE:
			domain = payload.getDomain();
			name = payload.getName();

			if (StringUtils.isEmpty(name)) {
				setUpdateResult(model, 0);
			} else {
				Pair<Boolean, List<Integer>> deleteCommandResult = m_appConfigManager.deleteCommand(domain, name);
				if (deleteCommandResult.getKey()) {
					setUpdateResult(model, 1);
					m_appRuleConfigManager.deleteDefaultRule(name, deleteCommandResult.getValue());
				} else {
					setUpdateResult(model, 2);
				}
			}
			break;
		case APP_CONFIG_FETCH:
			String type = payload.getType();

			try {
				if ("xml".equalsIgnoreCase(type)) {
					model.setFetchData(m_appConfigManager.getConfig().toString());
				} else if (StringUtils.isEmpty(type) || "json".equalsIgnoreCase(type)) {
					model.setFetchData(m_jsonBuilder.toJson(m_appConfigManager.getConfig()));
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case HOURLY_CRASH_LOG:
		case HISTORY_CRASH_LOG:
			try {
				m_crashLogProcessor.process(action, payload, model);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case APP_CRASH_LOG:
			buildAppCrashLog(payload, model);
			break;
		case APP_CRASH_LOG_DETAIL:
			buildAppCrashLogDetail(payload, model);
			break;
		case SPEED:
			try {
				Map<String, List<Speed>> speeds = buildPageStepInfo();
				model.setSpeeds(speeds);

				SpeedQueryEntity queryEntity1 = normalizeQueryEntity(payload, speeds);
				AppSpeedDisplayInfo info = m_appSpeedService.buildSpeedDisplayInfo(queryEntity1,
				      payload.getSpeedQueryEntity2());

				model.setAppSpeedDisplayInfo(info);
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case SPEED_JSON:
			try {
				Map<String, List<Speed>> speeds = buildPageStepInfo();
				SpeedQueryEntity queryEntity1 = normalizeQueryEntity(payload, speeds);
				AppSpeedDisplayInfo info = m_appSpeedService.buildSpeedDisplayInfo(queryEntity1,
				      payload.getSpeedQueryEntity2());

				jsonObjs.put("lineCharts", info.getLineChart());
				jsonObjs.put("appSpeedDetails", info.getAppSpeedDetails());
				jsonObjs.put("appSpeedSummarys", info.getAppSpeedSummarys());
				model.setFetchData(m_jsonBuilder.toJson(jsonObjs));
			} catch (Exception e) {
				Cat.logError(e);
			}
			break;
		case CONN_LINECHART:
			Pair<LineChart, List<AppDataDetail>> lineChartPair = buildConnLineChart(model, payload);

			model.setLineChart(lineChartPair.getKey());
			model.setAppDataDetailInfos(lineChartPair.getValue());
			break;
		case CONN_LINECHART_JSON:
			lineChartPair = buildConnLineChart(model, payload);

			jsonObjs.put("lineChart", lineChartPair.getKey());
			jsonObjs.put("detailInfos", lineChartPair.getValue());
			model.setFetchData(m_jsonBuilder.toJson(jsonObjs));
			break;
		case CONN_PIECHART:
			pieChartPair = buildConnPieChart(payload);

			if (pieChartPair != null) {
				model.setPieChart(pieChartPair.getKey());
				model.setPieChartDetailInfo(pieChartPair.getValue());
			}
			commandId = payload.getQueryEntity1().getId();

			model.setCommandId(commandId);
			model.setCodes(m_appConfigManager.queryInternalCodes(commandId));
			break;
		case CONN_PIECHART_JSON:
			pieChartPair = buildConnPieChart(payload);

			if (pieChartPair != null) {
				jsonObjs.put("pieChart", pieChartPair.getKey());
				jsonObjs.put("detailInfos", pieChartPair.getValue());
				model.setFetchData(m_jsonBuilder.toJson(jsonObjs));
			}
			break;
		case STATISTICS:
			AppReport report = queryAppReport(payload);
			DisplayCommands displayCommands = buildDisplayCommands(report, payload.getSort());

			model.setDisplayCommands(displayCommands);
			model.setAppReport(report);
			model.setCodeDistributions(buildCodeDistributions(displayCommands));
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void buildAppCrashLog(Payload payload, Model model) {
		CrashLogQueryEntity entity = new CrashLogQueryEntity();
		CrashLogDisplayInfo info = m_crashLogService.buildCrashLogDisplayInfo(entity);
		model.setCrashLogDisplayInfo(info);
	}

	private void buildAppCrashLogDetail(Payload payload, Model model) {
		CrashLogDetailInfo info = m_crashLogService.queryCrashLogDetailIno(payload.getId());
		model.setCrashLogDetailInfo(info);
	}

	private void normalize(Model model, Payload payload) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.APP);
		model.setConnectionTypes(m_appConfigManager.queryConfigItem(AppConfigManager.CONNECT_TYPE));
		model.setCities(m_appConfigManager.queryConfigItem(AppConfigManager.CITY));
		model.setNetworks(m_appConfigManager.queryConfigItem(AppConfigManager.NETWORK));
		model.setOperators(m_appConfigManager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setPlatforms(m_appConfigManager.queryConfigItem(AppConfigManager.PLATFORM));
		model.setVersions(m_appConfigManager.queryConfigItem(AppConfigManager.VERSION));
		model.setCommands(m_appConfigManager.queryCommands());
		model.setCommand2Id(m_appConfigManager.getCommands());
		model.setCommand2Codes(m_appConfigManager.queryCommand2Codes());

		Command defaultCommand = m_appConfigManager.getRawCommands().get(CommandQueryEntity.DEFAULT_COMMAND);

		model.setDefaultCommand(defaultCommand.getName() + "|" + defaultCommand.getTitle());
		m_normalizePayload.normalize(model, payload);
	}

	private SpeedQueryEntity normalizeQueryEntity(Payload payload, Map<String, List<Speed>> speeds) {
		SpeedQueryEntity query1 = payload.getSpeedQueryEntity1();

		if (StringUtils.isEmpty(payload.getQuery1())) {
			if (!speeds.isEmpty()) {
				List<Speed> first = speeds.get(speeds.keySet().toArray()[0]);

				if (first != null && !first.isEmpty()) {
					query1.setId(first.get(0).getId());
				}
			}
		}
		return query1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void parallelBuildLineChart(Model model, final Payload payload) {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		List<FutureTask> tasks = new LinkedList<FutureTask>();
		FutureTask lineChartTask = new FutureTask(new Callable<LineChart>() {
			@Override
			public LineChart call() throws Exception {
				return buildLineChart(payload);
			}
		});
		tasks.add(lineChartTask);
		executor.execute(lineChartTask);

		FutureTask appDetailTask = new FutureTask(new Callable<List<AppDataDetail>>() {
			@Override
			public List<AppDataDetail> call() throws Exception {
				return buildAppDataDetails(payload);
			}
		});
		tasks.add(appDetailTask);
		executor.execute(appDetailTask);

		FutureTask comparisonTask = new FutureTask(new Callable<Map<String, AppDataDetail>>() {
			@Override
			public Map<String, AppDataDetail> call() throws Exception {
				return buildComparisonInfo(payload);
			}
		});
		tasks.add(comparisonTask);
		executor.execute(comparisonTask);

		LineChart lineChart = fetchTaskResult(tasks, 0);
		List<AppDataDetail> appDataDetails = fetchTaskResult(tasks, 1);
		Map<String, AppDataDetail> comparisonDetails = fetchTaskResult(tasks, 2);

		executor.shutdown();
		model.setLineChart(lineChart);
		model.setAppDataDetailInfos(appDataDetails);
		model.setComparisonAppDetails(comparisonDetails);
	}

	private AppReport queryAppReport(Payload payload) {
		Date startDate = payload.getDayDate();
		Date endDate = TimeHelper.addDays(startDate, 1);
		AppReport report = m_appReportService.queryDailyReport(Constants.CAT, startDate, endDate);
		return report;
	}

	private void setUpdateResult(Model model, int i) {
		switch (i) {
		case 0:
			model.setContent("{\"status\":500, \"info\":\"name is required.\"}");
			break;
		case 1:
			model.setContent("{\"status\":200}");
			break;
		case 2:
			model.setContent("{\"status\":500}");
			break;
		case 3:
			model.setContent("{\"status\":500, \"info\":\"name is duplicated.\"}");
			break;
		}
	}

	public class CodeDistributionComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			int id1 = Integer.parseInt(o1.replaceAll("X", "0"));
			int id2 = Integer.parseInt(o2.replaceAll("X", "0"));

			return id2 - id1;
		}
	}

	public static class PieChartDetailInfoComparator implements Comparator<Item> {

		@Override
		public int compare(Item o1, Item o2) {
			double sum2 = o2.getRequestSum();
			double sum1 = o1.getRequestSum();

			if (sum2 > sum1) {
				return 1;
			} else if (sum2 < sum1) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	public static class LineChartDetailInfoComparator implements Comparator<AppDataDetail> {

		private QueryType m_sortBy;

		public LineChartDetailInfoComparator() {
			this(QueryType.REQUEST.getName());
		}

		public LineChartDetailInfoComparator(String sortBy) {
			m_sortBy = QueryType.findByName(sortBy);
		}

		@Override
		public int compare(AppDataDetail o1, AppDataDetail o2) {
			switch (m_sortBy) {
			case DELAY:
				return (int) ((o2.getResponseTimeAvg() - o1.getResponseTimeAvg()) * 1000);
			case REQUEST:
				return (int) (o2.getAccessNumberSum() - o1.getAccessNumberSum());
			case REQUEST_PACKAGE:
				return (int) ((o2.getRequestPackageAvg() - o1.getRequestPackageAvg()) * 1000);
			case RESPONSE_PACKAGE:
				return (int) ((o2.getResponsePackageAvg() - o1.getResponsePackageAvg()) * 1000);
			case SUCCESS:
				return (int) ((o2.getSuccessRatio() - o1.getSuccessRatio()) * 1000);
			}
			return 0;
		}
	}
}
