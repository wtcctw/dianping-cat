package com.dianping.cat.config.content;

import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;

@Named(type = ContentFetcher.class)
public class LocalResourceContentFetcher implements ContentFetcher {
	private final String PATH = "/config/";

	@Override
	public String getConfigContent(String configName) {
		String path = PATH + configName + ".xml";
		String content = "";

		try {
			content = Files.forIO().readFrom(this.getClass().getResourceAsStream(path), "utf-8");
		} catch (Exception e) {
			System.err.println(configName+" can't find");
			Cat.logError(configName + " can't find", e);
		}
		return content;
	}
}
