package com.dianping.cat.report.page.browser;

import com.dianping.cat.report.ReportPage;

import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ReportPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case JS_ERROR:
			return JspFile.JS_ERROR.getPath();
		case VIEW:
			return JspFile.VIEW.getPath();
		case PIECHART:
			return JspFile.PIECHART.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
