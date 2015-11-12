package com.dianping.cat.report.page.server;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.metric.MetricType;
import com.dianping.cat.metric.QueryParameter;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.server.config.ScreenConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ScreenConfigManager m_screenConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private MetricService m_metricService;

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

		switch (action) {
		case VIEW:
			break;
		case SCREEN:
			long date = payload.getDate();
			int timeRange = payload.getTimeRange();
			Date start = new Date(date - (timeRange - 1) * TimeHelper.ONE_HOUR);
			Date end = new Date(date + TimeHelper.ONE_HOUR);
			QueryParameter parameter = new QueryParameter();

			parameter.setCategory("system").setStart(start).setEnd(end).setMeasurement("userCpu").setType(MetricType.AVG)
			      .setInterval("1m");

			Map<Date, Double> result = m_metricService.query(parameter);

			System.out.println(result);

			model.setScreens(m_screenConfigManager.getConfig().getScreens().values());
			break;
		case CONFIG_UPDATE:
			String config = payload.getContent();

			if (!StringUtils.isEmpty(config)) {
				model.setOpState(m_screenConfigManager.insert(config));
			}
			model.setConfig(m_configHtmlParser.parse(m_screenConfigManager.getConfig().toString()));
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

		int timeRange = payload.getTimeRange();
		Date startTime = new Date(payload.getDate() - (timeRange - 1) * TimeHelper.ONE_HOUR);
		Date endTime = new Date(payload.getDate() + TimeHelper.ONE_HOUR - 1);

		model.setStartTime(startTime);
		model.setEndTime(endTime);

		String screen = payload.getScreen();

		if (StringUtils.isEmpty(screen)) {
			screen = m_screenConfigManager.getConfig().getScreens().keySet().iterator().next();

			payload.setScreen(screen);
		}

	}
}
