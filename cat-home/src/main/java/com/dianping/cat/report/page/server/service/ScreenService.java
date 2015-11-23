package com.dianping.cat.report.page.server.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.MetricScreen;
import com.dianping.cat.home.dal.report.MetricScreenDao;
import com.dianping.cat.home.dal.report.MetricScreenEntity;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;
import com.dianping.cat.report.page.server.display.MetricScreenInfo;
import com.dianping.cat.report.page.server.display.MetricScreenTransformer;

public class ScreenService implements Initializable {

	@Inject
	private MetricScreenDao m_dao;

	@Inject
	private GraphBuilder m_graphBuilder;

	@Inject
	private MetricScreenTransformer m_transformer;

	private Map<String, Map<String, MetricScreenInfo>> m_cachedScreens = new ConcurrentHashMap<String, Map<String, MetricScreenInfo>>();

	private Graph buildGraph(MetricScreen screen) {
		try {
			String xml = screen.getContent();
			Graph graph = DefaultSaxParser.parse(xml);

			return graph;
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}
	}

	public void deleteByScreen(String screen) {
		MetricScreen metricScreen = new MetricScreen();

		metricScreen.setName(screen);

		try {
			m_dao.deleteByName(metricScreen);
			m_cachedScreens.remove(screen);
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	@Override
	public void initialize() throws InitializationException {
		refresh();
	}

	public void insert(String name, List<String> graphNames) {
		for (String graphName : graphNames) {
			Map<String, MetricScreenInfo> screens = m_cachedScreens.get(name);

			if (screens == null) {
				screens = new LinkedHashMap<String, MetricScreenInfo>();

				m_cachedScreens.put(name, screens);
			}
			MetricScreenInfo metricInfo = new MetricScreenInfo();

			metricInfo.setName(name).setGraphName(graphName);
			screens.put(graphName, metricInfo);
		}
	}

	public boolean insert(String name, String category, Graph graph) {
		boolean ret = true;
		MetricScreen screen = m_dao.createLocal();

		screen.setCategory(category);
		screen.setName(name);
		screen.setGraphName(graph.getId());
		screen.setContent(graph.toString());

		try {
			m_dao.insert(screen);
		} catch (DalException e) {
			ret = false;

			Cat.logError(e);
		}

		return ret;
	}

	public Graph queryById(int id) {
		try {
			MetricScreen entity = m_dao.findByPK(id, MetricScreenEntity.READSET_CONTENT);

			return buildGraph(entity);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	public Map<String, MetricScreenInfo> queryByName(String name) {
		Map<String, MetricScreenInfo> screeInfos = m_cachedScreens.get(name);

		if (screeInfos == null) {
			screeInfos = new HashMap<String, MetricScreenInfo>();
		}
		return screeInfos;
	}

	public MetricScreenInfo queryByNameGraph(String name, String graphName) {
		Map<String, MetricScreenInfo> screens = m_cachedScreens.get(name);

		if (screens != null) {
			MetricScreenInfo metricScreenInfo = screens.get(graphName);

			if (metricScreenInfo != null) {
				return metricScreenInfo;
			}
		}
		return queryByNameGraphFromDB(name, graphName);
	}

	public MetricScreenInfo queryByNameGraphFromDB(String name, String graphName) {
		try {
			MetricScreen entity = m_dao.findByNameGraph(name, graphName, MetricScreenEntity.READSET_CONTENT);

			return refreshByMetricScreen(name, entity);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	public Map<String, Map<String, MetricScreenInfo>> queryScreens() {
		return m_cachedScreens;
	}

	private void refresh() {
		try {
			Map<String, Map<String, MetricScreenInfo>> cachedScreens = new ConcurrentHashMap<String, Map<String, MetricScreenInfo>>();
			List<MetricScreen> entities = m_dao.findAll(MetricScreenEntity.READSET_FULL);

			for (MetricScreen entity : entities) {
				String screenName = entity.getName();
				Map<String, MetricScreenInfo> screens = cachedScreens.get(screenName);

				if (screens == null) {
					screens = new LinkedHashMap<String, MetricScreenInfo>();

					cachedScreens.put(screenName, screens);
				}

				screens.put(entity.getGraphName(), m_transformer.transform2ScreenInfo(entity));
			}

			m_cachedScreens = cachedScreens;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private MetricScreenInfo refreshByMetricScreen(String screen, MetricScreen entity) {
		MetricScreenInfo screenInfo = m_transformer.transform2ScreenInfo(entity);
		Map<String, MetricScreenInfo> map = m_cachedScreens.get(screen);

		if (map == null) {
			map = new LinkedHashMap<String, MetricScreenInfo>();

			m_cachedScreens.put(entity.getName(), map);
		}
		map.put(entity.getGraphName(), screenInfo);

		return screenInfo;
	}

	public void updateGraph(UpdateGraphParam param) {
		Map<String, MetricScreenInfo> screens = m_cachedScreens.get(param.getName());
		MetricScreenInfo screen = screens.get(param.getGraphName());

		if (screen != null) {
			screen.setName(param.getName());
			screen.setGraphName(param.getGraphName());
			screen.setEndPoints(param.getEndPoints());
			screen.setMeasures(param.getMeasurements());
			screen.setCategory(param.getCategory());
			screen.setGraph(m_graphBuilder.buildGraph(param.getEndPoints(), param.getMeasurements(), param.getName() + "-"
			      + param.getGraphName(), param.getView()));
		}

		MetricScreen metricScreen = m_transformer.transform2MetricScreen(screen);

		updateOrInsert(metricScreen);
	}

	private void updateOrInsert(MetricScreen metricScreen) {
		try {
			MetricScreen entity = m_dao.findByNameGraph(metricScreen.getName(), metricScreen.getGraphName(),
			      MetricScreenEntity.READSET_METAINFO);

			metricScreen.setId(entity.getId()).setKeyId(entity.getKeyId());
			m_dao.updateByPK(metricScreen, MetricScreenEntity.UPDATESET_FULL);
		} catch (DalNotFoundException e) {
			try {
				m_dao.insert(metricScreen);
			} catch (DalException e1) {
				Cat.logError(e);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

}
