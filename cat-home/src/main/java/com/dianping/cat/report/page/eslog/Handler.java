package com.dianping.cat.report.page.eslog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

import com.dianping.cat.Cat;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.system.page.config.ConfigHtmlParser;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private EsServerConfigManager m_configManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	private static String s_pars = "{ \"query\": { \"bool\": { \"filter\": [ { \"term\": { \"dpid\": \"${dpId}\" } } , {  \"range\" : { \"request_time\" : { \"gte\" : \"${start}\", \"lte\" : \"${end}\" , \"format\": \"yyyy-MM-dd HH:mm:ss\"}  }  } ] } } }";

	private void buildConfig(Model model, Payload payload) {
		String content = payload.getContent();

		if (content != null && content.length() > 0) {
			m_configManager.insert(content);
		}

		String xml = m_configManager.getConfig().toString();

		model.setContent(m_configHtmlParser.parse(xml));
	}

	private void buildQuery(Model model, Payload payload) {
		Date start = payload.getStartDate();
		Date end = payload.getEndDate();
		String dpid = payload.getDpid();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (dpid != null) {
			String pars = s_pars.replace("${dpId}", dpid).replace("${start}", sdf.format(start))
			      .replace("${end}", sdf.format(end));
			String url = m_configManager.getLogServer(payload.getType());

			if (url != null) {
				try {
					String content = httpPostSend(url, pars);
					JsonObject object = new JsonObject(content);
					JsonArray hits = object.getJSONObject("hits").getJSONArray("hits");
					int length = hits.length();
					List<String> logs = new ArrayList<String>();

					for (int i = 0; i < length; i++) {
						JsonObject log = hits.getJSONObject(i).getJSONObject("_source");

						logs.add(log.toString());
					}
					model.setLogs(logs);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "eslog")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "eslog")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		switch (action) {
		case VIEW:
			buildQuery(model, payload);
			break;
		case CONFIG:
			buildConfig(model, payload);
			break;
		}

		model.setAction(action);
		model.setPage(ReportPage.ESLOG);
		model.setLogTypes(m_configManager.getLogTypes());

		if (!ctx.isProcessStopped()) {
			m_jspViewer.view(ctx, model);
		}
	}

	private String httpPostSend(String urlPrefix, String content) {
		URL url = null;
		InputStream in = null;
		OutputStreamWriter writer = null;
		URLConnection conn = null;

		try {
			url = new URL(urlPrefix);
			conn = url.openConnection();

			conn.setConnectTimeout(2000);
			conn.setReadTimeout(3000);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestProperty("content-type", "application/x-www-form-urlencoded;charset=UTF-8");
			writer = new OutputStreamWriter(conn.getOutputStream());

			writer.write(content);
			writer.flush();

			in = conn.getInputStream();
			StringBuilder sb = new StringBuilder();

			sb.append(Files.forIO().readFrom(in, "utf-8")).append("");

			return sb.toString();
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
			}
		}
	}

}
