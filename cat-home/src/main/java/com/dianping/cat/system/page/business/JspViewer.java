package com.dianping.cat.system.page.business;

import com.dianping.cat.system.SystemPage;
import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<SystemPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case DELETE:
		case LIST:
		case AlertRuleAddSubmit:
			return JspFile.VIEW.getPath();
		case ADD:
		case AddSubmit:
			return JspFile.ADD.getPath();
		case AlertRuleAdd:
			return JspFile.AlertAdd.getPath();
		case TagConfig:
			return JspFile.TAG.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
