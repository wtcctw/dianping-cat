package com.dianping.cat.report.page.browser;

public enum Action implements org.unidal.web.mvc.Action {
	
	VIEW("view"),

	PIECHART("piechart"),

	JS_ERROR("jsError"),
	
	JS_ERROR_DETAIL("jsErrorDetail"),
	
	SPEED("speed"),
	
	SPEED_GRAPH("speedGraph");

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
