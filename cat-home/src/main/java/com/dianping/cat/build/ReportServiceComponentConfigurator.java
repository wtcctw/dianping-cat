package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.report.page.app.service.AppReportService;
import com.dianping.cat.report.page.business.service.BusinessReportService;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.dependency.service.DependencyReportService;
import com.dianping.cat.report.page.event.service.EventReportService;
import com.dianping.cat.report.page.heartbeat.service.HeartbeatReportService;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.network.service.NetTopologyReportService;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.page.transaction.HistoryGraphs;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.system.page.router.service.RouterConfigService;

public class ReportServiceComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(TransactionMergeHelper.class));
		all.add(A(TransactionReportService.class));
		all.add(A(HistoryGraphs.class));
		all.add(A(EventReportService.class));
		all.add(A(ProblemReportService.class));
		all.add(A(MatrixReportService.class));
		all.add(A(CrossReportService.class));
		all.add(A(StateReportService.class));
		all.add(A(StorageReportService.class));

		all.add(A(UtilizationReportService.class));
		all.add(A(ServiceReportService.class));
		all.add(A(HeavyReportService.class));
		all.add(A(NetTopologyReportService.class));
		all.add(A(RouterConfigService.class));
		all.add(A(JarReportService.class));
		all.add(A(ClientReportService.class));

		all.add(A(TopReportService.class));
		all.add(A(DependencyReportService.class));
		all.add(A(HeartbeatReportService.class));
		all.add(A(MetricReportService.class));
		all.add(A(BusinessReportService.class));

		all.add(A(AppReportService.class));

		return all;
	}
}
