package com.dianping.cat;

import org.unidal.helper.Threads;
import org.unidal.initialization.AbstractModule;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;

import com.dianping.cat.task.ConfigSyncTask;

public class CatCoreModule extends AbstractModule {
	public static final String ID = "cat-core";

	@Override
	protected void execute(final ModuleContext ctx) throws Exception {
		Threads.forGroup("cat").start(ConfigSyncTask.getInstance());
	}

	@Override
	public Module[] getDependencies(ModuleContext ctx) {
		return ctx.getModules(CatClientModule.ID);
	}
}
