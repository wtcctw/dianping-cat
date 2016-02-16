package com.dianping.cat.api.page.metric;

import com.dianping.cat.api.ApiPage;

import org.unidal.web.mvc.view.BaseJspViewer;

public class JspViewer extends BaseJspViewer<ApiPage, Action, Context, Model> {
	@Override
	protected String getJspFilePath(Context ctx, Model model) {
		Action action = model.getAction();

		switch (action) {
		case VIEW:
			return JspFile.VIEW.getPath();
		}

		throw new RuntimeException("Unknown action: " + action);
	}
}
