package com.dianping.cat.report.alert;

import java.util.Map;

import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.alarm.server.ServerAlarm;
import com.dianping.cat.report.alert.app.AppAlert;
import com.dianping.cat.report.alert.browser.AjaxAlert;
import com.dianping.cat.report.alert.browser.JsAlert;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.database.DatabaseAlert;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.network.NetworkAlert;
import com.dianping.cat.report.alert.storage.cache.StorageCacheAlert;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCAlert;
import com.dianping.cat.report.alert.storage.sql.StorageSQLAlert;
import com.dianping.cat.report.alert.system.SystemAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlert;
import com.dianping.cat.report.alert.thirdParty.ThirdPartyAlertBuilder;
import com.dianping.cat.report.alert.transaction.TransactionAlert;

@Named
public class AlarmManager extends ContainerHolder {

	public void startAlarm() {
		Map<String, ServerAlarm> serverAlarms = lookupMap(ServerAlarm.class);

		for (ServerAlarm serverAlarm : serverAlarms.values()) {
			Threads.forGroup("cat").start(serverAlarm);
		}

		BusinessAlert businessAlert = lookup(BusinessAlert.class);
		NetworkAlert networkAlert = lookup(NetworkAlert.class);
		DatabaseAlert databaseAlert = lookup(DatabaseAlert.class);
		SystemAlert systemAlert = lookup(SystemAlert.class);
		ExceptionAlert exceptionAlert = lookup(ExceptionAlert.class);
		HeartbeatAlert heartbeatAlert = lookup(HeartbeatAlert.class);
		ThirdPartyAlert thirdPartyAlert = lookup(ThirdPartyAlert.class);
		ThirdPartyAlertBuilder alertBuildingTask = lookup(ThirdPartyAlertBuilder.class);
		AppAlert appAlert = lookup(AppAlert.class);
		TransactionAlert transactionAlert = lookup(TransactionAlert.class);
		EventAlert eventAlert = lookup(EventAlert.class);
		StorageSQLAlert storageDatabaseAlert = lookup(StorageSQLAlert.class);
		StorageCacheAlert storageCacheAlert = lookup(StorageCacheAlert.class);
		StorageRPCAlert storageRpcAlert = lookup(StorageRPCAlert.class);
		JsAlert jsAlert = lookup(JsAlert.class);
		AjaxAlert ajaxAlert = lookup(AjaxAlert.class);

		Threads.forGroup("cat").start(businessAlert);
		Threads.forGroup("cat").start(networkAlert);
		Threads.forGroup("cat").start(databaseAlert);
		Threads.forGroup("cat").start(systemAlert);
		Threads.forGroup("cat").start(exceptionAlert);
		Threads.forGroup("cat").start(heartbeatAlert);
		Threads.forGroup("cat").start(thirdPartyAlert);
		Threads.forGroup("cat").start(alertBuildingTask);
		Threads.forGroup("cat").start(appAlert);
		Threads.forGroup("cat").start(transactionAlert);
		Threads.forGroup("cat").start(eventAlert);
		Threads.forGroup("cat").start(storageDatabaseAlert);
		Threads.forGroup("cat").start(storageCacheAlert);
		Threads.forGroup("cat").start(storageRpcAlert);
		Threads.forGroup("cat").start(jsAlert);
		Threads.forGroup("cat").start(ajaxAlert);
	}
}
