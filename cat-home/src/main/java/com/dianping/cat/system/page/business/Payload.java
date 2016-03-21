package com.dianping.cat.system.page.business;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.system.SystemPage;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain = Constants.CAT;

	@FieldMeta("key")
	private String m_key;

	@FieldMeta("content")
	private String m_content;

	@FieldMeta("attributes")
	private String m_attribtues;

	@ObjectMeta("businessItemConfig")
	private BusinessItemConfig m_businessItemConfig = new BusinessItemConfig();

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.LIST);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.BUSINESS);
	}

	public String getReportType() {
		return "";
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public String getAttribtues() {
		return m_attribtues;
	}

	public void setAttribtues(String attribtues) {
		m_attribtues = attribtues;
	}

	public String getContent() {
		return m_content;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public BusinessItemConfig getBusinessItemConfig() {
		return m_businessItemConfig;
	}

	public void setBusinessItemConfig(BusinessItemConfig businessItemConfig) {
		m_businessItemConfig = businessItemConfig;
	}

	public String getKey() {
		return m_key;
	}

	public void setKey(String key) {
		m_key = key;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.LIST;
		}
	}
}
