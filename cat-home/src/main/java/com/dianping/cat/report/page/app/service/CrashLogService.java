package com.dianping.cat.report.page.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.app.CrashLog;
import com.dianping.cat.app.CrashLogContent;
import com.dianping.cat.app.CrashLogContentDao;
import com.dianping.cat.app.CrashLogContentEntity;
import com.dianping.cat.app.CrashLogDao;
import com.dianping.cat.app.CrashLogEntity;
import com.dianping.cat.config.Level;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.report.ErrorMsg;
import com.dianping.cat.report.page.app.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.app.display.CrashLogDisplayInfo;

public class CrashLogService {

	private final int LIMIT = 10000;

	@Inject
	private CrashLogContentDao m_crashLogContentDao;

	@Inject
	private CrashLogDao m_crashLogDao;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	private String APP_VERSIONS = "appVersions";

	private String LEVELS = "levels";

	private String MODULES = "modules";

	private String PLATFORM_VERSIONS = "platformVersions";

	private String DEVICES = "devices";

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

		buildCrashLogData(entity, info);
		info.setAppNames(m_serverFilterConfigManager.getCrashLogDomains().values());

		return info;
	}

	private void buildCrashLogData(CrashLogQueryEntity entity, CrashLogDisplayInfo info) {
		Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();
		CrashLogFilter crashLogFilter = new CrashLogFilter(entity.getQuery());

		Date startTime = entity.buildStartTime();
		Date endTime = entity.buildEndTime();
		String appName = entity.getAppName();
		int platform = entity.getPlatform();
		String dpid = entity.getDpid();
		Map<String, ErrorMsg> errorMsgs = new HashMap<String, ErrorMsg>();
		int offset = 0;
		int totalCount = 0;

		try {
			while (true) {
				List<CrashLog> result = m_crashLogDao.findDataByConditions(startTime, endTime, appName, platform, dpid,
				      offset, LIMIT, CrashLogEntity.READSET_FULL);

				for (CrashLog log : result) {
					buildFieldsMap(fieldsMap, log);

					if (crashLogFilter.checkFlag(log)) {
						buildErrorMsg(errorMsgs, log);
						totalCount ++;
					}
				}

				int count = result.size();
				offset += count;

				if (count < LIMIT) {
					break;
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		info.setTotalCount(totalCount);
		info.setErrors(buildErrors(errorMsgs));

		if (!fieldsMap.isEmpty()) {
			info.setFieldsInfo(buildFiledsInfo(fieldsMap));
		}

	}

	private void buildFieldsMap(Map<String, Set<String>> fieldsMap, CrashLog log) {
		findOrCreate(APP_VERSIONS, fieldsMap).add(log.getAppVersion());
		findOrCreate(PLATFORM_VERSIONS, fieldsMap).add(log.getPlatformVersion());
		findOrCreate(MODULES, fieldsMap).add(log.getModule());
		findOrCreate(LEVELS, fieldsMap).add(Level.getNameByCode(log.getLevel()));
		findOrCreate(DEVICES, fieldsMap).add(log.getDeviceBrand() + "-" + log.getDeviceModel());
	}

	private void buildErrorMsg(Map<String, ErrorMsg> errorMsgs, CrashLog log) {
		String msg = log.getMsg();
		ErrorMsg errorMsg = errorMsgs.get(msg);

		if (errorMsg == null) {
			errorMsg = new ErrorMsg();
			errorMsg.setMsg(msg);
			errorMsgs.put(msg, errorMsg);
		}

		errorMsg.addCount();
		errorMsg.addId(log.getId());
	}

	private FieldsInfo buildFiledsInfo(Map<String, Set<String>> fieldsMap) {
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s2.compareTo(s1);
			}
		};

		FieldsInfo fieldsInfo = new FieldsInfo();
		List<String> v = new ArrayList<String>(fieldsMap.get(APP_VERSIONS));
		List<String> p = new ArrayList<String>(fieldsMap.get(PLATFORM_VERSIONS));
		List<String> l = new ArrayList<String>(fieldsMap.get(LEVELS));
		List<String> m = new ArrayList<String>(fieldsMap.get(MODULES));
		List<String> d = new ArrayList<String>(fieldsMap.get(DEVICES));

		Collections.sort(v, comparator);
		Collections.sort(p, comparator);

		fieldsInfo.setAppVersions(v).setPlatVersions(p).setModules(m).setLevels(l).setDevices(d);

		return fieldsInfo;
	}

	private Set<String> findOrCreate(String key, Map<String, Set<String>> map) {
		Set<String> value = map.get(key);

		if (value == null) {
			value = new HashSet<String>();
			map.put(key, value);
		}
		return value;
	}

	private List<ErrorMsg> buildErrors(Map<String, ErrorMsg> errorMsgs) {
		List<ErrorMsg> errorMsgList = new ArrayList<ErrorMsg>();
		Iterator<Entry<String, ErrorMsg>> iter = errorMsgs.entrySet().iterator();

		while (iter.hasNext()) {
			errorMsgList.add(iter.next().getValue());
		}

		Collections.sort(errorMsgList);
		return errorMsgList;
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
