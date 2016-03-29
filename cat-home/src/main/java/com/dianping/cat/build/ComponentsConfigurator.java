package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.initialization.DefaultModuleManager;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatHomeModule;
import com.dianping.cat.alarm.build.AlarmComponentConfigurator;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.alert.browser.JsRuleConfigManager;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.report.alert.business2.BusinessRuleConfigManager2;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.alert.network.NetworkRuleConfigManager;
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;
import com.dianping.cat.report.alert.storage.cache.StorageCacheRuleConfigManager;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCRuleConfigManager;
import com.dianping.cat.report.alert.storage.sql.StorageSQLRuleConfigManager;
import com.dianping.cat.report.alert.system.SystemRuleConfigManager;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyConfigManager;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.graph.metric.impl.CachedMetricReportServiceImpl;
import com.dianping.cat.report.graph.metric.impl.DataExtractorImpl;
import com.dianping.cat.report.graph.metric.impl.MetricDataFetcherImpl;
import com.dianping.cat.report.graph.svg.DefaultGraphBuilder;
import com.dianping.cat.report.graph.svg.DefaultValueTranslater;
import com.dianping.cat.report.page.DomainGroupConfigManager;
import com.dianping.cat.report.page.app.service.AppConnectionService;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.app.service.AppSpeedDataBuilder;
import com.dianping.cat.report.page.app.service.AppSpeedService;
import com.dianping.cat.report.page.dependency.config.TopoGraphFormatConfigManager;
import com.dianping.cat.report.page.dependency.graph.DependencyItemBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphBuilder;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphConfigManager;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.eslog.EsServerConfigManager;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.report.page.server.config.ServerMetricConfigManager;
import com.dianping.cat.report.page.server.display.LineChartBuilder;
import com.dianping.cat.report.page.server.display.MetricScreenTransformer;
import com.dianping.cat.report.page.server.service.MetricGraphService;
import com.dianping.cat.report.page.server.service.MetricScreenService;
import com.dianping.cat.report.page.state.StateGraphBuilder;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	private List<Component> defineCommonComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(JsonBuilder.class));

		all.add(A(DefaultValueTranslater.class));
		all.add(A(DefaultGraphBuilder.class));

		all.add(A(PayloadNormalizer.class));

		all.add(A(StateGraphBuilder.class));

		all.add(A(DependencyItemBuilder.class));

		all.add(A(TopologyGraphBuilder.class));

		all.add(A(TopologyGraphManager.class));

		all.add(A(ProjectUpdateTask.class));

		all.add(A(StorageAlertInfoBuilder.class));

		return all;
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineCommonComponents());

		all.addAll(defineConfigComponents());

		all.addAll(defineMetricComponents());

		all.add(C(Module.class, CatHomeModule.ID, CatHomeModule.class));

		all.add(C(ModuleManager.class, DefaultModuleManager.class) //
		      .config(E("topLevelModules").value(CatHomeModule.ID)));

		// report serivce
		all.addAll(new ReportServiceComponentConfigurator().defineComponents());
		// task
		all.addAll(new TaskComponentConfigurator().defineComponents());

		// model service
		all.addAll(new ServiceComponentConfigurator().defineComponents());

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));
		all.addAll(new CatDatabaseConfigurator().defineComponents());

		// for alarm module
		all.addAll(new AlarmComponentConfigurator().defineComponents());

		// for alarm module
		all.addAll(new HomeAlarmComponentConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private List<Component> defineConfigComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(BaseRuleHelper.class));
		all.add(A(UserDefinedRuleManager.class));
		all.add(A(TopologyGraphConfigManager.class));
		all.add(A(ExceptionRuleConfigManager.class));
		all.add(A(DomainGroupConfigManager.class));
		all.add(A(NetworkRuleConfigManager.class));
		all.add(A(BusinessRuleConfigManager.class));
		all.add(A(BusinessRuleConfigManager2.class));
		all.add(A(TransactionRuleConfigManager.class));
		all.add(A(EventRuleConfigManager.class));
		all.add(A(HeartbeatRuleConfigManager.class));
		all.add(A(SystemRuleConfigManager.class));
		all.add(A(StorageSQLRuleConfigManager.class));
		all.add(A(StorageGroupConfigManager.class));
		all.add(A(StorageCacheRuleConfigManager.class));
		all.add(A(StorageRPCRuleConfigManager.class));
		all.add(A(AlertConfigManager.class));
		all.add(A(JsRuleConfigManager.class));
		all.add(A(NetGraphConfigManager.class));
		all.add(A(ThirdPartyConfigManager.class));
		all.add(A(RouterConfigManager.class));
		all.add(A(RouterConfigHandler.class));
		all.add(A(TopoGraphFormatConfigManager.class));
		all.add(A(EsServerConfigManager.class));
		all.add(A(ServerMetricConfigManager.class));

		return all;
	}

	private List<Component> defineMetricComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(CachedMetricReportServiceImpl.class));
		all.add(A(DataExtractorImpl.class));
		all.add(A(MetricDataFetcherImpl.class));

		all.add(A(AppSpeedDataBuilder.class));
		all.add(A(AppSpeedService.class));

		all.add(A(AppDataService.class));
		all.add(A(AppConnectionService.class));

		all.add(A(MetricScreenTransformer.class));
		all.add(A(MetricScreenService.class));
		all.add(A(MetricGraphService.class));
		all.add(A(LineChartBuilder.class));

		return all;
	}
}
