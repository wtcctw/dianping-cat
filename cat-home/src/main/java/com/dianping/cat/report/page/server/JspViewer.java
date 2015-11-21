package com.dianping.cat.report.page.server;

import com.dianping.cat.report.ReportPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		case SCREEN:
			return JspFile.SCREEN.getPath();
		case AGGREGATE:
			return JspFile.AGGREGATE.getPath();
		case ENDPOINT:
		case MEASUREMTN:
		case BUILDVIEW:
			return JspFile.JSON.getPath();
		case SCREEN_UPDATE:
			return JspFile.SCREEN_CONFIG_UPDATE.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
