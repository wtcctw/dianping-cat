package com.dianping.cat.report.alert.browser;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;
import com.dianping.cat.report.alert.sender.decorator.Decorator;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class JsDecorator extends Decorator implements Initializable {

	public static final String ID = AlertType.JS.getName();

	public Configuration m_configuration;
	
	protected DateFormat m_dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	protected DateFormat m_timeFormat = new SimpleDateFormat("HH:mm");

	@Override
	public void initialize() throws InitializationException {
		m_configuration = new Configuration();
		m_configuration.setDefaultEncoding("UTF-8");
		try {
			m_configuration.setClassForTemplateLoading(this.getClass(), "/freemaker");
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	@Override
	public String generateContent(AlertEntity alert) {
		Map<Object, Object> dataMap = generateExceptionMap(alert);
		StringWriter sw = new StringWriter(5000);

		try {
			Template t = m_configuration.getTemplate("js.ftl");
			t.process(dataMap, sw);
		} catch (Exception e) {
			Cat.logError("build front end content error:" + alert.toString(), e);
		}

		return sw.toString();
	}

	private Map<Object, Object> generateExceptionMap(AlertEntity alert) {
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("level", alert.getLevel().getLevel().toUpperCase());
		map.put("module", alert.getGroup());
		
		Date startTime = alert.getDate();
		map.put("date", m_dateFormat.format(startTime));
		map.put("start", m_timeFormat.format(startTime));
		map.put("end", m_timeFormat.format(alert.getParas().get("end")));
		return map;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("[JS异常告警]").append(alert.getContent());
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
