package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.home.dal.report.AlertDao;
import com.dianping.cat.home.dal.report.AlertSummaryDao;
import com.dianping.cat.home.dal.report.AlterationDao;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.app.AppContactor;
import com.dianping.cat.report.alert.app.AppDecorator;
import com.dianping.cat.report.alert.app.AppRuleConfigManager;
import com.dianping.cat.report.alert.browser.AjaxAlert;
import com.dianping.cat.report.alert.browser.AjaxContactor;
import com.dianping.cat.report.alert.browser.AjaxDecorator;
import com.dianping.cat.report.alert.browser.AjaxRuleConfigManager;
import com.dianping.cat.report.alert.browser.JsAlert;
import com.dianping.cat.report.alert.browser.JsContactor;
import com.dianping.cat.report.alert.browser.JsDecorator;
import com.dianping.cat.report.alert.browser.JsRuleConfigManager;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.business.BusinessContactor;
import com.dianping.cat.report.alert.business.BusinessDecorator;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.report.alert.business2.BusinessAlert2;
import com.dianping.cat.report.alert.business2.BusinessContactor2;
import com.dianping.cat.report.alert.business2.BusinessDecorator2;
import com.dianping.cat.report.alert.business2.BusinessReportGroupService;
import com.dianping.cat.report.alert.business2.BusinessRuleConfigManager2;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.database.DatabaseAlert;
import com.dianping.cat.report.alert.database.DatabaseContactor;
import com.dianping.cat.report.alert.database.DatabaseDecorator;
import com.dianping.cat.report.alert.database.DatabaseRuleConfigManager;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.event.EventContactor;
import com.dianping.cat.report.alert.event.EventDecorator;
import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.exception.ExceptionContactor;
import com.dianping.cat.report.alert.exception.ExceptionDecorator;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatContactor;
import com.dianping.cat.report.alert.heartbeat.HeartbeatDecorator;
import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.alert.network.NetworkAlert;
import com.dianping.cat.report.alert.network.NetworkContactor;
import com.dianping.cat.report.alert.network.NetworkDecorator;
import com.dianping.cat.report.alert.network.NetworkRuleConfigManager;
import com.dianping.cat.report.alert.service.AlertService;
import com.dianping.cat.report.alert.spi.AlertManager;
import com.dianping.cat.report.alert.spi.config.AlertConfigManager;
import com.dianping.cat.report.alert.spi.config.AlertPolicyManager;
import com.dianping.cat.report.alert.spi.config.SenderConfigManager;
import com.dianping.cat.report.alert.spi.data.MetricReportGroupService;
import com.dianping.cat.report.alert.spi.decorator.Decorator;
import com.dianping.cat.report.alert.spi.decorator.DecoratorManager;
import com.dianping.cat.report.alert.spi.receiver.Contactor;
import com.dianping.cat.report.alert.spi.receiver.ContactorManager;
import com.dianping.cat.report.alert.spi.rule.DataChecker;
import com.dianping.cat.report.alert.spi.rule.DefaultDataChecker;
import com.dianping.cat.report.alert.spi.sender.MailSender;
import com.dianping.cat.report.alert.spi.sender.Sender;
import com.dianping.cat.report.alert.spi.sender.SenderManager;
import com.dianping.cat.report.alert.spi.sender.SmsSender;
import com.dianping.cat.report.alert.spi.sender.WeixinSender;
import com.dianping.cat.report.alert.spi.spliter.MailSpliter;
import com.dianping.cat.report.alert.spi.spliter.SmsSpliter;
import com.dianping.cat.report.alert.spi.spliter.Spliter;
import com.dianping.cat.report.alert.spi.spliter.SpliterManager;
import com.dianping.cat.report.alert.spi.spliter.WeixinSpliter;
import com.dianping.cat.report.alert.storage.cache.StorageCacheAlert;
import com.dianping.cat.report.alert.storage.cache.StorageCacheContactor;
import com.dianping.cat.report.alert.storage.cache.StorageCacheDecorator;
import com.dianping.cat.report.alert.storage.cache.StorageCacheRuleConfigManager;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCAlert;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCContactor;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCDecorator;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCRuleConfigManager;
import com.dianping.cat.report.alert.storage.sql.StorageSQLAlert;
import com.dianping.cat.report.alert.storage.sql.StorageSQLContactor;
import com.dianping.cat.report.alert.storage.sql.StorageSQLDecorator;
import com.dianping.cat.report.alert.storage.sql.StorageSQLRuleConfigManager;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.alert.summary.AlertSummaryService;
import com.dianping.cat.report.alert.summary.build.AlertInfoBuilder;
import com.dianping.cat.report.alert.summary.build.AlterationSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.FailureSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.RelatedSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.SummaryBuilder;
import com.dianping.cat.report.alert.system.SystemAlert;
import com.dianping.cat.report.alert.system.SystemContactor;
import com.dianping.cat.report.alert.system.SystemDecorator;
import com.dianping.cat.report.alert.system.SystemRuleConfigManager;
import com.dianping.cat.report.alert.thirdParty.HttpConnector;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyConfigManager;
import com.dianping.cat.report.alert.thirdParty.ThirdpartyContactor;
import com.dianping.cat.report.alert.thirdParty.ThirdpartyDecorator;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.transaction.TransactionContactor;
import com.dianping.cat.report.alert.transaction.TransactionDecorator;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.page.app.service.AppDataService;
import com.dianping.cat.report.page.browser.service.AjaxDataService;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;
import com.dianping.cat.report.page.event.transform.EventMergeHelper;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.report.page.metric.service.BaselineService;
import com.dianping.cat.report.page.storage.config.StorageGroupConfigManager;
import com.dianping.cat.report.page.storage.transform.StorageMergeHelper;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.web.JsErrorLogDao;

