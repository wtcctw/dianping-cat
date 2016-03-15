package com.dianping.cat.report.page.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.business.graph.BusinessGraphCreator;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private PayloadNormalizer m_normalizePayload;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private BusinessGraphCreator m_graphCreator;

	@Inject
	private BusinessTagConfigManager m_tagConfigManager;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "business")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "business")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		normalize(model, payload);

		long start = payload.getHistoryStartDate().getTime();
		long end = payload.getHistoryEndDate().getTime();
		start = start - start % TimeHelper.ONE_HOUR;
		end = end - end % TimeHelper.ONE_HOUR;
		Date startDate = new Date(start);
		Date endDate = new Date(end);

		model.setStartTime(startDate);
		model.setEndTime(endDate);

		switch (action) {
		case VIEW:
			Map<String, LineChart> allCharts = buildLineCharts(payload, startDate, endDate);

			model.setLineCharts(new ArrayList<LineChart>(allCharts.values()));
			break;
		}
		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private Map<String, LineChart> buildLineCharts(Payload payload, Date start, Date end) {
		Map<String, LineChart> allCharts = null;
		Type type = Type.getType(payload.getType());
		String name = payload.getDomain();

		if (type == Type.Tag) {
			allCharts = m_graphCreator.buildDashboardByTag(start, end, name);
		} else {
			allCharts = m_graphCreator.buildDashboardByDomain(start, end, name);
		}

		return allCharts;
	}

	private void normalize(Model model, Payload payload) {
		model.setDomains(m_projectService.findAllDomains());
		model.setTags(m_tagConfigManager.findAllTags());
		model.setPage(ReportPage.BUSINESS);
		model.setAction(payload.getAction());
		m_normalizePayload.normalize(model, payload);
	}
}
