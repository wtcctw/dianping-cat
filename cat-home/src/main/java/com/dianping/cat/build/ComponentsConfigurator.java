package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.dal.jdbc.mapping.TableProvider;
import org.unidal.initialization.DefaultModuleManager;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatHomeModule;
import com.dianping.cat.alarm.UserDefineRuleDao;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppConnectionDataDao;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.config.app.AppCmdDailyTableProvider;
import com.dianping.cat.config.app.AppCommandTableProvider;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.AppConnectionTableProvider;
import com.dianping.cat.config.app.AppSpeedTableProvider;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.web.AjaxDataTableProvider;
import com.dianping.cat.config.web.WebSpeedDataTableProvider;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.core.config.BusinessConfigDao;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.dal.DailyReportContentDao;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dal.report.MetricGraphDao;
import com.dianping.cat.home.dal.report.MetricScreenDao;
import com.dianping.cat.home.dal.report.TopologyGraphDao;
import com.dianping.cat.influxdb.InfluxDB;
import com.dianping.cat.metric.MetricService;
import com.dianping.cat.mvc.PayloadNormalizer;
import com.dianping.cat.report.HourlyReportTableProvider;
import com.dianping.cat.report.alert.app.AppRuleConfigManager;
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
import com.dianping.cat.report.graph.metric.CachedMetricReportService;
import com.dianping.cat.report.graph.metric.DataExtractor;
import com.dianping.cat.report.graph.metric.MetricDataFetcher;
import com.dianping.cat.report.graph.metric.impl.CachedMetricReportServiceImpl;
import com.dianping.cat.report.graph.metric.impl.DataExtractorImpl;
import com.dianping.cat.report.graph.metric.impl.MetricDataFetcherImpl;
import com.dianping.cat.report.graph.svg.DefaultGraphBuilder;
import com.dianping.cat.report.graph.svg.DefaultValueTranslater;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.graph.svg.ValueTranslater;
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
import com.dianping.cat.report.page.metric.service.MetricReportService;
import com.dianping.cat.report.page.network.config.NetGraphConfigManager;
import com.dianping.cat.report.page.server.config.ServerMetricConfigManager;
import com.dianping.cat.report.page.server.display.LineChartBuilder;
import com.dianping.cat.report.page.server.display.MetricScreenTransformer;
import com.dianping.cat.report.page.server.service.MetricGraphBuilder;
import com.dianping.cat.report.page.server.service.MetricGraphService;
import com.dianping.cat.report.page.server.service.MetricScreenService;
import com.dianping.cat.report.page.state.StateGraphBuilder;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.display.StorageAlertInfoBuilder;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.report.task.cmdb.ProjectUpdateTask;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.RouterConfigService;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	private List<Component> defineCommonComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(JsonBuilder.class));

		all.add(C(ValueTranslater.class, DefaultValueTranslater.class));
		all.add(C(GraphBuilder.class, DefaultGraphBuilder.class) //
		      .req(ValueTranslater.class));

		all.add(C(PayloadNormalizer.class).req(ServerConfigManager.class));

		all.add(C(StateGraphBuilder.class, StateGraphBuilder.class).//
		      req(StateReportService.class, ServerFilterConfigManager.class));

		all.add(C(DependencyItemBuilder.class).req(TopologyGraphConfigManager.class));

		all.add(C(TopologyGraphBuilder.class).req(DependencyItemBuilder.class));

		all.add(C(TopologyGraphManager.class)
		      .req(TopologyGraphBuilder.class, DependencyItemBuilder.class, ServerConfigManager.class,
		            ServerFilterConfigManager.class)
		      .req(TopoGraphFormatConfigManager.class, TopologyGraphDao.class, ProjectService.class)
		      .req(ModelService.class, DependencyAnalyzer.ID));

		// update project database
		all.add(C(ProjectUpdateTask.class).req(ProjectService.class, HostinfoService.class)//
		      .req(TransactionReportService.class));

		all.add(C(StorageAlertInfoBuilder.class).req(AlertService.class));

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

		all.add(C(TableProvider.class, "app-command-data", AppCommandTableProvider.class));
		all.add(C(TableProvider.class, "app-command-data-daily", AppCmdDailyTableProvider.class));
		all.add(C(TableProvider.class, "app-connection-data", AppConnectionTableProvider.class));
		all.add(C(TableProvider.class, "app-speed-data", AppSpeedTableProvider.class));
		all.add(C(TableProvider.class, "ajax-data", AjaxDataTableProvider.class));
		all.add(C(TableProvider.class, "web-speed-data", WebSpeedDataTableProvider.class));
		all.add(C(TableProvider.class, "report", HourlyReportTableProvider.class));

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));
		all.addAll(new CatDatabaseConfigurator().defineComponents());

		// for alarm module
		all.addAll(new AlarmComponentConfigurator().defineComponents());

		// web, please keep it last
		all.addAll(new WebComponentConfigurator().defineComponents());

		return all;
	}

	private List<Component> defineConfigComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(BaseRuleHelper.class));
		all.add(C(UserDefinedRuleManager.class).req(UserDefineRuleDao.class));
		all.add(C(TopologyGraphConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ExceptionRuleConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(DomainGroupConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(NetworkRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      BaseRuleHelper.class, ContentFetcher.class));
		all.add(C(BusinessRuleConfigManager.class).req(ConfigDao.class, MetricConfigManager.class,
		      UserDefinedRuleManager.class, BaseRuleHelper.class, ContentFetcher.class));
		all.add(C(BusinessRuleConfigManager2.class).req(BusinessConfigDao.class));
		all.add(C(AppRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class, BaseRuleHelper.class,
		      ContentFetcher.class));
		all.add(C(TransactionRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      BaseRuleHelper.class, ContentFetcher.class));
		all.add(C(EventRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class, BaseRuleHelper.class,
		      ContentFetcher.class));
		all.add(C(HeartbeatRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      BaseRuleHelper.class, ContentFetcher.class));
		all.add(C(SystemRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class, BaseRuleHelper.class,
		      ContentFetcher.class));
		all.add(C(StorageSQLRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      BaseRuleHelper.class, ContentFetcher.class));
		all.add(C(StorageGroupConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(StorageCacheRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      BaseRuleHelper.class, ContentFetcher.class));
		all.add(C(StorageRPCRuleConfigManager.class).req(ConfigDao.class, UserDefinedRuleManager.class,
		      BaseRuleHelper.class, ContentFetcher.class));
		all.add(C(AlertConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(JsRuleConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(NetGraphConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ThirdPartyConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ThirdPartyConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(RouterConfigManager.class).req(ConfigDao.class, ContentFetcher.class, DailyReportDao.class,
		      DailyReportContentDao.class));
		all.add(C(RouterConfigHandler.class).req(StateReportService.class, RouterConfigService.class,
		      RouterConfigManager.class, DailyReportDao.class));
		all.add(C(TopoGraphFormatConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(EsServerConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(ServerMetricConfigManager.class).req(ConfigDao.class, ContentFetcher.class));

		return all;
	}

	private List<Component> defineMetricComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(CachedMetricReportService.class, CachedMetricReportServiceImpl.class)
		      .req(ModelService.class, MetricAnalyzer.ID).req(MetricReportService.class).req(IpService.class));
		all.add(C(DataExtractor.class, DataExtractorImpl.class));
		all.add(C(MetricDataFetcher.class, MetricDataFetcherImpl.class));

		all.add(C(AppSpeedDataBuilder.class).req(AppSpeedDataDao.class, AppCommandConfigManager.class,
		      MobileConfigManager.class));
		all.add(C(AppSpeedService.class).req(AppSpeedDataDao.class, AppSpeedDataBuilder.class));

		all.add(C(AppDataService.class).req(AppCommandDataDao.class, AppCommandConfigManager.class));
		all.add(C(AppConnectionService.class).req(AppConnectionDataDao.class, AppCommandConfigManager.class));

		all.add(C(GraphBuilder.class));
		all.add(C(MetricScreenTransformer.class));
		all.add(C(MetricScreenService.class).req(MetricScreenDao.class,
		      com.dianping.cat.report.page.server.service.MetricGraphBuilder.class, MetricScreenTransformer.class));
		all.add(C(MetricGraphService.class).req(MetricGraphDao.class));
		all.add(C(LineChartBuilder.class).req(MetricService.class, InfluxDB.ID).req(MetricGraphBuilder.class));

		return all;
	}
}
