package com.dianping.cat.alarm.spi.dx.api;

import java.util.Collection;

public class PushUtil {

	private static Pusher pusher;

	public static void init(String appkey, String token, String sender, String baseUrl) {
		pusher = new Pusher();
		pusher.init(appkey, token, sender, baseUrl);
	}

	public static void init(String appkey, String token, String sender, String baseUrl, int socket_timeout,
	      int conn_timeout) {
		pusher = new Pusher();
		pusher.init(appkey, token, sender, baseUrl, socket_timeout, conn_timeout);
	}

	public static long push(String body, String... receivers) {
		return pusher.push(body, receivers);
	}

	public static long push(XBody body, String... receivers) {
		return pusher.push(body, receivers);
	}

	public static long push(String body, long crowdId) {
		return pusher.push(body, crowdId);
	}

	public static long push(XBody body, long crowdId) {
		return pusher.push(body, crowdId);
	}

	public static long pushToRoom(String body, long roomId) {
		return pusher.pushToRoom(body, roomId);
	}

	public static long pushToRoom(XBody body, long roomId) {
		return pusher.pushToRoom(body, roomId);
	}

	public static long push(String body, Collection<String> receivers) {
		return pusher.push(body, receivers);
	}

	public static long push(XBody body, Collection<String> receivers) {
		return pusher.push(body, receivers);
	}

}