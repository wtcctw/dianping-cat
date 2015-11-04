package com.dianping.cat.report.page.server;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

import com.dianping.cat.influxdb.InfluxDBManager;
import com.dianping.cat.influxdb.dto.QueryResult;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.server.config.ScreenConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class Handler implements PageHandler<Context> {

	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private InfluxDBManager m_influxDBManager;

	@Inject
	private ScreenConfigManager m_screenConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

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
			QueryResult result = m_influxDBManager.query("SELECT * FROM cpu");

			System.out.println(result);

			break;
		case SCREEN:

			break;
		case CONFIG_UPDATE:
			String config = payload.getConfig();

			if (!StringUtils.isEmpty(config)) {
				model.setOpState(m_screenConfigManager.insert(config));
			}
			model.setConfig(m_configHtmlParser.parse(m_screenConfigManager.getConfig().toString()));
			break;
		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void normalize(Payload payload, Model model) {
		model.setAction(payload.getAction());
		model.setPage(ReportPage.SERVER);
	}
}
