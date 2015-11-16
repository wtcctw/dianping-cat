package com.dianping.cat.report.page.server;

import java.io.IOException;

import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.home.screen.entity.Graph;
import com.dianping.cat.home.screen.transform.DefaultSaxParser;

public class GraphTests {

	@Test
	public void test() throws IOException, SAXException {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("graph-config.xml"), "utf-8");

		Graph graph = DefaultSaxParser.parseEntity(Graph.class, xml);
		System.out.println(graph.toString());
	}

}
