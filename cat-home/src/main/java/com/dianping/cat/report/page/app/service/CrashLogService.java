package com.dianping.cat.report.page.app.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.CrashLog;
import com.dianping.cat.app.CrashLogContent;
import com.dianping.cat.app.CrashLogContentDao;
import com.dianping.cat.app.CrashLogContentEntity;
import com.dianping.cat.app.CrashLogDao;
import com.dianping.cat.app.CrashLogEntity;
import com.dianping.cat.config.Level;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.CrashLogConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.config.app.MobileConstants;
import com.dianping.cat.helper.Status;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.ErrorMsg;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.page.app.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.app.display.CrashLogDisplayInfo;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class CrashLogService {

	private static final String MAPPER = "mapper";

	private final int LIMIT = 10000;

	private final int BUFFER = 1024;

	@Inject
	private CrashLogContentDao m_crashLogContentDao;

	@Inject
	private CrashLogDao m_crashLogDao;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	@Inject
	private CrashLogConfigManager m_crashLogConfig;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	private String APP_VERSIONS = "appVersions";

	private String LEVELS = "levels";

	private String MODULES = "modules";

	private String PLATFORM_VERSIONS = "platformVersions";

	private String DEVICES = "devices";

	private void addCount(String item, Map<String, AtomicInteger> distributions) {
		AtomicInteger count = distributions.get(item);

		if (count == null) {
			count = new AtomicInteger(1);
			distributions.put(item, count);
		} else {
			count.incrementAndGet();
		}
	}

	private String buildContent(byte[] content) {
		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPInputStream gis = null;

		try {
			gis = new GZIPInputStream(bais);
		} catch (IOException ex) {
			try {
				baos.close();
				bais.close();
			} catch (IOException e) {
				Cat.logError(e);
			}
			return m_configHtmlParser.parse(new String(content)).replace("\n", "<br/>");
		}

		try {
			int count;
			byte data[] = new byte[BUFFER];

			while ((count = gis.read(data, 0, BUFFER)) != -1) {
				baos.write(data, 0, count);
			}

			byte[] result = baos.toByteArray();

			baos.flush();
			return m_configHtmlParser.parse(new String(result)).replace("\n", "<br/>");
		} catch (IOException e) {
			Cat.logError(e);
			return m_configHtmlParser.parse(new String(content)).replace("\n", "<br/>");
		} finally {
			try {
				gis.close();
				baos.close();
				bais.close();
			} catch (IOException e) {
				Cat.logError(e);
			}
		}
	}

	public CrashLogDisplayInfo buildCrashGraph(CrashLogQueryEntity entity) {
		CrashLogDisplayInfo info = new CrashLogDisplayInfo();
		buildCrashGraph(entity, info);

		return info;
	}

	private void buildCrashGraph(CrashLogQueryEntity entity, CrashLogDisplayInfo info) {
		CrashLogFilter crashLogFilter = new CrashLogFilter(entity.getQuery());
		Map<String, Map<String, AtomicInteger>> distributions = new HashMap<String, Map<String, AtomicInteger>>();

		Date startTime = entity.buildStartTime();
		Date endTime = entity.buildEndTime();
		String appName = entity.getAppName();
		int platform = entity.getPlatform();
		String dpid = entity.getDpid();
		int offset = 0;

		try {
			while (true) {
				List<CrashLog> result = m_crashLogDao.findDataByConditions(startTime, endTime, appName, platform, dpid,
				      offset, LIMIT, CrashLogEntity.READSET_FULL);

				for (CrashLog log : result) {
					if (crashLogFilter.checkFlag(log) && log.getMsg().equals(entity.getMsg())) {
						buildDistributions(log, distributions);
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

		info.setMsgDistributions(buildDistributionChart(distributions));

	}

	private void buildCrashLogData(CrashLogQueryEntity entity, CrashLogDisplayInfo info) {
		Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();
		CrashLogFilter crashLogFilter = new CrashLogFilter(entity.getQuery());
		Map<String, Map<String, AtomicInteger>> distributions = new HashMap<String, Map<String, AtomicInteger>>();

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
						buildDistributions(log, distributions);
						totalCount++;
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
		info.setDistributions(buildDistributionChart(distributions));

		if (!fieldsMap.isEmpty()) {
			info.setFieldsInfo(buildFiledsInfo(fieldsMap));
		}
	}

	public CrashLogDisplayInfo buildCrashLogDisplayInfo(CrashLogQueryEntity entity) {
		CrashLogDisplayInfo info = new CrashLogDisplayInfo();

		buildCrashLogData(entity, info);
		info.setAppNames(m_mobileConfigManager.queryConstantItem(MobileConstants.APP_NAME).values());

		return info;
	}

	public CrashLogDisplayInfo buildCrashTrend(CrashLogQueryEntity entity1, CrashLogQueryEntity entity2) {
		CrashLogDisplayInfo info = new CrashLogDisplayInfo();
		Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();
		Double[] current = getCrashTrendData(entity1, fieldsMap);
		Double[] comparison = null;

		if (entity2 != null) {
			comparison = getCrashTrendData(entity2, fieldsMap);
		}
		LineChart lineChart = buildLineChart(current, comparison);

		info.setLineChart(lineChart);

		if (!fieldsMap.isEmpty()) {
			info.setFieldsInfo(buildFiledsInfo(fieldsMap));
		}
		info.setAppNames(m_mobileConfigManager.queryConstantItem(MobileConstants.APP_NAME).values());
		return info;
	}

	public Map<String, PieChart> buildDistributionChart(Map<String, Map<String, AtomicInteger>> distributions) {
		Map<String, PieChart> charts = new HashMap<String, PieChart>();

		for (Entry<String, Map<String, AtomicInteger>> entrys : distributions.entrySet()) {
			Map<String, AtomicInteger> distribution = entrys.getValue();
			PieChart chart = new PieChart();
			List<Item> items = new ArrayList<Item>();

			for (Entry<String, AtomicInteger> entry : distribution.entrySet()) {
				Item item = new Item();

				item.setNumber(entry.getValue().get()).setTitle(entry.getKey());
				items.add(item);
			}
			chart.addItems(items);
			chart.setTitle(entrys.getKey());
			charts.put(entrys.getKey(), chart);
		}

		return charts;
	}

	private void buildDistributions(CrashLog log, Map<String, Map<String, AtomicInteger>> distributions) {
		if (distributions.isEmpty()) {
			Map<String, AtomicInteger> appVersions = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> platVersions = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> modules = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> devices = new HashMap<String, AtomicInteger>();

			distributions.put(APP_VERSIONS, appVersions);
			distributions.put(PLATFORM_VERSIONS, platVersions);
			distributions.put(MODULES, modules);
			distributions.put(DEVICES, devices);
		}

		addCount(log.getAppVersion(), distributions.get(APP_VERSIONS));
		addCount(log.getPlatformVersion(), distributions.get(PLATFORM_VERSIONS));
		addCount(log.getModule(), distributions.get(MODULES));
		addCount(log.getDeviceBrand() + "-" + log.getDeviceModel(), distributions.get(DEVICES));
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

	private List<ErrorMsg> buildErrors(Map<String, ErrorMsg> errorMsgs) {
		List<ErrorMsg> errorMsgList = new ArrayList<ErrorMsg>();
		Iterator<Entry<String, ErrorMsg>> iter = errorMsgs.entrySet().iterator();

		while (iter.hasNext()) {
			errorMsgList.add(iter.next().getValue());
		}

		Collections.sort(errorMsgList);
		return errorMsgList;
	}

	private void buildFieldsMap(Map<String, Set<String>> fieldsMap, CrashLog log) {
		findOrCreate(APP_VERSIONS, fieldsMap).add(log.getAppVersion());
		findOrCreate(PLATFORM_VERSIONS, fieldsMap).add(log.getPlatformVersion());
		findOrCreate(MODULES, fieldsMap).add(log.getModule());
		findOrCreate(LEVELS, fieldsMap).add(Level.getNameByCode(log.getLevel()));
		findOrCreate(DEVICES, fieldsMap).add(log.getDeviceBrand() + "-" + log.getDeviceModel());
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

	private LineChart buildLineChart(Double[] current, Double[] comparison) {
		LineChart lineChart = new LineChart();
		lineChart.setId(Constants.APP);
		lineChart.setHtmlTitle("Crash数 (个/5分钟)");
		lineChart.add(Constants.CURRENT_STR, current);
		lineChart.add(Constants.COMPARISION_STR, comparison);
		return lineChart;
	}

	private boolean check(String condition, String value) {
		if (StringUtils.isBlank(condition) || condition.equals(value)) {
			return true;
		} else {
			return false;
		}
	}

	private Set<String> findOrCreate(String key, Map<String, Set<String>> map) {
		Set<String> value = map.get(key);

		if (value == null) {
			value = new HashSet<String>();
			map.put(key, value);
		}
		return value;
	}

	private Double[] getCrashTrendData(CrashLogQueryEntity entity, Map<String, Set<String>> fieldsMap) {
		Date startTime = entity.buildTrendStartTime();
		Date endTime = entity.buildEndTime();
		String appName = entity.getAppName();
		String appVersion = entity.getAppVersion();
		String platVersion = entity.getPlatformVersion();
		String module = entity.getModule();
		long day = entity.buildDay().getTime();
		long step = TimeHelper.ONE_MINUTE * 5;
		int duration = (int) ((endTime.getTime() - day) / step);
		Double[] data = new Double[duration];
		int offset = 0;

		try {
			while (true) {
				List<CrashLog> result = m_crashLogDao.findDataByConditions(startTime, endTime, appName, -1, null, offset,
				      LIMIT, CrashLogEntity.READSET_FULL);

				for (CrashLog log : result) {
					if (check(appVersion, log.getAppVersion()) && check(platVersion, log.getPlatformVersion())
					      && check(module, log.getModule())) {
						Date date = log.getCrashTime();
						int index = (int) ((date.getTime() - day) / step);
						Double minuteData = data[index];

						if (minuteData == null) {
							data[index] = new Double(0);
						}
						data[index]++;
					}
					buildFieldsMap(fieldsMap, log);
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
		return data;
	}

	public CrashLogDetailInfo queryCrashLogDetailInfo(int id) {
		CrashLogDetailInfo info = new CrashLogDetailInfo();

		try {
			CrashLog crashLog = m_crashLogDao.findByPK(id, CrashLogEntity.READSET_FULL);
			int tag = crashLog.getTag();

			if (tag == Status.NOT_MAPPED.getStatus() || tag == Status.FAILED.getStatus()) {
				try {
					String url = m_crashLogConfig.findServerUrl(MAPPER) + "&id=" + id;
					InputStream in = Urls.forIO().readTimeout(5000).connectTimeout(1000).openStream(url);
					Files.forIO().readFrom(in, "utf-8");
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			info.setAppName(crashLog.getAppName());
			info.setPlatform(m_mobileConfigManager.getPlatformStr(crashLog.getPlatform()).getValue());
			info.setAppVersion(crashLog.getAppVersion());
			info.setPlatformVersion(crashLog.getPlatformVersion());
			info.setModule(crashLog.getModule());
			info.setLevel(Level.getNameByCode(crashLog.getLevel()));
			info.setDeviceBrand(crashLog.getDeviceBrand());
			info.setDeviceModel(crashLog.getDeviceModel());
			info.setCrashTime(crashLog.getCrashTime());
			info.setDpid(crashLog.getDpid());

			CrashLogContent detail = m_crashLogContentDao.findByPK(id, CrashLogContentEntity.READSET_FULL);

			info.setDetail(buildContent(detail.getContent()));
		} catch (Exception e) {
			Cat.logError(e);
		}

		return info;
	}

}
