package com.dianping.cat.report.page.app;

import org.unidal.web.mvc.view.BaseJspViewer;

import com.dianping.cat.report.ReportPage;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case LINECHART:
			return JspFile.VIEW.getPath();
		case PIECHART:
			return JspFile.PIECHART.getPath();
		case CONN_LINECHART:
			return JspFile.CONN_LINECHART.getPath();
		case CONN_PIECHART:
			return JspFile.CONN_PIECHART.getPath();
		case APP_ADD:
		case APP_DELETE:
			return JspFile.APP_MODIFY_RESULT.getPath();
		case LINECHART_JSON:
		case PIECHART_JSON:
		case SPEED_JSON:
		case CONN_LINECHART_JSON:
		case CONN_PIECHART_JSON:
		case APP_CONFIG_FETCH:
			return JspFile.APP_FETCH_DATA.getPath();
		case HOURLY_CRASH_LOG:
		case HISTORY_CRASH_LOG:
			return JspFile.CRASH_LOG.getPath();
		case APP_CRASH_LOG:
			return JspFile.APP_CRASH_LOG.getPath();
		case APP_CRASH_LOG_DETAIL:
			return JspFile.APP_CRASH_LOG_DETAIL.getPath();
		case SPEED:
			return JspFile.SPEED.getPath();
		case SPEED_GRAPH:
			return JspFile.SPEED_GRAPH.getPath();
		case APP_COMMAND_DAILY:
			return JspFile.APP_COMMAND_DAILY.getPath();
		case STATISTICS:
			return JspFile.STATISTICS.getPath();
		case DASHBOARD:
			return JspFile.DASHBOARD.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
