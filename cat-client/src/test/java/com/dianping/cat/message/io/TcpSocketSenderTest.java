package com.dianping.cat.message.io;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class TcpSocketSenderTest extends ComponentTestCase {

	@Test
	public void test() throws InterruptedException {
		TcpSocketSender sender = lookup(TcpSocketSender.class);
		List<InetSocketAddress> servers = new ArrayList<InetSocketAddress>();

		servers.add(new InetSocketAddress("cat01.beta", 2280));
		servers.add(new InetSocketAddress("cat02.beta", 2280));
		sender.initialize(servers);
		
		Thread.sleep(5000);

		long start = System.currentTimeMillis();
		int total = 10000000;
		for (int i = 0; i < total; i++) {
			sender.sendInternal(buildMessage());
		}
		long duration = System.currentTimeMillis() - start;

		System.out.println("cost time:" + duration + " qps:"+ total*1000.0/duration);
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("WEB CLUSTER", "GET", 112819) //
				      .at(System.currentTimeMillis()) //
				      .after(1300).child(t("QUICKIE SERVICE", "gimme_stuff", 1571)) //
				      .after(100).child(e("SERVICE", "event1")) //
				      .after(100).child(h("SERVICE", "heartbeat1")) //
				      .after(100).child(t("WEB SERVER", "GET", 109358) //
				            .after(1000).child(t("SOME SERVICE", "get", 4345) //
				                  .after(4000).child(t("MEMCACHED", "Get", 279))) //
				            .mark().after(200).child(t("MEMCACHED", "Inc", 319)) //
				            .reset().after(500).child(t("BIG ASS SERVICE", "getThemDatar", 97155) //
				                  .after(1000).mark().child(t("SERVICE", "getStuff", 3760)) //
				                  .reset().child(t("DATAR", "findThings", 94537)) //
				                  .after(200).child(t("THINGIE", "getMoar", 1435)) //
				            ) //
				            .after(100).mark().child(t("OTHER DATA SERVICE", "get", 4394) //
				                  .after(1000).mark().child(t("MEMCACHED", "Get", 378)) //
				                  .reset().child(t("MEMCACHED", "Get", 3496)) //
				            ) //
				            .reset().child(t("FINAL DATA SERVICE", "get", 4394) //
				                  .after(1000).mark().child(t("MEMCACHED", "Get", 386)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				                  .reset().child(t("MEMCACHED", "Get", 322)) //
				            ) //
				      ) //
				;

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}

}
