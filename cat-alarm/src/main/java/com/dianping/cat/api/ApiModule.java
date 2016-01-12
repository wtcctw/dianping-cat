package com.dianping.cat.api;

import org.unidal.web.mvc.AbstractModule;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.ModulePagesMeta;

@ModuleMeta(name = "api", defaultInboundAction = "metric", defaultTransition = "default", defaultErrorAction = "default")
@ModulePagesMeta({
com.dianping.cat.api.page.metric.Handler.class
})
public class ApiModule extends AbstractModule {

}
