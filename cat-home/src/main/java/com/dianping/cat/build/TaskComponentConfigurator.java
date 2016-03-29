package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.report.page.app.task.AppDatabasePruner;
import com.dianping.cat.report.page.app.task.AppReportBuilder;
import com.dianping.cat.report.page.app.task.CommandAutoCompleter;
import com.dianping.cat.report.page.browser.task.WebDatabasePruner;
import com.dianping.cat.report.page.business.task.BusinessBaselineReportBuilder;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.report.page.business.task.BusinessPointParser;
import com.dianping.cat.report.page.cross.task.CrossReportBuilder;
import com.dianping.cat.report.page.dependency.task.DependencyReportBuilder;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.page.heartbeat.task.HeartbeatReportBuilder;
import com.dianping.cat.report.page.matrix.task.MatrixReportBuilder;
import com.dianping.cat.report.page.metric.service.DefaultBaselineService;
import com.dianping.cat.report.page.metric.task.BaselineConfigManager;
import com.dianping.cat.report.page.metric.task.BaselineCreator;
import com.dianping.cat.report.page.metric.task.DefaultBaselineCreator;
import com.dianping.cat.report.page.metric.task.MetricBaselineReportBuilder;
import com.dianping.cat.report.page.metric.task.MetricPointParser;
import com.dianping.cat.report.page.network.task.NetTopologyReportBuilder;
import com.dianping.cat.report.page.overload.task.CapacityUpdateStatusManager;
import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.page.overload.task.DailyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.HourlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.MonthlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.TableCapacityService;
import com.dianping.cat.report.page.overload.task.WeeklyCapacityUpdater;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.page.server.task.MetricGraphPruner;
import com.dianping.cat.report.page.state.task.StateReportBuilder;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportBuilder;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ClientReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportBuilder;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.report.page.storage.task.StorageReportBuilder;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;
import com.dianping.cat.report.task.DefaultTaskConsumer;
import com.dianping.cat.report.task.ReportFacade;
import com.dianping.cat.report.task.cmdb.CmdbInfoReloadBuilder;
import com.dianping.cat.report.task.current.CurrentReportBuilder;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

public class TaskComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(MetricPointParser.class));
		all.add(C(BaselineConfigManager.class));
		all.add(C(BusinessPointParser.class));
		all.add(C(BusinessKeyHelper.class));

		all.add(C(BaselineCreator.class, DefaultBaselineCreator.class));
		all.add(A(DefaultBaselineService.class));

		all.add(A(MetricBaselineReportBuilder.class));

		all.add(A(BusinessBaselineReportBuilder.class));

		all.add(A(TransactionReportBuilder.class));

		all.add(A(EventReportBuilder.class));

		all.add(A(ProblemReportBuilder.class));

		all.add(A(HeartbeatReportBuilder.class));

		all.add(A(ServiceReportBuilder.class));

		all.add(A(MatrixReportBuilder.class));

		all.add(A(CrossReportBuilder.class));

		all.add(A(StateReportBuilder.class));

		all.add(A(RouterConfigBuilder.class));

		all.add(A(HeavyReportBuilder.class));

		all.add(A(UtilizationReportBuilder.class));

		all.add(A(DependencyReportBuilder.class));

		all.add(A(NetTopologyReportBuilder.class));

		all.add(A(JarReportBuilder.class));

		all.add(A(ClientReportBuilder.class));

		all.add(A(CurrentReportBuilder.class));

		all.add(A(StorageReportBuilder.class));

		all.add(A(CmdbInfoReloadBuilder.class));

		all.add(A(CapacityUpdateStatusManager.class));

		all.add(A(HourlyCapacityUpdater.class));

		all.add(A(DailyCapacityUpdater.class));

		all.add(A(WeeklyCapacityUpdater.class));

		all.add(A(MonthlyCapacityUpdater.class));

		all.add(A(TableCapacityService.class));

		all.add(A(CapacityUpdateTask.class));

		all.add(A(AppDatabasePruner.class));

		all.add(A(WebDatabasePruner.class));

		all.add(A(MetricGraphPruner.class));

		all.add(A(CommandAutoCompleter.class));

		all.add(A(AppReportBuilder.class));

		all.add(C(ReportFacade.class));

		all.add(A(DefaultTaskConsumer.class));

		return all;
	}
}
