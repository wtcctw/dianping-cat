package com.dianping.cat.report.page.browser;

import java.util.Collection;

import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;

public class Model extends AbstractReportModel<Action, ReportPage, Context> {
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.JS_ERROR;
	}

	@Override
   public String getDomain() {
		return getDisplayDomain();
   }

	@Override
   public Collection<String> getDomains() {
	   // TODO Auto-generated method stub
	   return null;
   }
}
