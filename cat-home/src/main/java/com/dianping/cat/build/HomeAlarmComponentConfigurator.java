package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.report.alert.AlarmManager;
import com.dianping.cat.report.alert.browser.AjaxAlert;
import com.dianping.cat.report.alert.browser.AjaxContactor;
import com.dianping.cat.report.alert.browser.AjaxDecorator;
import com.dianping.cat.report.alert.browser.JsAlert;
import com.dianping.cat.report.alert.browser.JsContactor;
import com.dianping.cat.report.alert.browser.JsDecorator;
import com.dianping.cat.report.alert.browser.JsRuleConfigManager;
import com.dianping.cat.report.alert.config.BaseRuleHelper;
import com.dianping.cat.report.alert.database.DatabaseAlert;
import com.dianping.cat.report.alert.database.DatabaseContactor;
import com.dianping.cat.report.alert.database.DatabaseDecorator;
import com.dianping.cat.report.alert.network.NetworkAlert;
import com.dianping.cat.report.alert.network.NetworkContactor;
import com.dianping.cat.report.alert.network.NetworkDecorator;
import com.dianping.cat.report.alert.network.NetworkRuleConfigManager;
import com.dianping.cat.report.alert.spi.config.UserDefinedRuleManager;
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
import com.dianping.cat.service.ProjectService;

public class HomeAlarmComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {

		List<Component> all = new ArrayList<Component>();

		all.add(A(AlarmManager.class));
		all.add(A(AlertConfigManager.class));
		all.add(A(BaseRuleHelper.class));
		all.add(A(UserDefinedRuleManager.class));

		// network
		all.add(A(NetworkRuleConfigManager.class));
		all.add(C(Decorator.class, NetworkDecorator.ID, NetworkDecorator.class));
		all.add(C(Contactor.class, NetworkContactor.ID, NetworkContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(A(NetworkAlert.class));

		// database
		all.add(C(Decorator.class, DatabaseDecorator.ID, DatabaseDecorator.class));
		all.add(C(Contactor.class, DatabaseContactor.ID, DatabaseContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(A(DatabaseAlert.class));

		// system
		all.add(A(SystemRuleConfigManager.class));
		all.add(C(Decorator.class, SystemDecorator.ID, SystemDecorator.class));
		all.add(C(Contactor.class, SystemContactor.ID, SystemContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(A(SystemAlert.class));

		// third-party
		all.add(C(Decorator.class, ThirdpartyDecorator.ID, ThirdpartyDecorator.class).req(ProjectService.class));
		all.add(C(Contactor.class, ThirdpartyContactor.ID, ThirdpartyContactor.class).req(ProjectService.class,
		      AlertConfigManager.class));
		all.add(A(ThirdPartyAlertBuilder.class));
		all.add(A(HttpConnector.class));
		all.add(A(ThirdPartyAlert.class));
		all.add(A(ThirdPartyConfigManager.class));

		// js
		all.add(A(JsRuleConfigManager.class));
		all.add(C(Decorator.class, JsDecorator.ID, JsDecorator.class));
		all.add(C(Contactor.class, JsContactor.ID, JsContactor.class).req(JsRuleConfigManager.class,
		      AlertConfigManager.class));
		all.add(A(JsAlert.class));

		// ajax
		all.add(C(Decorator.class, AjaxDecorator.ID, AjaxDecorator.class));
		all.add(C(Contactor.class, AjaxContactor.ID, AjaxContactor.class).req(AlertConfigManager.class,
		      ProjectService.class, UrlPatternConfigManager.class));
		all.add(A(AjaxAlert.class));

		return all;
	}
}
