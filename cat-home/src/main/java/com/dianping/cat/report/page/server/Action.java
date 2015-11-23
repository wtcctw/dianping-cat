package com.dianping.cat.report.page.server;

public enum Action implements org.unidal.web.mvc.Action {
	GRAPH("graph"),

	AGGREGATE("aggregate"),

	ENDPOINT("endPoint"),

	MEASUREMTN("measurement"),

	BUILDVIEW("buildview"),

	SCREEN("screen"),

	SCREENS("screens"),

	SCREEN_UPDATE("screenUpdate"),

	SCREEN_DELETE("screenDelete"),

	SCREEN_SUBMIT("screenSubmit"),

	GRAPH_UPDATE("graphUpdate"),

	GRAPH_SUBMIT("graphSubmit"),

	;

	private String m_name;

	private Action(String name) {
		m_name = name;
	}

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
