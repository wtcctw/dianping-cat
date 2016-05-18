package com.dianping.cat.alarm.spi.dx.api;

import java.util.Collection;
import java.util.Date;

import com.dianping.cat.alarm.spi.dx.http.MtHttpUtil;

public class Pusher {

	private String baseUrl;

	private String appkey;

	private String token;

	private String sender;

	private int socket_timeout = 0;

	private int conn_timeout = 0;

	public void init(String appkey, String token, String sender, String baseUrl) {
		this.appkey = appkey;
		this.token = token;
		this.sender = sender;
		this.baseUrl = baseUrl;
	}

	public void init(String appkey, String token, String sender, String baseUrl, int socket_timeout, int conn_timeout) {
		this.appkey = appkey;
		this.token = token;
		this.sender = sender;
		this.baseUrl = baseUrl;
		this.socket_timeout = socket_timeout;
		this.conn_timeout = conn_timeout;
	}

	public long push(String body, String... receivers) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setReceivers(receivers);
		msg.setType("text/markup");
		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

	public long push(XBody body, String... receivers) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setReceivers(receivers);
		msg.setType(body.type());

		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

	public long push(String body, long crowdId) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setCrowdId(crowdId);
		msg.setType("text/markup");
		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

	public long push(XBody body, long crowdId) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setCrowdId(crowdId);
		msg.setType(body.type());

		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

	public long pushToRoom(String body, long roomId) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setRoomId(roomId);
		msg.setType("text/markup");
		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

	public long pushToRoom(XBody body, long roomId) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setRoomId(roomId);
		msg.setType(body.type());

		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

	private String[] getSignHeaders() {
		String date = MtApiUtil.getDateString(new Date());
		String stringToSign = "PUT /api/message\n" + date;
		String check = MtApiUtil.getSignature(stringToSign, token);

		return new String[] { "Authorization", "MWS " + appkey + ":" + check, "Date", date };
	}

	public long push(String body, Collection<String> receivers) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setReceivers(receivers.toArray(new String[receivers.size()]));
		msg.setType("text/markup");
		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

	public long push(XBody body, Collection<String> receivers) {
		XMessage msg = new XMessage();
		msg.setBody(body);
		msg.setSender(sender);
		msg.setReceivers(receivers.toArray(new String[receivers.size()]));
		msg.setType(body.type());
		return MtHttpUtil.put(baseUrl + "/message", msg, Long.class, socket_timeout, conn_timeout, getSignHeaders());
	}

}
