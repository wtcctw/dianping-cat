package com.dianping.cat.report.page.app.service;

import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.CrashLog;
import com.dianping.cat.app.CrashLogContent;
import com.dianping.cat.app.CrashLogContentDao;
import com.dianping.cat.app.CrashLogContentEntity;
import com.dianping.cat.app.CrashLogDao;
import com.dianping.cat.app.CrashLogEntity;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.web.js.Level;
import com.dianping.cat.report.page.app.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.app.display.CrashLogDisplayInfo;

public class CrashLogService {

	@Inject
	private CrashLogContentDao m_crashLogContentDao;

	@Inject
	private CrashLogDao m_crashLogDao;

	@Inject
	private AppConfigManager m_appConfigManager;
	
	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	public CrashLogDetailInfo queryCrashLogDetailIno(int id) {
		CrashLogDetailInfo info = new CrashLogDetailInfo();

		try {
			CrashLog crashLog = m_crashLogDao.findByPK(id, CrashLogEntity.READSET_FULL);
			CrashLogContent detail = m_crashLogContentDao.findByPK(id, CrashLogContentEntity.READSET_FULL);

			info.setAppName(crashLog.getAppName());
			info.setPlatform(m_appConfigManager.getPlatformStr(crashLog.getPlatform()).getName());
			info.setAppVersion(crashLog.getAppVersion());
			info.setPlatformVersion(crashLog.getPlatformVersion());
			info.setModule(crashLog.getModule());
			info.setLevel(Level.getNameByCode(crashLog.getLevel()));
			info.setDeviceBrand(crashLog.getDeviceBrand());
			info.setDeviceModel(crashLog.getDeviceModel());
			info.setCrashTime(crashLog.getCrashTime());
			info.setDetail(new String(detail.getContent()));

		} catch (DalException e) {
			Cat.logError(e);
		}

		return info;
	}

	public CrashLogDisplayInfo buildCrashLogDisplayInfo(CrashLogQueryEntity entity) {
		CrashLogDisplayInfo info = new CrashLogDisplayInfo();
		info.setAppNames(m_serverFilterConfigManager.getCrashLogDomains().values());
		
		FieldsInfo fields = new FieldsInfo();
		info.setFieldsInfo(fields);
		return info;
	}
	
	public class FieldsInfo {

		private List<String> m_platVersions;

		private List<String> m_appVersions;

		private List<String> m_modules;

		private List<String> m_levels;
		
		private List<String> m_devices;
		
		public List<String> getDevices() {
			return m_devices;
		}
		
		public void setDevices(List<String> devices) {
			m_devices = devices;
		}

		public List<String> getAppVersions() {
			return m_appVersions;
		}

		public List<String> getLevels() {
			return m_levels;
		}

		public List<String> getModules() {
			return m_modules;
		}

		public List<String> getPlatVersions() {
			return m_platVersions;
		}

		public FieldsInfo setAppVersions(List<String> appVersions) {
			m_appVersions = appVersions;
			return this;
		}

		public FieldsInfo setLevels(List<String> levels) {
			m_levels = levels;
			return this;
		}

		public FieldsInfo setModules(List<String> modules) {
			m_modules = modules;
			return this;
		}

		public FieldsInfo setPlatVersions(List<String> platVersions) {
			m_platVersions = platVersions;
			return this;
		}
	}
}
