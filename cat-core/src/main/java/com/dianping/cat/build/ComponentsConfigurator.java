package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.datasource.JdbcDataSourceDescriptorManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.CatCoreModule;
import com.dianping.cat.analysis.DefaultMessageAnalyzerManager;
import com.dianping.cat.analysis.DefaultMessageHandler;
import com.dianping.cat.analysis.RealtimeConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.app.AppCmdDailyTableProvider;
import com.dianping.cat.config.app.AppCommandGroupConfigManager;
import com.dianping.cat.config.app.AppCommandTableProvider;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppConnectionTableProvider;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.config.app.AppSpeedTableProvider;
import com.dianping.cat.config.app.BrokerConfigManager;
import com.dianping.cat.config.app.CrashLogConfigManager;
import com.dianping.cat.config.app.command.CommandFormatConfigManager;
import com.dianping.cat.config.app.command.DefaultCommandFormatlHandler;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.content.LocalResourceContentFetcher;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.web.AjaxDataTableProvider;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.config.web.WebSpeedDataTableProvider;
import com.dianping.cat.config.web.js.DefaultAggregationHandler;
import com.dianping.cat.config.web.url.DefaultUrlPatternHandler;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import com.dianping.cat.message.DefaultPathBuilder;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.HourlyReportTableProvider;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.IpService;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(RealtimeConsumer.class));

		all.add(A(ServerConfigManager.class));
		all.add(A(HostinfoService.class));
		all.add(A(IpService.class));
		all.add(A(TaskManager.class));
		all.add(A(ServerStatisticManager.class));
		all.add(A(DomainValidator.class));
		all.add(A(LocalResourceContentFetcher.class));
		all.add(A(ServerFilterConfigManager.class));

		all.add(A(DefaultPathBuilder.class));

		all.add(A(DefaultMessageAnalyzerManager.class));

		all.add(A(TcpSocketReceiver.class));

		all.add(A(DefaultMessageHandler.class));

		all.add(A(DefaultAggregationHandler.class));
		all.add(A(DefaultCommandFormatlHandler.class));
		all.add(A(CommandFormatConfigManager.class));
		all.add(A(SampleConfigManager.class));
		all.add(A(AppConfigManager.class));
		all.add(A(AppCommandGroupConfigManager.class));
		all.add(A(WebConfigManager.class));
		all.add(A(WebSpeedConfigManager.class));
		all.add(A(AppSpeedConfigManager.class));
		all.add(A(BusinessConfigManager.class));
		all.add(A(BrokerConfigManager.class));
		all.add(A(CrashLogConfigManager.class));

		all.add(A(DefaultUrlPatternHandler.class));
		all.add(A(UrlPatternConfigManager.class));

		all.add(A(CatCoreModule.class));

		all.add(A(AppCommandTableProvider.class));
		all.add(A(AppCmdDailyTableProvider.class));
		all.add(A(AppConnectionTableProvider.class));
		all.add(A(AppSpeedTableProvider.class));
		all.add(A(AjaxDataTableProvider.class));
		all.add(A(WebSpeedDataTableProvider.class));
		all.add(A(HourlyReportTableProvider.class));

		// database
		all.add(C(JdbcDataSourceDescriptorManager.class) //
		      .config(E("datasourceFile").value("/data/appdatas/cat/datasources.xml")));

		all.addAll(new CatCoreDatabaseConfigurator().defineComponents());
		all.addAll(new AppDatabaseConfigurator().defineComponents());
		all.addAll(new WebDatabaseConfigurator().defineComponents());

		all.addAll(new CodecComponentConfigurator().defineComponents());
		all.addAll(new StorageComponentConfigurator().defineComponents());

		return all;
	}
}
