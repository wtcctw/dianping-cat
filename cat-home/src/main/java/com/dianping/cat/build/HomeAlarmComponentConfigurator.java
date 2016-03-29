package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.app.AppContactor;
import com.dianping.cat.report.alert.app.AppDecorator;
import com.dianping.cat.report.alert.browser.AjaxAlert;
import com.dianping.cat.report.alert.browser.AjaxContactor;
import com.dianping.cat.report.alert.browser.AjaxDecorator;
import com.dianping.cat.report.alert.browser.JsAlert;
import com.dianping.cat.report.alert.browser.JsContactor;
import com.dianping.cat.report.alert.browser.JsDecorator;
import com.dianping.cat.report.alert.browser.JsRuleConfigManager;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.business.BusinessContactor;
import com.dianping.cat.report.alert.business.BusinessDecorator;
import com.dianping.cat.report.alert.business2.BusinessAlert2;
import com.dianping.cat.report.alert.business2.BusinessContactor2;
import com.dianping.cat.report.alert.business2.BusinessDecorator2;
import com.dianping.cat.report.alert.business2.BusinessReportGroupService;
import com.dianping.cat.report.alert.database.DatabaseAlert;
import com.dianping.cat.report.alert.database.DatabaseContactor;
import com.dianping.cat.report.alert.database.DatabaseDecorator;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.event.EventContactor;
import com.dianping.cat.report.alert.event.EventDecorator;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.exception.ExceptionContactor;
import com.dianping.cat.report.alert.exception.ExceptionDecorator;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatContactor;
import com.dianping.cat.report.alert.heartbeat.HeartbeatDecorator;
import com.dianping.cat.report.alert.network.NetworkAlert;
import com.dianping.cat.report.alert.network.NetworkContactor;
import com.dianping.cat.report.alert.network.NetworkDecorator;
import com.dianping.cat.report.alert.spi.data.MetricReportGroupService;
import com.dianping.cat.report.alert.storage.cache.StorageCacheAlert;
import com.dianping.cat.report.alert.storage.cache.StorageCacheContactor;
import com.dianping.cat.report.alert.storage.cache.StorageCacheDecorator;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCAlert;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCContactor;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCDecorator;
import com.dianping.cat.report.alert.storage.sql.StorageSQLAlert;
import com.dianping.cat.report.alert.storage.sql.StorageSQLContactor;
import com.dianping.cat.report.alert.storage.sql.StorageSQLDecorator;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.alert.summary.AlertSummaryService;
import com.dianping.cat.report.alert.summary.build.AlertInfoBuilder;
import com.dianping.cat.report.alert.summary.build.AlterationSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.FailureSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.RelatedSummaryBuilder;
import com.dianping.cat.report.alert.system.SystemAlert;
import com.dianping.cat.report.alert.system.SystemContactor;
import com.dianping.cat.report.alert.system.SystemDecorator;
import com.dianping.cat.report.alert.thirdParty.HttpConnector;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.alert.thirdParty.ThirdpartyContactor;
import com.dianping.cat.report.alert.thirdParty.ThirdpartyDecorator;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.transaction.TransactionContactor;
import com.dianping.cat.report.alert.transaction.TransactionDecorator;
import com.dianping.cat.service.ProjectService;

public class HomeAlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(C(AlarmManager.class));

		all.add(A(MetricReportGroupService.class));
		all.add(A(BusinessReportGroupService.class));
		all.add(C(Contactor.class, BusinessContactor.ID, BusinessContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(C(Contactor.class, BusinessContactor2.ID, BusinessContactor2.class).req(AlertConfigManager.class));

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

		all.add(A(BusinessAlert.class));

		all.add(A(BusinessAlert2.class));

		all.add(A(NetworkAlert.class));

		all.add(A(DatabaseAlert.class));

		all.add(A(HeartbeatAlert.class));

		all.add(A(SystemAlert.class));

		all.add(A(AppAlert.class));

		all.add(A(AjaxAlert.class));

		all.add(A(TransactionAlert.class));

		all.add(A(EventAlert.class));

		all.add(A(StorageSQLAlert.class));

		all.add(A(StorageCacheAlert.class));

		all.add(A(StorageRPCAlert.class));

		all.add(A(AlertExceptionBuilder.class));

		all.add(A(ExceptionAlert.class));

		all.add(A(ThirdPartyAlert.class));

		all.add(A(JsAlert.class));

		all.add(C(HttpConnector.class));

		all.add(A(ThirdPartyAlertBuilder.class));

		all.add(A(AlertInfoBuilder.class));

		all.add(A(AlertSummaryService.class));

		all.add(A(RelatedSummaryBuilder.class));

		all.add(A(FailureSummaryBuilder.class));

		all.add(A(AlterationSummaryBuilder.class));

		all.add(A(AlertSummaryExecutor.class));

		return all;
	}
}
