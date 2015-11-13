package com.dianping.cat.system.page.web;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case SPEED_DELETE:
		case SPEED_LIST:
		case SPEED_SUBMIT:
			return JspFile.SPEED_LIST.getPath();
		case SPEED_UPDATE:
			return JspFile.SPEED_UPDATE.getPath();
		default:
			break;
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
