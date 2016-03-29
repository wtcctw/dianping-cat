package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.message.storage.LocalMessageBucket;
import com.dianping.cat.report.DefaultReportBucketManager;
import com.dianping.cat.report.LocalReportBucket;

class StorageComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultReportBucketManager.class));

		all.add(A(LocalReportBucket.class));

		all.add(A(LocalMessageBucket.class));

		return all;
	}
}
