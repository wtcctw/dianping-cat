package com.dianping.cat.alarm.spi.dx.api;

public class XMessage {
	private String sender;

	private String[] receivers;

	private long crowdId;

	private long roomId;

	private String type;

	private String title;

	private Object body;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String[] getReceivers() {
		return receivers;
	}

	public void setReceivers(String... receivers) {
		this.receivers = receivers;
	}

	public long getCrowdId() {
		return crowdId;
	}

	public void setCrowdId(long crowdId) {
		this.crowdId = crowdId;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}
}
