package com.dianping.cat.report.page.server.display;

import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.MetricScreen;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;

public class MetricScreenTransformer {

	public MetricScreen transform2MetricScreen(MetricScreenInfo screenInfo) {
		MetricScreen metricScreen = new MetricScreen();

		metricScreen.setName(screenInfo.getName());
		metricScreen.setGraphName(screenInfo.getGraphName());
		metricScreen.setCategory(screenInfo.getCategory());
		metricScreen.setEndPoints(StringUtils.join(screenInfo.getEndPoints(), ","));
		metricScreen.setMeasurements(StringUtils.join(screenInfo.getMeasures(), ","));
		metricScreen.setContent(screenInfo.getGraph().toString());

		return metricScreen;
	}

	public MetricScreenInfo transform2ScreenInfo(MetricScreen entity) {
		MetricScreenInfo metricScreenInfo = new MetricScreenInfo();
		List<String> endPoints = Splitters.by(",").noEmptyItem().split(entity.getEndPoints());
		List<String> measures = Splitters.by(",").noEmptyItem().split(entity.getMeasurements());

		metricScreenInfo.setName(entity.getName()).setCategory(entity.getCategory()).setGraphName(entity.getGraphName())
		      .setMeasures(measures).setEndPoints(endPoints);

		try {
			Graph graph = DefaultSaxParser.parse(entity.getContent());

			metricScreenInfo.setGraph(graph);
			return metricScreenInfo;
		} catch (Exception e) {
			Cat.logError(e);
			e.printStackTrace();
		}
		return null;
	}
}
