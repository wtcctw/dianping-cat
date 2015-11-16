package com.dianping.cat.system.page.web;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import com.dianping.cat.Cat;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.configuration.web.speed.entity.Speed;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dal.report.ConfigModification;
import com.dianping.cat.home.dal.report.ConfigModificationDao;
import com.dianping.cat.system.SystemPage;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private WebSpeedConfigManager m_webSpeedConfigManager;

	@Inject
	private ConfigModificationDao m_configModificationDao;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "web")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "web")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		model.setPage(SystemPage.WEB);
		storeModifyInfo(ctx, payload);

		Action action = payload.getAction();
		model.setAction(action);

		switch (action) {
		case SPEED_DELETE:
			String webpage = payload.getWebPage();
			int pageId = m_webSpeedConfigManager.querySpeedId(webpage);
			int stepId = payload.getStepId();
			m_webSpeedConfigManager.deleteStep(pageId, stepId);
			model.setSpeeds(m_webSpeedConfigManager.getSpeeds());
			break;
		case SPEED_LIST:
			model.setSpeeds(m_webSpeedConfigManager.getSpeeds());
			break;
		case SPEED_SUBMIT:
			Step step = payload.getStep();
			String page = step.getPage();
			Speed speed = m_webSpeedConfigManager.querySpeed(page);

			if (speed == null) {
				int id = m_webSpeedConfigManager.generateSpeedId();
				speed = new Speed();
				speed.setId(id);
				speed.setPage(page);
			}

			addNewStep(step, speed);
			m_webSpeedConfigManager.updateConfig(speed);
			model.setSpeeds(m_webSpeedConfigManager.getSpeeds());
			break;
		case SPEED_UPDATE:
			queryStep(model, payload);
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	private void addNewStep(Step step, Speed speed) {
		com.dianping.cat.configuration.web.speed.entity.Step newStep = new com.dianping.cat.configuration.web.speed.entity.Step();
		int stepId = step.getStepid();

		if (step.getStepid() == 0) {
			stepId = m_webSpeedConfigManager.generateStepId(step.getPage());
		}

		newStep.setId(stepId);
		newStep.setTitle(step.getStep());
		speed.addStep(newStep);
	}

	private void queryStep(Model model, Payload payload) {
		String page = payload.getWebPage();
		int stepId = payload.getStepId();
		Step step = new Step();

		if (page != null) {
			Speed speed = m_webSpeedConfigManager.querySpeed(page);

			if (speed != null) {
				step.setPageid(speed.getId());
				step.setPage(speed.getPage());
				step.setStepid(stepId);
				step.setStep(speed.getSteps().get(stepId).getTitle());
			}
		}

		model.setStep(step);
	}

	public void store(String userName, String accountName, Payload payload) {
		ConfigModification modification = m_configModificationDao.createLocal();

		modification.setUserName(userName);
		modification.setAccountName(accountName);
		modification.setActionName(payload.getAction().getName());
		modification.setDate(new Date());
		modification.setArgument(new JsonBuilder().toJson(payload));

		try {
			m_configModificationDao.insert(modification);
		} catch (Exception ex) {
			Cat.logError(ex);
		}
	}

	private void storeModifyInfo(Context ctx, Payload payload) {
		Cookie cookie = ctx.getCookie("ct");

		if (cookie != null) {
			String cookieValue = cookie.getValue();

			try {
				String[] values = cookieValue.split("\\|");
				String userName = values[0];
				String account = values[1];

				if (userName.startsWith("\"")) {
					userName = userName.substring(1, userName.length() - 1);
				}
				userName = URLDecoder.decode(userName, "UTF-8");

				store(userName, account, payload);
			} catch (Exception ex) {
				Cat.logError("store cookie fail:" + cookieValue, new RuntimeException());
			}
		}
	}
}
