package com.dianping.cat.report.page.app;

public enum Action implements org.unidal.web.mvc.Action {

	LINECHART("view"),

	LINECHART_JSON("linechartJson"),

	PIECHART("piechart"),

	PIECHART_JSON("piechartJson"),

	APP_COMMAND_DAILY("commandDaily"),

	APP_ADD("appAdd"),

	APP_DELETE("appDelete"),

	APP_CONFIG_FETCH("appConfigFetch"),

	APP_CRASH_LOG("appCrashLog"),

	APP_CRASH_LOG_JSON("appCrashLogJson"),

	APP_CRASH_LOG_DETAIL("appCrashLogDetail"),

	APP_CRASH_GRAPH("appCrashGraph"),

	SPEED("speed"),

	SPEED_JSON("speedJson"),

	SPEED_GRAPH("speedGraph"),

	CONN_LINECHART("connLinechart"),

	CONN_LINECHART_JSON("connLinechartJson"),

	CONN_PIECHART("connPiechart"),

	CONN_PIECHART_JSON("connPiechartJson"),

	STATISTICS("statistics"),

	DASHBOARD("dashboard");

	private String m_name;

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	private Action(String name) {
		m_name = name;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
