package com.dianping.cat.api.page.metric;

import com.dianping.cat.api.ApiPage;

import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<ApiPage, Action, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}
}
