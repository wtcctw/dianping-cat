package com.dianping.cat.report.page.browser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.configuration.web.url.entity.PatternItem;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
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
	private ModuleManager m_moduleManager;

	@Inject
	private UrlPatternConfigManager m_patternManager;

	@Inject
	private WebConfigManager m_webConfigManager;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private WebGraphCreator m_graphCreator;

	@Inject
	private JsErrorLogDao m_jsErrorLogDao;

	@Inject
	private JsErrorLogContentDao m_jsErrorLogContentlDao;

	private final int LIMIT = 10000;

	@Inject(type = ModelService.class, value = ProblemAnalyzer.ID)
	private ModelService<ProblemReport> m_service;

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
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "browser")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
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

	private void normalize(Model model, Payload payload) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.BROWSER);
		model.setCities(m_webConfigManager.queryConfigItem(WebConfigManager.CITY));
		model.setOperators(m_webConfigManager.queryConfigItem(WebConfigManager.OPERATOR));
		model.setNetworks(m_webConfigManager.queryConfigItem(WebConfigManager.NETWORK));
		model.setCodes(m_patternManager.queryCodes());

		PatternItem first = m_patternManager.queryUrlPatternRules().iterator().next();

		model.setDefaultApi(first.getName() + "|" + first.getPattern());
		model.setPattermItems(m_patternManager.queryUrlPatterns());
		m_normalizePayload.normalize(model, payload);
	}

	private void processLog(Map<String, ErrorMsg> errorMsgs, JsErrorLog log) {
		String msg = log.getMsg();
		ErrorMsg errorMsg = errorMsgs.get(msg);

		if (errorMsg == null) {
			errorMsg = new ErrorMsg();
			errorMsg.setMsg(msg);
			errorMsgs.put(msg, errorMsg);
		}

		errorMsg.addCount();
		errorMsg.addId(log.getId());
	}

	private List<ErrorMsg> sort(Map<String, ErrorMsg> errorMsgs) {
		List<ErrorMsg> errorMsgList = new ArrayList<ErrorMsg>();
		Iterator<Entry<String, ErrorMsg>> iter = errorMsgs.entrySet().iterator();

		while (iter.hasNext()) {
			errorMsgList.add(iter.next().getValue());
		}

		Collections.sort(errorMsgList);
		return errorMsgList;
	}

	private void viewJsError(Payload payload, Model model) {
		try {
			Date startTime = payload.buildStartTime();
			Date endTime = payload.buildEndTime();
			int levelCode = payload.buildLevel();
			String module = payload.getModule();
			Map<String, ErrorMsg> errorMsgs = new HashMap<String, ErrorMsg>();
			int offset = 0;
			int totalCount = 0;

			while (true) {
				List<JsErrorLog> result = m_jsErrorLogDao.findDataByTimeModuleLevelBrowser(startTime, endTime, module,
				      levelCode, null, offset, LIMIT, JsErrorLogEntity.READSET_FULL);

				for (JsErrorLog log : result) {
					processLog(errorMsgs, log);
				}

				int count = result.size();
				totalCount += count;
				offset += count;

				if (count < LIMIT) {
					break;
				}
			}

			List<ErrorMsg> errorMsgList = sort(errorMsgs);

			model.setErrors(errorMsgList);
			model.setTotalCount(totalCount);
			model.setLevels(Level.getLevels());
			model.setModules(m_moduleManager.getModules());
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private void viewJsErrorDetail(Payload payload, Model model) {
		try {
			int id = payload.getId();

			JsErrorLogContent detail = m_jsErrorLogContentlDao.findByPK(id, JsErrorLogContentEntity.READSET_FULL);
			JsErrorLog jsErrorLog = m_jsErrorLogDao.findByPK(id, JsErrorLogEntity.READSET_FULL);

			model.setErrorTime(jsErrorLog.getErrorTime());
			model.setLevel(Level.getNameByCode(jsErrorLog.getLevel()));
			model.setModule(jsErrorLog.getModule());
			model.setDetail(new String(detail.getContent(), "UTF-8"));
			model.setAgent(jsErrorLog.getBrowser());
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

}
