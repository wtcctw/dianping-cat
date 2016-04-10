package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.app.AppContactor;
import com.dianping.cat.report.alert.app.AppDecorator;
import com.dianping.cat.report.page.app.display.AppStatisticPiechartBuilder;
import com.dianping.cat.report.page.app.service.AppConnectionService;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.AppReportService;
import com.dianping.cat.report.page.app.service.AppSpeedDataBuilder;
import com.dianping.cat.report.page.app.service.AppSpeedService;
import com.dianping.cat.report.page.app.task.AppCommandAutoCompleter;
import com.dianping.cat.report.page.app.task.AppDatabasePruner;
import com.dianping.cat.report.page.app.task.AppReportBuilder;
import com.dianping.cat.service.ProjectService;

public class AppComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(AppSpeedDataBuilder.class));
		all.add(A(AppSpeedService.class));

		all.add(A(AppDataService.class));

		all.add(A(AppConnectionService.class));

		all.add(A(AppReportService.class));

		all.add(A(AppDatabasePruner.class));

		all.add(A(AppCommandAutoCompleter.class));

		all.add(A(AppReportBuilder.class));

		all.add(A(AppStatisticPiechartBuilder.class));

		all.add(C(Contactor.class, AppContactor.ID, AppContactor.class).req(AlertConfigManager.class,
		      AppConfigManager.class, ProjectService.class));
		all.add(C(Decorator.class, AppDecorator.ID, AppDecorator.class));

		all.add(A(AppAlert.class));

		return all;
	}
}