public class AlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(DataChecker.class, DefaultDataChecker.class));
		all.add(C(MetricReportGroupService.class).req(ModelService.class, MetricAnalyzer.ID));
		all.add(C(BusinessReportGroupService.class).req(ModelService.class, BusinessAnalyzer.ID));

		all.add(C(Contactor.class, BusinessContactor.ID, BusinessContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, BusinessContactor2.ID, BusinessContactor2.class).req(ProjectService.class,
		      AlertConfigManager.class));

		all.add(C(Contactor.class, NetworkContactor.ID, NetworkContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, DatabaseContactor.ID, DatabaseContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, SystemContactor.ID, SystemContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, ExceptionContactor.ID, ExceptionContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, HeartbeatContactor.ID, HeartbeatContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, ThirdpartyContactor.ID, ThirdpartyContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, JsContactor.ID, JsContactor.class).req(JsRuleConfigManager.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, AppContactor.ID, AppContactor.class).req(AlertConfigManager.class,
		      AppConfigManager.class, ProjectService.class));
		all.add(C(Contactor.class, AjaxContactor.ID, AjaxContactor.class).req(AlertConfigManager.class,
		      ProjectService.class, UrlPatternConfigManager.class));
		all.add(C(Contactor.class, TransactionContactor.ID, TransactionContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, EventContactor.ID, EventContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, StorageSQLContactor.ID, StorageSQLContactor.class).req(AlertConfigManager.class));
		all.add(C(Contactor.class, StorageCacheContactor.ID, StorageCacheContactor.class).req(AlertConfigManager.class));
		all.add(C(Contactor.class, StorageRPCContactor.ID, StorageRPCContactor.class).req(AlertConfigManager.class));
		all.add(C(ContactorManager.class));

		all.add(C(Decorator.class, BusinessDecorator.ID, BusinessDecorator.class).req(ProductLineConfigManager.class,
		      AlertSummaryExecutor.class, ProjectService.class));
		all.add(C(Decorator.class, BusinessDecorator2.ID, BusinessDecorator2.class).req(ProjectService.class));
		all.add(C(Decorator.class, NetworkDecorator.ID, NetworkDecorator.class));
		all.add(C(Decorator.class, DatabaseDecorator.ID, DatabaseDecorator.class));
		all.add(C(Decorator.class, HeartbeatDecorator.ID, HeartbeatDecorator.class));
		all.add(C(Decorator.class, ExceptionDecorator.ID, ExceptionDecorator.class).req(ProjectService.class,
		      AlertSummaryExecutor.class));
		all.add(C(Decorator.class, SystemDecorator.ID, SystemDecorator.class));
		all.add(C(Decorator.class, ThirdpartyDecorator.ID, ThirdpartyDecorator.class).req(ProjectService.class));
		all.add(C(Decorator.class, AppDecorator.ID, AppDecorator.class));
		all.add(C(Decorator.class, AjaxDecorator.ID, AjaxDecorator.class));
		all.add(C(Decorator.class, JsDecorator.ID, JsDecorator.class));
		all.add(C(Decorator.class, TransactionDecorator.ID, TransactionDecorator.class));
		all.add(C(Decorator.class, EventDecorator.ID, EventDecorator.class));
		all.add(C(Decorator.class, StorageSQLDecorator.ID, StorageSQLDecorator.class));
		all.add(C(Decorator.class, StorageCacheDecorator.ID, StorageCacheDecorator.class));
		all.add(C(Decorator.class, StorageRPCDecorator.ID, StorageRPCDecorator.class));

		all.add(C(DecoratorManager.class));

		all.add(C(AlertPolicyManager.class).req(ConfigDao.class, ContentFetcher.class));

		all.add(C(Spliter.class, MailSpliter.ID, MailSpliter.class));

		all.add(C(Spliter.class, SmsSpliter.ID, SmsSpliter.class));

		all.add(C(Spliter.class, WeixinSpliter.ID, WeixinSpliter.class));

		all.add(C(SpliterManager.class));

		all.add(C(Sender.class, MailSender.ID, MailSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, SmsSender.ID, SmsSender.class).req(SenderConfigManager.class));

		all.add(C(Sender.class, WeixinSender.ID, WeixinSender.class).req(SenderConfigManager.class));

		all.add(C(SenderManager.class).req(ServerConfigManager.class));

		all.add(C(AlertManager.class).req(AlertPolicyManager.class, DecoratorManager.class, ContactorManager.class,
		      AlertService.class, SpliterManager.class, SenderManager.class, ServerConfigManager.class));

		all.add(C(BusinessAlert.class).req(MetricConfigManager.class, ProductLineConfigManager.class).req(
		      MetricReportGroupService.class, BusinessRuleConfigManager.class, DataChecker.class, AlertManager.class,
		      BaselineService.class));

		all.add((C(BusinessAlert2.class).req(BusinessRuleConfigManager2.class, BusinessConfigManager.class,
		      BusinessTagConfigManager.class, BusinessReportGroupService.class, ProjectService.class, AlertManager.class,
		      BusinessKeyHelper.class, BaselineService.class, DataChecker.class, BaseRuleHelper.class)));

		all.add(C(NetworkAlert.class).req(ProductLineConfigManager.class).req(MetricReportGroupService.class,
		      NetworkRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(DatabaseAlert.class).req(ProductLineConfigManager.class).req(MetricReportGroupService.class,
		      DatabaseRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(HeartbeatAlert.class)
		      .req(HeartbeatDisplayPolicyManager.class)
		      .req(HeartbeatRuleConfigManager.class, DataChecker.class, ServerFilterConfigManager.class,
		            AlertManager.class, ProjectService.class)
		      .req(ModelService.class, HeartbeatAnalyzer.ID, "m_heartbeatService"));

		all.add(C(SystemAlert.class).req(ProductLineConfigManager.class).req(MetricReportGroupService.class,
		      SystemRuleConfigManager.class, DataChecker.class, AlertManager.class));

		all.add(C(AppAlert.class).req(AppDataService.class, AlertManager.class, AppRuleConfigManager.class,
		      DataChecker.class, AppConfigManager.class));

		all.add(C(AjaxAlert.class).req(AjaxDataService.class, AlertManager.class, AjaxRuleConfigManager.class,
		      DataChecker.class, UrlPatternConfigManager.class));

		all.add(C(TransactionAlert.class).req(TransactionMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, TransactionAnalyzer.ID).req(TransactionRuleConfigManager.class));

		all.add(C(EventAlert.class).req(EventMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, EventAnalyzer.ID).req(EventRuleConfigManager.class));

		all.add(C(StorageSQLAlert.class).req(StorageMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, StorageAnalyzer.ID)
		      .req(StorageSQLRuleConfigManager.class, StorageGroupConfigManager.class));

		all.add(C(StorageCacheAlert.class).req(StorageMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, StorageAnalyzer.ID)
		      .req(StorageCacheRuleConfigManager.class, StorageGroupConfigManager.class));

		all.add(C(StorageRPCAlert.class).req(StorageMergeHelper.class, DataChecker.class, AlertManager.class)
		      .req(ModelService.class, StorageAnalyzer.ID)
		      .req(StorageRPCRuleConfigManager.class, StorageGroupConfigManager.class));

		all.add(C(AlertExceptionBuilder.class).req(ExceptionRuleConfigManager.class));

		all.add(C(ExceptionAlert.class).req(ExceptionRuleConfigManager.class, AlertExceptionBuilder.class,
		      AlertManager.class).req(ModelService.class, TopAnalyzer.ID));

		all.add(C(ThirdPartyAlert.class).req(AlertManager.class));

		all.add(C(JsAlert.class).req(JsErrorLogDao.class, JsRuleConfigManager.class, AlertManager.class));

		all.add(C(HttpConnector.class));

		all.add(C(ThirdPartyAlertBuilder.class).req(HttpConnector.class, ThirdPartyAlert.class,
		      ThirdPartyConfigManager.class));

		all.add(C(AlertService.class).req(AlertDao.class));

		all.add(C(AlertInfoBuilder.class).req(AlertDao.class, TopologyGraphManager.class));

		all.add(C(AlertSummaryService.class).req(AlertSummaryDao.class));

		all.add(C(SummaryBuilder.class, RelatedSummaryBuilder.ID, RelatedSummaryBuilder.class).req(
		      AlertInfoBuilder.class, AlertSummaryService.class));

		all.add(C(SummaryBuilder.class, FailureSummaryBuilder.ID, FailureSummaryBuilder.class).req(ModelService.class,
		      ProblemAnalyzer.ID));

		all.add(C(SummaryBuilder.class, AlterationSummaryBuilder.ID, AlterationSummaryBuilder.class).req(
		      AlterationDao.class));

		all.add(C(AlertSummaryExecutor.class).req(SenderManager.class)
		      .req(SummaryBuilder.class, RelatedSummaryBuilder.ID, "m_relatedBuilder")
		      .req(SummaryBuilder.class, FailureSummaryBuilder.ID, "m_failureBuilder")
		      .req(SummaryBuilder.class, AlterationSummaryBuilder.ID, "m_alterationBuilder"));

		return all;
	}
}
