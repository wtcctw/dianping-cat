package com.dianping.cat.alarm.sender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.alarm.spi.dx.api.PushUtil;
import com.dianping.cat.helper.JsonBuilder;

public class DXSenderTest {

	private static final String HMAC_SHA1 = "HmacSHA1";

	private static final String AUTH_METHOD = "MWS";

	private static final String APPKEY = "0513021Rv2124712";

	private static final String APPSECRET = "415be4e6561846323d5fc48b83fa7a2c";

	private static Map<String, String> getBasicAuthHeader(String method, String url) {
		try {
			Map<String, String> header = new HashMap<String, String>();
			DateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			String date = sdf.format(new Date());
			String encryptStr = String.format("%s %s\n%s", method, new URL(url).getPath(), date);
			String sign = hmacSHA1(APPSECRET, encryptStr);
			header.put("Date", date);
			header.put("Authorization", String.format("%s %s:%s", AUTH_METHOD, APPKEY, sign));
			header.put("Content-type", "application/json;charset=utf-8");
			return header;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public static String hmacSHA1(String key, String data) {
		try {
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1);
			Mac mac = Mac.getInstance(HMAC_SHA1);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes("utf-8"));
			// 必须使用 commons-codec 1.5及以上版本，否则base64加密后会出现换行问题
			return Base64.encodeBase64String(rawHmac);
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage());
		}
	}

	private void httpPostSend(String urlPrefix, String content) {
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

			Map<String, String> header = getBasicAuthHeader("post", "http://xm-in.sankuai.com/api/message");

			System.out.println(header);
			for (Entry<String, String> entry : header.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}

			writer = new OutputStreamWriter(conn.getOutputStream());

			writer.write(content);
			writer.flush();

			in = conn.getInputStream();
			StringBuilder sb = new StringBuilder();

			sb.append(Files.forIO().readFrom(in, "utf-8")).append("");

			System.out.println(sb);
		} catch (Exception e) {
			e.printStackTrace();
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

	public void test() {
		List<String> receivers = Arrays.asList("jialin.sun@dianping.com");
		Map<String, Object> datas = new HashMap<String, Object>();

		datas.put("sender", "cat2281@meituan.com");
		datas.put("receivers", receivers);
		datas.put("body", "this is a test");
		datas.put("type", "text/markup");

		String jsonData = new JsonBuilder().toJson(datas);
		System.out.println(jsonData);
		httpPostSend("http://xm-in.sankuai.com/api/message", jsonData);
	}

	@Test
	public void test2() {
		PushUtil.init("0513021Rv2124712", "415be4e6561846323d5fc48b83fa7a2c", "cat2281@meituan.com",
		      "http://xm-in.sankuai.com/api");
		long data = PushUtil.push("Hello OA!", "jialin.sun@dianping.com");
		System.out.println(data);

		// XLink link = new XLink();
		// link.setTitle("Title");
		// link.setContent("Content");
		// link.setImage("aaaa");
		// link.setLink("bbbb");
		//
		// PushUtil.push(link, "zhangdongxiao@meituan.com");
		//
		// XMultiCard multiCard = new XMultiCard();
		//
		// Article link = new Article();
		// link.setTitle("上单通知");
		// link.setContent("恭喜你，上单.....");
		// link.setImage("http://xs.xm.sankuai.com/s/group1/M00/00/0A/CkANwVHMUgyAR69-AAJUwLTcAqs3602029");
		// link.setLink("http://apps.xm.sankuai.com");
		//
		// multiCard.addArticles(link);
		//
		// link = new Article();
		// link.setTitle("驳回通知");
		// link.setContent("非常抱歉，上单.....");
		// link.setImage("http://xs.xm.sankuai.com/s/group1/M00/00/0A/CkANwVHMUgyAR69-AAJUwLTcAqs3602029");
		// link.setLink("http://apps.xm.sankuai.com");
		// multiCard.addArticles(link);
		//
		// link = new Article();
		// link.setTitle("下线通知");
		// link.setContent("郑重提醒，上单.....");
		// link.setImage("http://xs.xm.sankuai.com/s/group1/M00/00/0A/CkANwVHMUgyAR69-AAJUwLTcAqs3602029");
		// link.setLink("http://apps.xm.sankuai.com");
		// multiCard.addArticles(link);
		//
		// PushUtil.push(multiCard, "zhangdongxiao@meituan.com");
		//
		// PushUtil.pushToRoom(multiCard, 96709); // 推送群组 msg, roomPid
		//
		// PushUtil.pushToRoom("测试信息", 96709); // 推送群组 msg, roomPid
	}

}
