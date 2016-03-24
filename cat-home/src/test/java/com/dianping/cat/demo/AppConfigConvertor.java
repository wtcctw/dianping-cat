package com.dianping.cat.demo;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.AppConfig;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.group.entity.AppCommandGroupConfig;
import com.dianping.cat.configuration.group.entity.SubCommand;

public class AppConfigConvertor extends ComponentTestCase {

	@Test
	public void test() {
		AppConfigManager appConfigManager = lookup(AppConfigManager.class);
		AppConfig config = appConfigManager.getConfig();
		AppCommandGroupConfig groupConfig = new AppCommandGroupConfig();

		for (Command command : config.getCommands().values()) {
			if (command.getAll()) {
				com.dianping.cat.configuration.group.entity.Command all = groupConfig.findOrCreateCommand("all");

				all.addSubCommand(new SubCommand(command.getName()));
			}
		}

		System.out.println(groupConfig);
	}
}
