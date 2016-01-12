package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.api.ApiModule;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineModuleRegistry(all, ApiModule.class, ApiModule.class);

		return all;
	}
}
