package com.dianping.cat.report.page.browser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.page.app.display.ChartSorter;
import com.dianping.cat.report.page.app.display.PieChartDetailInfo;
import com.dianping.cat.report.page.browser.graph.WebGraphCreator;
import com.dianping.cat.report.page.browser.service.AjaxDataQueryEntity;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.web.JsErrorLog;
import com.dianping.cat.web.JsErrorLogContent;
import com.dianping.cat.web.JsErrorLogContentDao;
import com.dianping.cat.web.JsErrorLogContentEntity;
import com.dianping.cat.web.JsErrorLogDao;
import com.dianping.cat.web.JsErrorLogEntity;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.config.web.js.Level;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	@Inject
	private WebConfigManager m_webConfigManager;

	@Inject
	private CityManager m_cityManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private WebGraphCreator m_graphCreator;

	@Inject
	private JsErrorLogDao m_jsErrorLogDao;

	@Inject
	private JsErrorLogContentDao m_jsErrorLogContentlDao;

	private static final String ALL = "ALL";

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "browser")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	private LineChart buildLineChart(Payload payload) {
		AjaxDataQueryEntity entity1 = payload.getQueryEntity1();
		AjaxDataQueryEntity entity2 = payload.getQueryEntity2();
		String type = payload.getType();
		LineChart lineChart = new LineChart();

		try {
			lineChart = m_graphCreator.buildLineChart(entity1, entity2, type);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return lineChart;
	}

	private Pair<PieChart, List<PieChartDetailInfo>> buildPieChart(Payload payload) {
		try {
			Pair<PieChart, List<PieChartDetailInfo>> pair = m_graphCreator.buildPieChart(payload.getQueryEntity1(),
			      payload.getGroupByField());
			List<PieChartDetailInfo> infos = pair.getValue();
			Collections.sort(infos, new ChartSorter().buildPieChartInfoComparator());

			return pair;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	@OutboundActionMeta(name = "browser")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);

		switch (action) {
		case VIEW:
			LineChart lineChart = buildLineChart(payload);
			model.setLineChart(lineChart);
			break;
		case PIECHART:
			Pair<PieChart, List<PieChartDetailInfo>> pieChartPair = buildPieChart(payload);

			if (pieChartPair != null) {
				model.setPieChart(pieChartPair.getKey());
				model.setPieChartDetailInfos(pieChartPair.getValue());
			}
			break;
		case JS_ERROR:
			viewJsError(payload, model);
			break;
		case JS_ERROR_DETAIL:
			viewJsErrorDetail(payload, model);
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void viewJsErrorDetail(Payload payload, Model model) {
		try {
			int id = payload.getId();

			JsErrorLogContent detail = m_jsErrorLogContentlDao.findByPK(id, JsErrorLogContentEntity.READSET_FULL);
			model.setDetail(new String(detail.getContent(), "UTF-8"));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void viewJsError(Payload payload, Model model) {
		try {
			String day = payload.getDay();
			Date startTime = buildDate(day, payload.getStartTime(), TimeHelper.getCurrentDay());
			Date endTime = buildDate(day, payload.getEndTime(), TimeHelper.getCurrentDay(1));
			int levelCode = buildLevel(payload.getLevel(), -1);
			String module = buildModule(payload.getModule());

			List<JsErrorLog> result = m_jsErrorLogDao.findDataByTimeModuleLevelBrowser(startTime, endTime, module,
			      levelCode, null, JsErrorLogEntity.READSET_MSG_DATA);
			int totalCount = 0;
			List<ErrorMsg> errorMsgs = new ArrayList<ErrorMsg>();

			for (JsErrorLog log : result) {
				ErrorMsg errorMsg = new ErrorMsg();
				errorMsg.setMsg(log.getMsg());

				int count = log.getCount();
				totalCount += count;
				errorMsg.setCount(count);

				buildIds(errorMsg, count, startTime, endTime, module, levelCode, null);
				errorMsgs.add(errorMsg);
			}

			model.setErrors(errorMsgs);
			model.setTotalCount(totalCount);
			model.setLevels(Level.getLevels());

			buildModuleList(model);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private void buildIds(ErrorMsg errorMsg, int count, Date startTime, Date endTime, String module, int levelCode,
	      String browser) {
		List<Integer> ids = new ArrayList<Integer>();

		try {
			List<JsErrorLog> result = m_jsErrorLogDao.findIds(startTime, endTime, module, levelCode, browser,
			      errorMsg.getMsg(), JsErrorLogEntity.READSET_FULL);

			for (JsErrorLog log : result) {
				ids.add(log.getId());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}

		errorMsg.setIds(ids);
	}

	private String buildModule(String module) {
		if (StringUtils.isEmpty(module)) {
			return null;
		} else {
			return module;
		}
	}

	private int buildLevel(String level, int defaultValue) {
		if (StringUtils.isEmpty(level) || level.equals(ALL)) {
			return defaultValue;
		} else {
			return Level.getCodeByName(level);

		}
	}

	private Date buildDate(String day, String time, Date defaultDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		if (StringUtils.isBlank(day) || StringUtils.isBlank(time)) {
			return defaultDate;
		} else {
			try {
				Date date = sdf.parse(day + " " + time);
				return date;
			} catch (ParseException e) {
				return defaultDate;
			}
		}
	}

	private void buildModuleList(Model model) throws DalException {
		Set<String> moduleList = new HashSet<String>();
		List<JsErrorLog> modules = m_jsErrorLogDao.findModules(JsErrorLogEntity.READSET_DISTINCT_MODULES);
		for (JsErrorLog log : modules) {
			moduleList.add(log.getModules());
		}
		model.setModules(moduleList);
	}

	private void normalize(Model model, Payload payload) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.WEB);
		model.setCities(m_webConfigManager.queryConfigItem(AppConfigManager.CITY));
		model.setOperators(m_webConfigManager.queryConfigItem(AppConfigManager.OPERATOR));
		model.setCodes(m_patternManager.queryCodes());

		PatternItem first = m_patternManager.queryUrlPatternRules().iterator().next();

		model.setDefaultApi(first.getName() + "|" + first.getPattern());
		model.setPattermItems(m_patternManager.queryUrlPatterns());
		m_normalizePayload.normalize(model, payload);
	}

}
