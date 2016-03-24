package com.dianping.cat.system.page.app;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.ConfigItem;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.configuration.group.entity.AppCommandGroupConfig;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.system.SystemPage;

public class Model extends ViewModel<SystemPage, Action, Context> {

	private Map<Integer, Item> m_cities;

	private Map<Integer, Item> m_versions;

	private Map<Integer, Item> m_connectionTypes;

	private Map<Integer, Speed> m_speeds;

	private Map<Integer, Code> m_codes;

	private Map<Integer, Item> m_operators;

	private Map<Integer, Item> m_networks;

	private Map<Integer, Item> m_platforms;

	private String m_id;

	private String m_domain;

	private List<Command> m_commands;

	private AppConfigManager m_appConfigManager;

	private String m_nameUniqueResult;

	private Command m_updateCommand;

	private List<String> m_validatePaths;

	private List<String> m_invalidatePaths;

	public static final String SUCCESS = "Success";

	public static final String FAIL = "Fail";

	private String m_opState = SUCCESS;

	private Code m_code;

	private Speed m_speed;

	private String m_content;

	private Collection<Rule> m_rules;

	private Item m_appItem;

	private String m_configHeader;

	private AppCommandGroupConfig m_commandGroupConfig;

	public Model(Context ctx) {
		super(ctx);
		try {
			m_appConfigManager = ContainerLoader.getDefaultContainer().lookup(AppConfigManager.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public Map<String, List<Command>> getApiCommands() {
		return m_appConfigManager.queryDomain2Commands();
	}

	public Item getAppItem() {
		return m_appItem;
	}

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public Code getCode() {
		return m_code;
	}

	public Map<Integer, Code> getCodes() {
		return m_codes;
	}

	public AppCommandGroupConfig getCommandGroupConfig() {
		return m_commandGroupConfig;
	}

	public String getCommandJson() {
		return new JsonBuilder().toJson(m_appConfigManager.queryCommand2Codes());
	}

	public List<Command> getCommands() {
		return m_commands;
	}

	public String getConfigHeader() {
		return m_configHeader;
	}

	public Map<String, ConfigItem> getConfigItems() {
		return m_appConfigManager.getConfig().getConfigItems();
	}

	public Map<Integer, Item> getConnectionTypes() {
		return m_connectionTypes;
	}

	public String getContent() {
		return m_content;
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.APP_LIST;
	}

	public String getDomain() {
		return m_domain;
	}

	public String getDomain2CommandsJson() {
		return new JsonBuilder().toJson(m_appConfigManager.queryDomain2Commands());
	}

	public String getId() {
		return m_id;
	}

	public List<String> getInvalidatePaths() {
		return m_invalidatePaths;
	}

	public String getIpAddress() {
		return "";
	}

	public String getNameUniqueResult() {
		return m_nameUniqueResult;
	}

	public Map<Integer, Item> getNetworks() {
		return m_networks;
	}

	public Map<Integer, Item> getOperators() {
		return m_operators;
	}

	public String getOpState() {
		return m_opState;
	}

	public Map<Integer, Item> getPlatforms() {
		return m_platforms;
	}

	public String getReportType() {
		return "";
	}

	public Collection<Rule> getRules() {
		return m_rules;
	}

	public Speed getSpeed() {
		return m_speed;
	}

	public Map<Integer, Speed> getSpeeds() {
		return m_speeds;
	}

	public Command getUpdateCommand() {
		return m_updateCommand;
	}

	public List<String> getValidatePaths() {
		return m_validatePaths;
	}

	public Map<Integer, Item> getVersions() {
		return m_versions;
	}

	public void setAppItem(Item appItem) {
		m_appItem = appItem;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
	}

	public void setCode(Code code) {
		m_code = code;
	}

	public void setCodes(Map<Integer, Code> codes) {
		m_codes = codes;
	}

	public void setCommandGroupConfig(AppCommandGroupConfig commandGroupConfig) {
		m_commandGroupConfig = commandGroupConfig;
	}

	public void setCommands(List<Command> commands) {
		m_commands = commands;
	}

	public void setConfigHeader(String configHeader) {
		m_configHeader = configHeader;
	}

	public void setConnectionTypes(Map<Integer, Item> connectionTypes) {
		m_connectionTypes = connectionTypes;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setId(String id) {
		m_id = id;
	}

	public void setInvalidatePaths(List<String> invalidatePaths) {
		m_invalidatePaths = invalidatePaths;
	}

	public void setNameUniqueResult(String nameUniqueResult) {
		m_nameUniqueResult = nameUniqueResult;
	}

	public void setNetworks(Map<Integer, Item> networks) {
		m_networks = networks;
	}

	public void setOperators(Map<Integer, Item> operators) {
		m_operators = operators;
	}

	public void setOpState(boolean result) {
		if (result) {
			m_opState = SUCCESS;
		} else {
			m_opState = FAIL;
		}
	}

	public void setPlatforms(Map<Integer, Item> platforms) {
		m_platforms = platforms;
	}

	public void setRules(Collection<Rule> rules) {
		m_rules = rules;
	}

	public void setSpeed(Speed speed) {
		m_speed = speed;
	}

	public void setSpeeds(Map<Integer, Speed> speeds) {
		m_speeds = speeds;
	}

	public void setUpdateCommand(Command updateCommand) {
		m_updateCommand = updateCommand;
	}

	public void setValidatePaths(List<String> validatePaths) {
		m_validatePaths = validatePaths;
	}

	public void setVersions(Map<Integer, Item> versions) {
		m_versions = versions;
	}
}
