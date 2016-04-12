package com.dianping.cat.report.page.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.alert.app.AppRuleConfigManager;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.app.display.AppCommandDisplayInfo;
import com.dianping.cat.report.page.app.display.AppConnectionDisplayInfo;
import com.dianping.cat.report.page.app.display.AppConnectionGraphCreator;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppDetailComparator;
import com.dianping.cat.report.page.app.display.AppGraphCreator;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;
import com.dianping.cat.report.page.app.display.AppStatisticBuilder;
import com.dianping.cat.report.page.app.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.app.display.CrashLogDisplayInfo;
import com.dianping.cat.report.page.app.display.DashBoardInfo;
import com.dianping.cat.report.page.app.display.DisplayCommands;
import com.dianping.cat.report.page.app.display.UpdateStatus;
import com.dianping.cat.report.page.app.service.AppConnectionService;
import com.dianping.cat.report.page.app.service.AppDataField;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.AppSpeedService;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;
import com.dianping.cat.report.page.app.service.CrashLogQueryEntity;
import com.dianping.cat.report.page.app.service.CrashLogService;
import com.dianping.cat.report.page.app.service.DailyCommandQueryEntity;
import com.dianping.cat.report.page.app.service.DailyReportService;
import com.dianping.cat.report.page.app.service.DashBoardBuilder;
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
	private CrashLogService m_crashLogService;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private DashBoardBuilder m_dashboardBuilder;

	@Inject
	private DailyReportService m_dailyService;

	@Inject
	private AppStatisticBuilder m_appStatisticBuilder;

	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	private void buildAppCrashGraph(Payload payload, Model model) {
		CrashLogQueryEntity entity = payload.getCrashLogQuery();
		CrashLogDisplayInfo info = m_crashLogService.buildCrashGraph(entity);

		model.setCrashLogDisplayInfo(info);
	}

	private CrashLogDisplayInfo buildAppCrashLog(Payload payload) {
		CrashLogQueryEntity entity = payload.getCrashLogQuery();
		CrashLogDisplayInfo info = m_crashLogService.buildCrashLogDisplayInfo(entity);

		return info;
	}

	private void buildAppCrashLogDetail(Payload payload, Model model) {
		CrashLogDetailInfo info = m_crashLogService.queryCrashLogDetailInfo(payload.getId());

		model.setCrashLogDetailInfo(info);
	}

	private List<AppDataDetail> buildAppDataDetails(Payload payload) {
		List<AppDataDetail> appDetails = new ArrayList<AppDataDetail>();

		try {
			appDetails = m_appDataService.buildAppDataDetailInfos(payload.getQueryEntity1(), payload.getGroupByField());

			Collections.sort(appDetails, new AppDetailComparator(payload.getSort()));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return appDetails;
	}

	private void buildCommandDailyChart(Payload payload, Model model) {
		DailyCommandQueryEntity queryEntity = payload.getCommandDailyQueryEntity();

		LineChart lineChart = m_dailyService.buildLineChart(queryEntity, payload.getQueryType());
		model.setLineChart(lineChart);
	}

	private AppCommandDisplayInfo buildCommandDistributeChart(Payload payload) {
		try {
			AppCommandDisplayInfo displayInfo = m_appGraphCreator.buildCommandDistributeChart(payload.getQueryEntity1(),
			      payload.getGroupByField());

			return displayInfo;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new AppCommandDisplayInfo();
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
			Collections.sort(appDetails, new AppDetailComparator(payload.getSort()));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Pair<LineChart, List<AppDataDetail>>(lineChart, appDetails);
	}

	private AppConnectionDisplayInfo buildConnPieChart(Payload payload) {
		try {
			return m_appConnectionGraphCreator.buildPieChart(payload.getQueryEntity1(), payload.getGroupByField());
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new AppConnectionDisplayInfo();
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

	private void buildSpeedBarCharts(Payload payload, Model model) {
		try {
			Map<String, List<Speed>> speeds = m_appSpeedConfigManager.getPageStepInfo();
			SpeedQueryEntity queryEntity = normalizeQueryEntity(payload, speeds);
			AppSpeedDisplayInfo info = m_appSpeedService.buildBarCharts(queryEntity);

			info.setSpeeds(speeds);
			model.setAppSpeedDisplayInfo(info);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private AppSpeedDisplayInfo buildSpeedTendency(Payload payload) {
		try {
			Map<String, List<Speed>> speeds = m_appSpeedConfigManager.getPageStepInfo();
			SpeedQueryEntity queryEntity1 = normalizeQueryEntity(payload, speeds);
			AppSpeedDisplayInfo info = m_appSpeedService.buildSpeedDisplayInfo(queryEntity1,
			      payload.getSpeedQueryEntity2());

			info.setSpeeds(speeds);
			return info;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new AppSpeedDisplayInfo();
	}

	private void buillAppStatisticInfo(Model model, Payload payload) throws IOException {
		AppReport report = m_appStatisticBuilder.queryAppReport(payload.getDayDate());
		DisplayCommands displayCommands = m_appStatisticBuilder.buildDisplayCommands(report, payload.getSort());
		Set<String> codeKeys = m_appStatisticBuilder.buildCodeKeys(displayCommands);
		List<String> piechartCodes = payload.getCodes();

		if (piechartCodes.isEmpty()) {
			piechartCodes = new ArrayList<String>(codeKeys);
		}

		Map<String, PieChart> piecharts = m_appStatisticBuilder.buildCodePiecharts(piechartCodes, displayCommands,
		      payload.getTop());

		model.setPiecharts(piecharts);
		model.setDisplayCommands(displayCommands);
		model.setAppReport(report);
		model.setCodeDistributions(codeKeys);
	}

	private void commandAdd(Payload payload, Model model) {
		String domain = payload.getDomain();
		String name = payload.getName();
		String title = payload.getTitle();

		if (StringUtils.isEmpty(name)) {
			model.setContent(UpdateStatus.NO_NAME.getStatusJson());
		} else {
			if (m_appConfigManager.isNameDuplicate(name)) {
				model.setContent(UpdateStatus.DUPLICATE_NAME.getStatusJson());
			} else {
				try {
					Command command = new Command();

					command.setDomain(domain).setTitle(title).setName(name);

					Pair<Boolean, Integer> addCommandResult = m_appConfigManager.addCommand(command);

					if (addCommandResult.getKey()) {
						m_appRuleConfigManager.addDefultRule(name, addCommandResult.getValue());
						model.setContent(UpdateStatus.SUCCESS.getStatusJson());
					} else {
						model.setContent(UpdateStatus.INTERNAL_ERROR.getStatusJson());
					}
				} catch (Exception e) {
					model.setContent(UpdateStatus.INTERNAL_ERROR.getStatusJson());
				}
			}
		}
	}

	private void commandDelete(Payload payload, Model model) {
		String domain = payload.getDomain();
		String name = payload.getName();

		if (StringUtils.isEmpty(name)) {
			model.setContent(UpdateStatus.NO_NAME.getStatusJson());

		} else {
			Pair<Boolean, List<Integer>> deleteCommandResult = m_appConfigManager.deleteCommand(domain, name);
			if (deleteCommandResult.getKey()) {
				m_appRuleConfigManager.deleteDefaultRule(name, deleteCommandResult.getValue());
				model.setContent(UpdateStatus.SUCCESS.getStatusJson());
			} else {
				model.setContent(UpdateStatus.INTERNAL_ERROR.getStatusJson());
			}
		}
	}

	private void fetchConfig(Payload payload, Model model) {
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
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T fetchTaskResult(FutureTask task) {
		T data = null;

		try {
			data = (T) task.get(30L, TimeUnit.SECONDS);
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
			AppCommandDisplayInfo commandDisplayInfo = buildCommandDistributeChart(payload);
			int commandId = payload.getQueryEntity1().getId();

			model.setCommandDisplayInfo(commandDisplayInfo);
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
			AppCommandDisplayInfo appCommandDisplayInfo = buildCommandDistributeChart(payload);
			Map<String, Object> pieChartObjs = new HashMap<String, Object>();

			pieChartObjs.put("pieCharts", appCommandDisplayInfo.getPieChart());
			pieChartObjs.put("pieChartDetails", appCommandDisplayInfo.getDistributeDetails());
			model.setFetchData(m_jsonBuilder.toJson(pieChartObjs));
			break;
		case APP_ADD:
			commandAdd(payload, model);
			break;
		case APP_DELETE:
			commandDelete(payload, model);
			break;
		case APP_CONFIG_FETCH:
			fetchConfig(payload, model);
			break;
		case APP_CRASH_LOG:
			CrashLogDisplayInfo displayInfo = buildAppCrashLog(payload);

			model.setCrashLogDisplayInfo(displayInfo);
			break;
		case APP_CRASH_LOG_JSON:
			displayInfo = buildAppCrashLog(payload);

			model.setFetchData(m_jsonBuilder.toJson(displayInfo));
			break;
		case APP_CRASH_LOG_DETAIL:
			buildAppCrashLogDetail(payload, model);
			break;
		case APP_CRASH_GRAPH:
			buildAppCrashGraph(payload, model);
			break;
		case SPEED:
			model.setAppSpeedDisplayInfo(buildSpeedTendency(payload));
			break;
		case SPEED_JSON:
			AppSpeedDisplayInfo info = buildSpeedTendency(payload);

			jsonObjs.put("lineCharts", info.getLineChart());
			jsonObjs.put("appSpeedDetails", info.getAppSpeedDetails());
			jsonObjs.put("appSpeedSummarys", info.getAppSpeedSummarys());
			model.setFetchData(m_jsonBuilder.toJson(jsonObjs));
			break;
		case SPEED_GRAPH:
			buildSpeedBarCharts(payload, model);
			break;
		case APP_COMMAND_DAILY:
			buildCommandDailyChart(payload, model);
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
			AppConnectionDisplayInfo connDisplayInfo = buildConnPieChart(payload);
			commandId = payload.getQueryEntity1().getId();

			model.setConnDisplayInfo(connDisplayInfo);
			model.setCommandId(commandId);
			model.setConnDisplayInfo(connDisplayInfo);
			model.setCodes(m_appConfigManager.queryInternalCodes(commandId));
			break;
		case CONN_PIECHART_JSON:
			AppConnectionDisplayInfo appConnDisplayInfo = buildConnPieChart(payload);

			if (appConnDisplayInfo != null) {
				jsonObjs.put("pieChart", appConnDisplayInfo.getPieChart());
				jsonObjs.put("detailInfos", appConnDisplayInfo.getPieChartDetailInfo());
				model.setFetchData(m_jsonBuilder.toJson(jsonObjs));
			}
			break;
		case STATISTICS:
			buillAppStatisticInfo(model, payload);
			break;
		case DASHBOARD:
			DashBoardInfo dashboardInfo = m_dashboardBuilder.buildDashBoard(payload.getDashBoardQuery());
			model.setDashBoardInfo(dashboardInfo);
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
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
		model.setSources(m_appConfigManager.queryConfigItem(AppConfigManager.SOURCE));
		model.setCommands(m_appConfigManager.queryCommands());
		model.setCommand2Id(m_appConfigManager.getCommands());
		model.setCommand2Codes(m_appConfigManager.queryCommand2Codes());
		model.setGlobalCodes(m_appConfigManager.getConfig().getCodes());

		Command defaultCommand = m_appConfigManager.getRawCommands().get(CommandQueryEntity.DEFAULT_COMMAND);

		model.setDefaultCommand(defaultCommand.getName() + "|" + defaultCommand.getTitle());
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
		FutureTask lineChartTask = new FutureTask(new Callable<LineChart>() {
			@Override
			public LineChart call() throws Exception {
				return buildLineChart(payload);
			}
		});
		executor.execute(lineChartTask);

		FutureTask appDetailTask = new FutureTask(new Callable<List<AppDataDetail>>() {
			@Override
			public List<AppDataDetail> call() throws Exception {
				return buildAppDataDetails(payload);
			}
		});
		executor.execute(appDetailTask);

		FutureTask comparisonTask = new FutureTask(new Callable<Map<String, AppDataDetail>>() {
			@Override
			public Map<String, AppDataDetail> call() throws Exception {
				return buildComparisonInfo(payload);
			}
		});
		executor.execute(comparisonTask);

		LineChart lineChart = fetchTaskResult(lineChartTask);
		List<AppDataDetail> appDataDetails = fetchTaskResult(appDetailTask);
		Map<String, AppDataDetail> comparisonDetails = fetchTaskResult(comparisonTask);

		executor.shutdown();
		model.setLineChart(lineChart);
		model.setAppDataDetailInfos(appDataDetails);
		model.setComparisonAppDetails(comparisonDetails);
	}
}
