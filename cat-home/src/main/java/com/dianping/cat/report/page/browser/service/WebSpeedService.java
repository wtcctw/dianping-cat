package com.dianping.cat.report.page.browser.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.configuration.web.entity.Item;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.browser.display.WebSpeedDetail;
import com.dianping.cat.report.page.browser.display.WebSpeedDisplayInfo;
import com.dianping.cat.web.WebSpeedData;

public class WebSpeedService extends ContainerHolder {

	@Inject
	private WebSpeedDataManager m_dataManager;

	@Inject
	private WebConfigManager m_configManager;

	private final static String CURRENT = "当前值";

	private final static String COMPARISION = "对比值";

	private WebSpeedDetail build5MinuteData(int minute, List<WebSpeedData> datas, Date period) {
		long accessSum = 0;
		double responseSum = 0, responseAvg = 0;

		for (WebSpeedData data : datas) {
			accessSum += data.getAccessNumberSum();
			responseSum += data.getResponseSumTimeSum();
		}

		if (accessSum > 0) {
			responseAvg = responseSum / accessSum;
		}

		WebSpeedDetail d = new WebSpeedDetail();

		d.setPeriod(period);
		d.setMinuteOrder(minute);
		d.setAccessNumberSum(accessSum);
		d.setResponseTimeAvg(responseAvg);
		return d;
	}

	public LineChart buildLineChart(final Map<String, WebSpeedSequence> datas) {
		LineChart lineChart = new LineChart();
		lineChart.setId("web");
		lineChart.setUnit("");
		lineChart.setHtmlTitle("延时平均值（毫秒/5分钟）");

		for (Entry<String, WebSpeedSequence> entry : datas.entrySet()) {
			Double[] data = computeDelayAvg(entry.getValue());

			lineChart.add(entry.getKey(), data);
		}
		return lineChart;
	}

