package com.dianping.cat.report.page.server;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.entity.Item;
import com.dianping.cat.home.graph.entity.Segment;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.metric.MetricType;
import com.dianping.cat.metric.QueryParameter;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.DataExtractor;
import com.dianping.cat.report.page.server.display.MetricScreenInfo;
import com.dianping.cat.report.page.server.service.EndPointService;
import com.dianping.cat.report.page.server.service.GraphBuilder;
import com.dianping.cat.report.page.server.service.GraphService;
import com.dianping.cat.report.page.server.service.ScreenService;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ScreenService m_screenService;

	@Inject
	private GraphService m_graphService;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private MetricService m_metricService;

	@Inject
	private DataExtractor m_dataExtractor;

	@Inject
	private EndPointService m_endPointService;

	@Inject
	private GraphBuilder m_graphBuilder;

	@Inject
	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	private List<LineChart> buildLineCharts(Date start, Date end, String interval, String view, Graph graph) {
		List<LineChart> lineCharts = new LinkedList<LineChart>();

		for (Entry<String, Item> entry : graph.getItems().entrySet()) {
			Item item = entry.getValue();

			if (StringUtils.isEmpty(view) || view.equals(item.getView())) {
				LineChart linechart = new LineChart();

				linechart.setId(entry.getKey());
				linechart.setHtmlTitle(entry.getKey());

				for (Entry<String, Segment> e : item.getSegments().entrySet()) {
					Segment segment = e.getValue();
					Map<Long, Double> result = fetchData(start, end, e.getValue(), interval);
					String title = segment.getId();

					linechart.add(title, result);
				}
				lineCharts.add(linechart);
			}
		}
		return lineCharts;
	}

	private Map<Long, Double> fetchData(Date start, Date end, Segment segment, String interval) {
		MetricType type = MetricType.getByName(segment.getType(), MetricType.AVG);
		QueryParameter parameter = new QueryParameter();
		parameter.setCategory(segment.getCategory()).setStart(start).setEnd(end).setMeasurement(segment.getMeasure())
		      .setType(type).setTags(segment.getTags()).setInterval(interval);

		Map<Long, Double> result = m_metricService.query(parameter);

		return result;
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "server")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "server")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(payload, model);

		Date start = payload.getHistoryStartDate();
		Date end = payload.getHistoryEndDate();

		switch (action) {
		case GRAPH:
			normalizeGraphInfo(payload, model);

			Graph graph = m_graphService.queryByGraphId(payload.getGraphId());
			graph = convertGraphType(graph, payload.getType());

			if (graph != null) {
				List<LineChart> lineCharts = buildLineCharts(start, end, payload.getInterval(), payload.getView(), graph);

				model.setLineCharts(lineCharts);
			}
			break;
		case SCREEN:
			normalizeScreenInfo(payload, model);

			List<LineChart> lineCharts = new LinkedList<LineChart>();
			Map<String, MetricScreenInfo> graphs = m_screenService.queryByName(payload.getScreen());

			for (MetricScreenInfo info : graphs.values()) {
				Graph g = info.getGraph();

				if (g != null) {
					List<LineChart> lines = buildLineCharts(start, end, payload.getInterval(), "", g);

					lineCharts.addAll(lines);
				}
			}
			model.setLineCharts(lineCharts);
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case SCREENS:
			Map<String, Map<String, MetricScreenInfo>> screens = m_screenService.queryScreens();

			model.setMetricScreenInfos(screens);
			break;
		case SCREEN_UPDATE:
			String screen = payload.getScreen();

			if (StringUtils.isNotEmpty(screen)) {
				Map<String, MetricScreenInfo> infos = m_screenService.queryByName(screen);

				model.setGraphs(infos.keySet());
			}
			break;
		case SCREEN_DELETE:
			m_screenService.deleteByScreen(payload.getScreen());
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case SCREEN_SUBMIT:
			m_screenService.updateScreen(payload.getScreen(), payload.getGraphs());
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case GRAPH_UPDATE:
			MetricScreenInfo screenInfo = m_screenService.queryByNameGraph(payload.getScreen(), payload.getGraph());

			model.setMetricScreenInfo(screenInfo);
			break;
		case GRAPH_SUBMIT:
			m_screenService.insertOrUpdateGraph(payload.getGraphParam());
			model.setMetricScreenInfos(m_screenService.queryScreens());
			break;
		case AGGREGATE:

			break;
		case ENDPOINT:
			List<String> keywords = payload.getKeywordsList();

			if (!keywords.isEmpty()) {
				Set<String> endPoints = m_endPointService.queryEndPoints(payload.getSearch(), keywords);
				Map<String, Object> jsons = new HashMap<String, Object>();

				jsons.put("endPoints", endPoints);
				model.setJson(m_jsonBuilder.toJson(jsons));
			}
			break;
		case MEASUREMTN:
			List<String> endPoints = payload.getEndPoints();

			if (!endPoints.isEmpty()) {
				Set<String> measurements = m_endPointService.queryMeasurements(endPoints);
				Map<String, Object> jsons = new HashMap<String, Object>();

				jsons.put("endPoints", measurements);
				model.setJson(m_jsonBuilder.toJson(jsons));
			}
			break;
		case BUILDVIEW:
			graph = buildGraph(payload);
			boolean success = m_graphService.insert(graph);

			if (success) {
				Map<String, Object> jsons = new HashMap<String, Object>();

				jsons.put("id", payload.getGraphId());
				model.setJson(m_jsonBuilder.toJson(jsons));
			}
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private Graph convertGraphType(Graph graph, String type) {
		for (Entry<String, Item> entry : graph.getItems().entrySet()) {
			for (Entry<String, Segment> e : entry.getValue().getSegments().entrySet()) {
				Segment segment = e.getValue();

				segment.setType(type);
			}
		}
		return graph;
	}

	private Graph buildGraph(Payload payload) {
		List<String> endPoints = payload.getEndPoints();
		List<String> measurements = payload.getMeasurements();

		return m_graphBuilder.buildGraph(endPoints, measurements, String.valueOf(payload.getGraphId()), "");
	}

	private void normalize(Payload payload, Model model) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.SERVER);
		m_normalizePayload.normalize(model, payload);
	}

	private void normalizeGraphInfo(Payload payload, Model model) {
		if (StringUtils.isEmpty(payload.getInterval())) {
			long end = payload.getHistoryEndDate().getTime();
			long start = payload.getHistoryStartDate().getTime();
			int length = (int) ((end - start) / TimeHelper.ONE_MINUTE);
			int gap = m_dataExtractor.calculateInterval(length);

			payload.setInterval(gap + "m");
		}

		if (payload.getGraphId() == 0) {
			payload.setGraphId(m_graphService.getLast().getGraphId());
		}
	}

	private void normalizeScreenInfo(Payload payload, Model model) {
		normalizeGraphInfo(payload, model);

		Map<String, Map<String, MetricScreenInfo>> screenGroups = m_screenService.queryScreens();

		if (StringUtils.isEmpty(payload.getScreen())) {
			if (!screenGroups.isEmpty()) {
				String defaultScreen = screenGroups.keySet().iterator().next();

				payload.setScreen(defaultScreen);
			} else {
				payload.setScreen("");
			}
		}
	}
}
