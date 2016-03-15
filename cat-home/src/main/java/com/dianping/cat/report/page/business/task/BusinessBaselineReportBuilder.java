package com.dianping.cat.report.page.business.task;

import java.util.Date;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.report.task.TaskBuilder;

public class BusinessBaselineReportBuilder implements TaskBuilder, Initializable {

	@Override
   public void initialize() throws InitializationException {
	   // TODO Auto-generated method stub
	   
   }

	@Override
   public boolean buildDailyTask(String name, String domain, Date period) {
	   // TODO Auto-generated method stub
	   return false;
   }

	@Override
   public boolean buildHourlyTask(String name, String domain, Date period) {
	   // TODO Auto-generated method stub
	   return false;
   }

	@Override
   public boolean buildMonthlyTask(String name, String domain, Date period) {
	   // TODO Auto-generated method stub
	   return false;
   }

	@Override
   public boolean buildWeeklyTask(String name, String domain, Date period) {
	   // TODO Auto-generated method stub
	   return false;
   }

}
