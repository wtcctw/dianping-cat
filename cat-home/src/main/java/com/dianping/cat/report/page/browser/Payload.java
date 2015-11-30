package com.dianping.cat.report.page.browser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.js.Level;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.browser.service.AjaxDataField;
import com.dianping.cat.report.page.browser.service.AjaxDataQueryEntity;
import com.dianping.cat.report.page.browser.service.AjaxDataService;
import com.dianping.cat.report.page.browser.service.SpeedQueryEntity;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.tuple.Pair;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("url")
	private String m_url;

	@FieldMeta("type")
	private String m_type = AjaxDataService.REQUEST;

	@FieldMeta("sort")
	private String m_sort = AjaxDataService.SUCCESS;

	@FieldMeta("query1")
	private String m_query1;

	@FieldMeta("query2")
	private String m_query2;

	@FieldMeta("api1")
	private String m_api1;

	@FieldMeta("api2")
	private String m_api2;

	@FieldMeta("groupByField")
	private AjaxDataField m_groupByField = AjaxDataField.CODE;

	@FieldMeta("day")
	private String m_day;

	@FieldMeta("start")
	private String m_startTime;

	@FieldMeta("end")
	private String m_endTime;

	@FieldMeta("level")
	private String m_level;

	@FieldMeta("module")
	private String m_module;

	@FieldMeta("msg")
	private String m_msg;
	
	@FieldMeta("dpid")
	private String m_dpid;

	@FieldMeta("id")
	private int m_id;

	private SimpleDateFormat m_format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private static final String ALL = "ALL";

	public Payload() {
		super(ReportPage.BROWSER);
	}

	public Date buildEndTime() {
		if (StringUtils.isNotBlank(m_day) && StringUtils.isNotBlank(m_endTime)) {
			try {
				Date date = m_format.parse(m_day + " " + m_endTime);
				return date;
			} catch (ParseException e) {
			}
		}
		return TimeHelper.getCurrentDay(1);
	}

	public int buildLevel() {
		if (StringUtils.isEmpty(m_level) || ALL.equals(m_level)) {
			return -1;
		} else {
			return Level.getCodeByName(m_level);
		}
	}

	public Date buildStartTime() {
		if (StringUtils.isNotBlank(m_day) && StringUtils.isNotBlank(m_startTime)) {
			try {
				Date date = m_format.parse(m_day + " " + m_startTime);
				return date;
			} catch (ParseException e) {
			}
		}
		return TimeHelper.getCurrentHour();
	}

	private Date generateDate(String time, long start) {
		Date date = null;
		String[] times = time.split(":");

		if (times.length == 2) {
			int hour = Integer.parseInt(times[0]);
			int minute = Integer.parseInt(times[1]);
			if (minute > 0) {
				hour += 1;
			}

			date = new Date(TimeHelper.getCurrentDay(start).getTime() + hour * TimeHelper.ONE_HOUR);
			if (date.equals(TimeHelper.getCurrentDay(start, 1))) {
				date = new Date(date.getTime() - TimeHelper.ONE_MINUTE);
			}
		} else {
			date = TimeHelper.getCurrentHour(1);
		}
		return date;
	}

	private Date generateDefaultEnd() {
		Date date = TimeHelper.getCurrentHour(1);
		if (date.equals(TimeHelper.getCurrentDay(System.currentTimeMillis(), 1))) {
			date = new Date(date.getTime() - TimeHelper.ONE_MINUTE);
		}
		return date;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getApi1() {
		return m_api1;
	}

	public String getApi2() {
		return m_api2;
	}

	public String getDay() {
		return m_day;
	}

	public String getDpid() {
		if (StringUtils.isEmpty(m_dpid)) {
			return null;
		} else {
			return m_dpid;
		}
	}

	public String getEndTime() {
		return m_endTime;
	}

	public AjaxDataField getGroupByField() {
		return m_groupByField;
	}

	public Pair<Date, Date> getHistoryEndDatePair() {
		Date currentEnd = generateDefaultEnd();
		Date compareEnd = null;

		try {
			if (m_customEnd != null && m_customEnd.length() > 0) {
				String[] ends = m_customEnd.split(";");
				Pair<Date, Date> startDatePair = getHistoryStartDatePair();
				long start = startDatePair.getKey().getTime();
				currentEnd = generateDate(ends[0], start);

				if (ends.length == 2) {
					start = startDatePair.getValue().getTime();
					compareEnd = generateDate(ends[1], start);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Pair<Date, Date>(currentEnd, compareEnd);
	}

	public Pair<Date, Date> getHistoryStartDatePair() {
		Date currentStart = TimeHelper.getCurrentDay();
		Date compareStart = null;

		try {
			if (m_customStart != null && m_customStart.length() > 0) {
				String[] starts = m_customStart.split(";");
				Date current = m_format.parse(starts[0]);
				currentStart = new Date(current.getTime() - current.getTime() % TimeHelper.ONE_HOUR);

				if (starts.length == 2) {
					Date compare = m_format.parse(starts[1]);
					compareStart = new Date(compare.getTime() - compare.getTime() % TimeHelper.ONE_HOUR);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new Pair<Date, Date>(currentStart, compareStart);
	}

	public int getId() {
		return m_id;
	}

	public String getLevel() {
		return m_level;
	}

	public String getModule() {
		if (StringUtils.isEmpty(m_module)) {
			return null;
		} else {
			return m_module;
		}
	}

	public String getMsg() {
		return m_msg;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getQuery1() {
		return m_query1;
	}
	
	public void setQuery1(String query1) {
		m_query1 = query1;
	}

	public String getQuery2() {
		return m_query2;
	}

	public AjaxDataQueryEntity getQueryEntity1() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new AjaxDataQueryEntity(m_query1);
		} else {
			return new AjaxDataQueryEntity();
		}
	}

	public AjaxDataQueryEntity getQueryEntity2() {
		if (m_query2 != null && m_query2.length() > 0) {
			return new AjaxDataQueryEntity(m_query2);
		} else {
			return null;
		}
	}

	public String getSort() {
		return m_sort;
	}

	public SpeedQueryEntity getSpeedQueryEntity1() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new SpeedQueryEntity(m_query1);
		} else {
			return new SpeedQueryEntity();
		}
	}

	public SpeedQueryEntity getSpeedQueryEntity2() {
		if (m_query2 != null && m_query2.length() > 0) {
			return new SpeedQueryEntity(m_query2);
		} else {
			return null;
		}
	}

	public String getStartTime() {
		return m_startTime;
	}

	public String getType() {
		return m_type;
	}

	public String getUrl() {
		return m_url;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDay(String day) {
		m_day = day;
	}

	public void setDpid(String dpid) {
		m_dpid = dpid;
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	public void setGroupByField(String groupByField) {
		m_groupByField = AjaxDataField.getByName(groupByField, AjaxDataField.CODE);
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setLevel(String level) {
		m_level = level;
	}

	public void setModule(String module) {
		m_module = module;
	}

	public void setMsg(String msg) {
		m_msg = msg;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.BROWSER);
	}

	public void setSort(String sort) {
		m_sort = sort;
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setUrl(String url) {
		m_url = url;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
