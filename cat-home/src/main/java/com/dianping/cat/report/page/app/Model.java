package com.dianping.cat.report.page.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.unidal.tuple.Pair;
import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.app.entity.Code;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.configuration.app.entity.Item;
import com.dianping.cat.configuration.server.filter.entity.CrashLogDomain;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.app.display.AppCommandDisplayInfo;
import com.dianping.cat.report.page.app.display.AppConnectionDisplayInfo;
import com.dianping.cat.report.page.app.display.AppDataDetail;
import com.dianping.cat.report.page.app.display.AppSpeedDisplayInfo;
import com.dianping.cat.report.page.app.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.app.display.CrashLogDisplayInfo;
import com.dianping.cat.report.page.app.display.DashBoardInfo;
import com.dianping.cat.report.page.app.display.DisplayCommands;

@ModelMeta(Constants.APP)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	@EntityMeta
	private LineChart m_lineChart;

	private AppCommandDisplayInfo m_commandDisplayInfo;

	private AppConnectionDisplayInfo m_connDisplayInfo;

	private Map<Integer, Item> m_cities;

	private Map<Integer, Item> m_versions;

	private Map<Integer, Item> m_connectionTypes;

	private Map<Integer, Item> m_operators;

	private Map<Integer, Item> m_networks;

	private Map<Integer, Item> m_platforms;

	private Map<Integer, Item> m_sources;

	private Map<Integer, Code> m_globalCodes;

	private List<Command> m_commands;

	private List<AppDataDetail> m_appDataDetailInfos;

	private Map<String, AppDataDetail> m_comparisonAppDetails;

	private AppSpeedDisplayInfo m_appSpeedDisplayInfo;

	private String m_content;

	private String m_fetchData;

	private int m_commandId;

	private ProblemStatistics m_problemStatistics;

	private ProblemReport m_problemReport;

	private Map<Integer, Code> m_codes;

	private List<String> m_codeDistributions;

	private Map<Integer, List<Code>> m_command2Codes;

	private Map<String, Pair<String, String>> m_domain2Departments;

	private Map<String, Command> m_command2Id;

	private String m_defaultCommand;

	@EntityMeta
	private AppReport m_appReport;

	private DisplayCommands m_displayCommands;

	private Collection<CrashLogDomain> m_crashLogDomains;

	private CrashLogDetailInfo m_crashLogDetailInfo;

	private CrashLogDisplayInfo m_crashLogDisplayInfo;

	private DashBoardInfo m_dashBoardInfo;

	public Model(Context ctx) {
		super(ctx);
	}

	public List<AppDataDetail> getAppDataDetailInfos() {
		return m_appDataDetailInfos;
	}

	public AppReport getAppReport() {
		return m_appReport;
	}

	public AppSpeedDisplayInfo getAppSpeedDisplayInfo() {
		return m_appSpeedDisplayInfo;
	}

	public Map<Integer, Item> getCities() {
		return m_cities;
	}

	public List<String> getCodeDistributions() {
		return m_codeDistributions;
	}

	public Map<Integer, Code> getCodes() {
		return m_codes;
	}

	public Map<Integer, List<Code>> getCommand2Codes() {
		return m_command2Codes;
	}

	public String getCommand2CodesJson() {
		return new JsonBuilder().toJson(m_command2Codes);
	}

	public Map<String, Command> getCommand2Id() {
		return m_command2Id;
	}

	public String getCommand2IdJson() {
		return new JsonBuilder().toJson(m_command2Id);
	}

	public AppCommandDisplayInfo getCommandDisplayInfo() {
		return m_commandDisplayInfo;
	}

	public int getCommandId() {
		return m_commandId;
	}

	public List<Command> getCommands() {
		return m_commands;
	}

	public Map<String, AppDataDetail> getComparisonAppDetails() {
		return m_comparisonAppDetails;
	}

	public AppConnectionDisplayInfo getConnDisplayInfo() {
		return m_connDisplayInfo;
	}

	public Map<Integer, Item> getConnectionTypes() {
		return m_connectionTypes;
	}

	public String getContent() {
		return m_content;
	}

	public CrashLogDetailInfo getCrashLogDetailInfo() {
		return m_crashLogDetailInfo;
	}

	public CrashLogDisplayInfo getCrashLogDisplayInfo() {
		return m_crashLogDisplayInfo;
	}

	public Collection<CrashLogDomain> getCrashLogDomains() {
		return m_crashLogDomains;
	}

	public DashBoardInfo getDashBoardInfo() {
		return m_dashBoardInfo;
	}

	@Override
	public Action getDefaultAction() {
		return Action.LINECHART;
	}

	public String getDefaultCommand() {
		return m_defaultCommand;
	}

	public DisplayCommands getDisplayCommands() {
		return m_displayCommands;
	}

	@Override
	public String getDomain() {
		return getDisplayDomain();
	}

	public Map<String, Pair<String, String>> getDomain2Departments() {
		return m_domain2Departments;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public String getFetchData() {
		return m_fetchData;
	}

	public String getGlobalCodesJson() {
		return new JsonBuilder().toJson(m_globalCodes);
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public Map<Integer, Item> getNetworks() {
		return m_networks;
	}

	public Map<Integer, Item> getOperators() {
		return m_operators;
	}

	public Map<Integer, Item> getPlatforms() {
		return m_platforms;
	}

	public ProblemReport getProblemReport() {
		return m_problemReport;
	}

	public ProblemStatistics getProblemStatistics() {
		return m_problemStatistics;
	}

	public Map<Integer, Item> getSources() {
		return m_sources;
	}

	public Map<Integer, Item> getVersions() {
		return m_versions;
	}

	public void setAppDataDetailInfos(List<AppDataDetail> appDataDetailInfos) {
		m_appDataDetailInfos = appDataDetailInfos;
	}

	public void setAppReport(AppReport appReport) {
		m_appReport = appReport;
	}

	public void setAppSpeedDisplayInfo(AppSpeedDisplayInfo appSpeedDisplayInfo) {
		m_appSpeedDisplayInfo = appSpeedDisplayInfo;
	}

	public void setCities(Map<Integer, Item> cities) {
		m_cities = cities;
	}

	public void setCodeDistributions(List<String> codeDistributions) {
		m_codeDistributions = codeDistributions;
	}

	public void setCodes(Map<Integer, Code> codes) {
		m_codes = codes;
	}

	public void setCommand2Codes(Map<Integer, List<Code>> command2Codes) {
		m_command2Codes = command2Codes;
	}

	public void setCommand2Id(Map<String, Command> rawCommands) {
		m_command2Id = rawCommands;
	}

	public void setCommandDisplayInfo(AppCommandDisplayInfo commandDisplayInfo) {
		m_commandDisplayInfo = commandDisplayInfo;
	}

	public void setCommandId(int commandId) {
		m_commandId = commandId;
	}

	public void setCommands(List<Command> commands) {
		m_commands = commands;
	}

	public void setComparisonAppDetails(Map<String, AppDataDetail> comparisonAppDetail) {
		m_comparisonAppDetails = comparisonAppDetail;
	}

	public void setConnDisplayInfo(AppConnectionDisplayInfo connDisplayInfo) {
		m_connDisplayInfo = connDisplayInfo;
	}

	public void setConnectionTypes(Map<Integer, Item> map) {
		m_connectionTypes = map;
	}

	public void setContent(String content) {
		m_content = content;
	}

	public void setCrashLogDetailInfo(CrashLogDetailInfo crashLogDetailInfo) {
		m_crashLogDetailInfo = crashLogDetailInfo;
	}

	public void setCrashLogDisplayInfo(CrashLogDisplayInfo crashLogDisplayInfo) {
		m_crashLogDisplayInfo = crashLogDisplayInfo;
	}

	public void setCrashLogDomains(Collection<CrashLogDomain> crashLogDomains) {
		m_crashLogDomains = crashLogDomains;
	}

	public void setDashBoardInfo(DashBoardInfo dashBoardInfo) {
		m_dashBoardInfo = dashBoardInfo;
	}

	public void setDefaultCommand(String defaultCommand) {
		m_defaultCommand = defaultCommand;
	}

	public void setDisplayCommands(DisplayCommands displayCommands) {
		m_displayCommands = displayCommands;
	}

	public void setDomain2Departments(Map<String, Pair<String, String>> domain2Departments) {
		m_domain2Departments = domain2Departments;
	}

	public void setFetchData(String fetchData) {
		m_fetchData = fetchData;
	}

	public void setGlobalCodes(Map<Integer, Code> globalCodes) {
		m_globalCodes = globalCodes;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public void setNetworks(Map<Integer, Item> networks) {
		m_networks = networks;
	}

	public void setOperators(Map<Integer, Item> operators) {
		m_operators = operators;
	}

	public void setPlatforms(Map<Integer, Item> platforms) {
		m_platforms = platforms;
	}

	public void setProblemReport(ProblemReport problemReport) {
		m_problemReport = problemReport;
	}

	public void setProblemStatistics(ProblemStatistics problemStatistics) {
		m_problemStatistics = problemStatistics;
	}

	public void setSources(Map<Integer, Item> sources) {
		m_sources = sources;
	}

	public void setVersions(Map<Integer, Item> versions) {
		m_versions = versions;
	}

}