	private Map<String, WebSpeedDetail> buildOneDayData(Map<String, WebSpeedSequence> datas) {
		Map<String, WebSpeedDetail> summarys = new LinkedHashMap<String, WebSpeedDetail>();

		for (Entry<String, WebSpeedSequence> entry : datas.entrySet()) {
			try {
				Map<Integer, List<WebSpeedData>> appSpeedData = entry.getValue().getRecords();
				Date period = entry.getValue().getPeriod();

				if (!appSpeedData.isEmpty()) {
					long accessSum = 0;
					double responseSum = 0, responseAvg = 0;

					for (Entry<Integer, List<WebSpeedData>> e : appSpeedData.entrySet()) {
						for (WebSpeedData data : e.getValue()) {
							accessSum += data.getAccessNumberSum();
							responseSum += data.getResponseSumTimeSum();
						}
					}
					if (accessSum > 0) {
						responseAvg = responseSum / accessSum;
					}
					WebSpeedDetail d = new WebSpeedDetail();

					d.setPeriod(period);
					d.setAccessNumberSum(accessSum);
					d.setResponseTimeAvg(responseAvg);
					summarys.put(entry.getKey(), d);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return summarys;
	}

	private Map<String, List<WebSpeedDetail>> buildSpeedDetail(Map<String, WebSpeedSequence> datas) {
		Map<String, List<WebSpeedDetail>> details = new LinkedHashMap<String, List<WebSpeedDetail>>();

		for (Entry<String, WebSpeedSequence> entry : datas.entrySet()) {
			Map<Integer, List<WebSpeedData>> appSpeedDataMap = entry.getValue().getRecords();
			Date period = entry.getValue().getPeriod();
			List<WebSpeedDetail> detail = new ArrayList<WebSpeedDetail>();

			for (Entry<Integer, List<WebSpeedData>> e : appSpeedDataMap.entrySet()) {
				int minute = e.getKey();
				List<WebSpeedData> data = e.getValue();

				if (!data.isEmpty()) {
					detail.add(build5MinuteData(minute, data, period));
				}
			}
			details.put(entry.getKey(), detail);
		}

		return details;
	}

	public WebSpeedDisplayInfo buildSpeedDisplayInfo(SpeedQueryEntity queryEntity1, SpeedQueryEntity queryEntity2) {
		Map<String, WebSpeedSequence> datas = queryRawData(queryEntity1, queryEntity2);
		WebSpeedDisplayInfo appSpeedDisplayInfo = buildWebSpeedDisplayInfo(datas);

		return appSpeedDisplayInfo;
	}

	public WebSpeedDisplayInfo buildBarCharts(SpeedQueryEntity queryEntity) {
		WebSpeedDisplayInfo info = new WebSpeedDisplayInfo();

		info.setCityChart(buildCityChart(queryEntity));
		info.setOperatorChart(buildOperatorChart(queryEntity));
		info.setSourceChart(buildSourceChart(queryEntity));
		info.setPlatformChart(buildPlatformChart(queryEntity));
		info.setNetworkChart(buildNetworkChart(queryEntity));
		
		return info;
	}

	public BarChart buildCityChart(SpeedQueryEntity entity) {
		BarChart barChart = new BarChart();
		barChart.setTitle("请求平均时间(地区)");
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName("省份列表");

		List<WebSpeedData> datas = m_dataManager.queryValueByCity(entity);
		Map<String, Double> values = new HashMap<String, Double>();

		for (WebSpeedData data : datas) {
			Item item = m_configManager.queryItem(WebConfigManager.CITY, data.getCity());
			double avg = 0.0;

			if (data.getAccessNumberSum() > 0) {
				avg = data.getResponseSumTimeSum() / data.getAccessNumberSum();
			}
			values.put(item.getName(), avg);
		}
		barChart.addValues(values);

		return barChart;
	}

	public BarChart buildPlatformChart(SpeedQueryEntity entity) {
		BarChart barChart = new BarChart();
		barChart.setTitle("请求平均时间(平台)");
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName("平台列表");

		List<WebSpeedData> datas = m_dataManager.queryValueByPlatform(entity);
		Map<String, Double> values = new HashMap<String, Double>();

		for (WebSpeedData data : datas) {
			Item item = m_configManager.queryItem(WebConfigManager.PLATFORM, data.getPlatform());
			double avg = 0.0;

			if (data.getAccessNumberSum() > 0) {
				avg = data.getResponseSumTimeSum() / data.getAccessNumberSum();
			}
			values.put(item.getName(), avg);
		}
		barChart.addValues(values);

		return barChart;
	}

	public BarChart buildSourceChart(SpeedQueryEntity entity) {
		BarChart barChart = new BarChart();
		barChart.setTitle("请求平均时间(来源)");
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName("来源列表");

		List<WebSpeedData> datas = m_dataManager.queryValueBySource(entity);
		Map<String, Double> values = new HashMap<String, Double>();

		for (WebSpeedData data : datas) {
			Item item = m_configManager.queryItem(WebConfigManager.SOURCE, data.getSource());
			double avg = 0.0;

			if (data.getAccessNumberSum() > 0) {
				avg = data.getResponseSumTimeSum() / data.getAccessNumberSum();
			}
			values.put(item.getName(), avg);
		}
		barChart.addValues(values);

		return barChart;
	}

	public BarChart buildOperatorChart(SpeedQueryEntity entity) {
		BarChart barChart = new BarChart();
		barChart.setTitle("请求平均时间(运营商)");
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName("运营商列表");

		List<WebSpeedData> datas = m_dataManager.queryValueByOperator(entity);
		Map<String, Double> values = new HashMap<String, Double>();

		for (WebSpeedData data : datas) {
			Item item = m_configManager.queryItem(WebConfigManager.OPERATOR, data.getOperator());
			double avg = 0.0;

			if (data.getAccessNumberSum() > 0) {
				avg = data.getResponseSumTimeSum() / data.getAccessNumberSum();
			}
			values.put(item.getName(), avg);
		}
		barChart.addValues(values);

		return barChart;
	}
	
	public BarChart buildNetworkChart(SpeedQueryEntity entity) {
		BarChart barChart = new BarChart();
		barChart.setTitle("请求平均时间(网络类型)");
		barChart.setyAxis("加载时间(ms)");
		barChart.setSerieName("网络类型列表");

		List<WebSpeedData> datas = m_dataManager.queryValueByNetwork(entity);
		Map<String, Double> values = new HashMap<String, Double>();

		for (WebSpeedData data : datas) {
			Item item = m_configManager.queryItem(WebConfigManager.NETWORK, data.getNetwork());
			double avg = 0.0;

			if (data.getAccessNumberSum() > 0) {
				avg = data.getResponseSumTimeSum() / data.getAccessNumberSum();
			}
			values.put(item.getName(), avg);
		}
		barChart.addValues(values);

		return barChart;
	}

	private WebSpeedSequence buildWebSequence(List<WebSpeedData> fromDatas, Date period) {
		Map<Integer, List<WebSpeedData>> dataMap = new LinkedHashMap<Integer, List<WebSpeedData>>();
		int max = -5;

		for (WebSpeedData from : fromDatas) {
			int minute = from.getMinuteOrder();

			if (max < 0 || max < minute) {
				max = minute;
			}
			List<WebSpeedData> datas = dataMap.get(minute);

			if (datas == null) {
				datas = new LinkedList<WebSpeedData>();

				dataMap.put(minute, datas);
			}
			datas.add(from);
		}
		int n = max / 5 + 1;
		int length = queryWebDataDuration(period, n);

		return new WebSpeedSequence(period, length, dataMap);
	}

	private WebSpeedDisplayInfo buildWebSpeedDisplayInfo(Map<String, WebSpeedSequence> datas) {
		WebSpeedDisplayInfo info = new WebSpeedDisplayInfo();

		info.setLineChart(buildLineChart(datas));
		info.setWebSpeedDetails(buildSpeedDetail(datas));
		info.setWebSpeedSummarys(buildOneDayData(datas));

		return info;
	}

	public Double[] computeDelayAvg(WebSpeedSequence convertedData) {
		int n = convertedData.getDuration();
		Double[] value = new Double[n];

		for (Entry<Integer, List<WebSpeedData>> entry : convertedData.getRecords().entrySet()) {
			for (WebSpeedData data : entry.getValue()) {
				long count = data.getAccessNumberSum();
				long sum = data.getResponseSumTimeSum();
				double avg = 0;

				if (count > 0) {
					avg = sum / count;
				}

				int index = data.getMinuteOrder() / 5;

				if (index < n) {
					value[index] = avg;
				}
			}
		}
		return value;
	}

	private WebSpeedSequence queryData(SpeedQueryEntity queryEntity) {
		List<WebSpeedData> datas = m_dataManager.queryValueByTime(queryEntity);
		WebSpeedSequence sequence = buildWebSequence(datas, queryEntity.getDate());

		return sequence;
	}

	private Map<String, WebSpeedSequence> queryRawData(SpeedQueryEntity queryEntity1, SpeedQueryEntity queryEntity2) {
		Map<String, WebSpeedSequence> datas = new LinkedHashMap<String, WebSpeedSequence>();

		if (queryEntity1 != null) {
			WebSpeedSequence data1 = queryData(queryEntity1);

			if (data1.getDuration() > 0) {
				datas.put(CURRENT, data1);
			}
		}

		if (queryEntity2 != null) {
			WebSpeedSequence data2 = queryData(queryEntity2);

			if (data2.getDuration() > 0) {
				datas.put(COMPARISION, data2);
			}
		}
		return datas;
	}

	private int queryWebDataDuration(Date period, int defaultValue) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if (period.equals(cal.getTime())) {
			long start = cal.getTimeInMillis();
			long current = System.currentTimeMillis();
			int length = (int) (current - current % 300000 - start) / 300000 - 1;

			return length < 0 ? 0 : length;
		}
		return defaultValue;
	}

	public class WebSpeedSequence {
		private Date m_period;

		protected int m_duration;

		protected Map<Integer, List<WebSpeedData>> m_records;

		public WebSpeedSequence(Date period, int duration, Map<Integer, List<WebSpeedData>> records) {
			m_period = period;
			m_duration = duration;
			m_records = records;
		}

		public int getDuration() {
			return m_duration;
		}

		public Date getPeriod() {
			return m_period;
		}

		public Map<Integer, List<WebSpeedData>> getRecords() {
			return m_records;
		}
	}

}
