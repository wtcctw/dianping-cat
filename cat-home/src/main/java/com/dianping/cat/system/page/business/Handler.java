package com.dianping.cat.system.page.business;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.alarm.spi.decorator.RuleFTLDecorator;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.business.ConfigItem;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.report.alert.business2.BusinessRuleConfigManager2;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.SystemPage;
import com.dianping.cat.system.page.business.config.BusinessTagConfigManager;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private BusinessConfigManager m_configManager;

	@Inject
	private BusinessTagConfigManager m_tagConfigManger;

	@Inject
	private BusinessRuleConfigManager2 m_alertConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	@Inject
	protected RuleFTLDecorator m_ruleDecorator;

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "business")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "business")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();
		String domain = payload.getDomain();

		model.setPage(SystemPage.BUSINESS);
		model.setAction(action);
		model.setDomains(m_projectService.findAllDomains());

		switch (action) {
		case LIST:
			listConfigs(domain, model);
			break;
		case ADD:
			BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);

			if (config != null) {
				BusinessItemConfig itemConfig = config.findBusinessItemConfig(payload.getKey());
				if (itemConfig != null) {
					model.setBusinessItemConfig(itemConfig);
				}
			}
			break;
		case AddSubmit:
			updateConfig(model, payload, domain);
			break;
		case DELETE:
			String key = payload.getKey();

			m_configManager.deleteConfig(domain, key);
			listConfigs(domain, model);
			break;
		case TagConfig:
			String tagConfig = payload.getContent();

			if (!StringUtils.isEmpty(tagConfig)) {
				model.setOpState(m_tagConfigManger.store(tagConfig));
			}
			model.setContent(m_configHtmlParser.parse(m_tagConfigManger.getConfig().toString()));
			break;
		case AlertRuleAdd:
			alertRuleAdd(payload, model);
			break;
		case AlertRuleAddSubmit:
			alertRuleAddSubmit(payload, model);
			listConfigs(domain, model);
			break;

		}

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private void alertRuleAddSubmit(Payload payload, Model model) {
		String domain = payload.getDomain();
		String key = payload.getKey();
		String configs = payload.getContent();
		String attributes = payload.getAttribtues();

		m_alertConfigManager.updateRule(domain, key, configs, attributes);
	}

	private void alertRuleAdd(Payload payload, Model model) {
		String ruleId = "";
		String configsStr = "";
		String key = payload.getKey();
		String domain = payload.getDomain();
		Rule rule = m_alertConfigManager.queryRule(domain, key);
		String attributes = null;

		if (rule != null) {
			ruleId = rule.getId();
			attributes = StringUtils.join(rule.getDynamicAttributes().keySet(), ";");
			configsStr = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
		}
		String content = m_ruleDecorator.generateConfigsHtml(configsStr);

		model.setId(ruleId);
		model.setAttributes(attributes);
		model.setContent(content);
	}

	private void listConfigs(String domain, Model model) {
		BusinessReportConfig config = m_configManager.queryConfigByDomain(domain);
		Map<String, Set<String>> tags = m_tagConfigManger.findTagByDomain(domain);

		model.setConfig(config);
		model.setTags(tags);
	}

	private void updateConfig(Model model, Payload payload, String domain) {
		BusinessReportConfig config;
		BusinessItemConfig itemConfig = payload.getBusinessItemConfig();
		String key = itemConfig.getId();
		config = m_configManager.queryConfigByDomain(domain);
		boolean isModify = false;
		boolean result = false;

		if (config != null) {
			Map<String, BusinessItemConfig> itemConfigs = config.getBusinessItemConfigs();
			BusinessItemConfig origin = itemConfigs.get(key);

			if (origin != null) {
				isModify = true;
				config.addBusinessItemConfig(itemConfig);
				result = m_configManager.updateConfigByDomain(config);
			}
		}

		if (!isModify) {
			ConfigItem item = new ConfigItem();

			item.setShowAvg(itemConfig.getShowAvg());
			item.setShowCount(itemConfig.getShowCount());
			item.setShowSum(itemConfig.getShowSum());
			item.setTitle(itemConfig.getTitle());
			item.setViewOrder(itemConfig.getViewOrder());

			result = m_configManager.insertBusinessConfigIfNotExist(domain, key, item);
		}
		model.setBusinessItemConfig(itemConfig);
		model.setOpState(result);
	}
}
