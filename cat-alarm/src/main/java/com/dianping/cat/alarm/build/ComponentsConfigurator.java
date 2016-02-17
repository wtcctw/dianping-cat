package com.dianping.cat.alarm.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.alarm.AlertDao;
import com.dianping.cat.alarm.service.AlertService;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.config.AlertPolicyManager;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;
import com.dianping.cat.alarm.spi.decorator.DecoratorManager;
import com.dianping.cat.alarm.spi.receiver.ContactorManager;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.alarm.spi.rule.DefaultDataChecker;
import com.dianping.cat.alarm.spi.sender.MailSender;
import com.dianping.cat.alarm.spi.sender.Sender;
import com.dianping.cat.alarm.spi.sender.SenderManager;
import com.dianping.cat.alarm.spi.sender.SmsSender;
import com.dianping.cat.alarm.spi.sender.WeixinSender;
import com.dianping.cat.alarm.spi.spliter.MailSpliter;
import com.dianping.cat.alarm.spi.spliter.SmsSpliter;
import com.dianping.cat.alarm.spi.spliter.Spliter;
import com.dianping.cat.alarm.spi.spliter.SpliterManager;
import com.dianping.cat.alarm.spi.spliter.WeixinSpliter;
import com.dianping.cat.build.CatDatabaseConfigurator;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.config.ConfigDao;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(C(AlertService.class).req(AlertDao.class));
		all.add(C(AlertConfigManager.class).req(ConfigDao.class, ContentFetcher.class));
		all.add(C(SenderConfigManager.class).req(ConfigDao.class, ContentFetcher.class));

		all.add(C(DataChecker.class, DefaultDataChecker.class));
		all.add(C(DecoratorManager.class));
		all.add(C(ContactorManager.class));
		
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

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		all.addAll(new CatDatabaseConfigurator().defineComponents());

		return all;
	}
}
