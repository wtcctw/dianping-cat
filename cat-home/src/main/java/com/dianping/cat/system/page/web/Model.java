package com.dianping.cat.system.page.web;

import java.util.Map;

import com.dianping.cat.configuration.web.speed.entity.Speed;
import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ViewModel;

public class Model extends ViewModel<SystemPage, Action, Context> {

	private Map<String, Speed> m_speeds;

	private Step m_step;

	public Model(Context ctx) {
		super(ctx);
	}

	public Map<String, Speed> getSpeeds() {
		return m_speeds;
	}

	public Step getStep() {
		return m_step;
	}

	public String getDomain() {
		return "";
	}

	public String getIpAddress() {
		return "";
	}

	public String getDate() {
		return "";
	}

	public void setSpeeds(Map<String, Speed> speeds) {
		m_speeds = speeds;
	}

	public void setStep(Step step) {
		m_step = step;
	}

	@Override
	public Action getDefaultAction() {
		return Action.SPEED_LIST;
	}
}
