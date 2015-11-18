package com.dianping.cat.report.page.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

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
	protected DataExtractor m_dataExtractor;

	private List<LineChart> buildLineCharts(Date start, Date end, String interval, Graph graph) {
		List<LineChart> lineCharts = new ArrayList<LineChart>();

		for (Entry<String, Item> entry : graph.getItems().entrySet()) {
			Item item = entry.getValue();
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
		case VIEW:
			normalizeGraphInfo(payload, model);
			
			Graph graph = m_graphService.queryById(payload.getGraphId());

			if (graph != null) {
				List<LineChart> lineCharts = buildLineCharts(start, end, payload.getInterval(), graph);

				model.setLineCharts(lineCharts);
			}
			break;
		case SCREEN:
			QueryParameter parameter = new QueryParameter();

			parameter.setCategory("system").setStart(start).setEnd(end).setMeasurement("userCpu").setType(MetricType.AVG)
			      .setInterval("1m");

			Map<Long, Double> result = m_metricService.query(parameter);

			System.out.println(result);

			model.setGraphs(m_screenService.querByName(payload.getScreen()));
			break;
		case AGGREGATE:

			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Payload payload, Model model) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.SERVER);
		m_normalizePayload.normalize(model, payload);
		//
		// int timeRange = payload.getTimeRange();
		// Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeHelper.ONE_HOUR);
		// Date endTime = new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1);
		//
		// model.setStartTime(startTime);
		// model.setEndTime(endTime);

		String screen = payload.getScreen();

		if (StringUtils.isEmpty(screen)) {
			// TODO

			payload.setScreen(screen);
		}
	}

	private void normalizeGraphInfo(Payload payload, Model model) {
		if (StringUtils.isEmpty(payload.getInterval())) {
			long end = payload.getHistoryEndDate().getTime();
			long start = payload.getHistoryStartDate().getTime();
			int length = (int) ((end - start) / TimeHelper.ONE_MINUTE);
			int gap = m_dataExtractor.calculateInterval(length);

			payload.setInterval(gap + "m");
		}
	}
}
