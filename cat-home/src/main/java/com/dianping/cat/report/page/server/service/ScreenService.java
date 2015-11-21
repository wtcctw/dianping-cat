package com.dianping.cat.report.page.server.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.home.dal.report.MetricScreen;
import com.dianping.cat.home.dal.report.MetricScreenDao;
import com.dianping.cat.home.dal.report.MetricScreenEntity;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;

public class ScreenService implements Initializable {

	@Inject
	private MetricScreenDao m_dao;

	private Map<String, List<MetricScreen>> m_cachedScreens = new ConcurrentHashMap<String, List<MetricScreen>>();

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

	@Override
	public void initialize() throws InitializationException {
		refresh();
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

	public List<Graph> querByName(String name) {
		List<Graph> results = new LinkedList<Graph>();

		try {
			List<MetricScreen> entities = m_dao.findByName(name, MetricScreenEntity.READSET_FULL);

			for (MetricScreen entity : entities) {
				try {
					String xml = entity.getContent();
					Graph graph = DefaultSaxParser.parse(xml);

					results.add(graph);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return results;
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

	public Graph queryByNameGraph(String name, String graphName) {
		try {
			MetricScreen entity = m_dao.findByNameGraph(name, graphName, MetricScreenEntity.READSET_CONTENT);

			return buildGraph(entity);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			e.printStackTrace();
			Cat.logError(e);
		}
		return null;
	}

	public Map<String, List<MetricScreen>> queryScreens() {
		return m_cachedScreens;
	}

	public Map<String, List<String>> queryScreenGroups() {
		Map<String, List<String>> mapping = new LinkedHashMap<String, List<String>>();

		for (Entry<String, List<MetricScreen>> entry : m_cachedScreens.entrySet()) {
			List<String> list = new ArrayList<String>();

			for (MetricScreen screen : entry.getValue()) {
				list.add(screen.getName());
			}
			mapping.put(entry.getKey(), list);
		}

		SortHelper.sortMap(mapping, new Comparator<Map.Entry<String, List<String>>>() {

			@Override
			public int compare(Entry<String, List<String>> o1, Entry<String, List<String>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});

		return mapping;
	}

	private void refresh() {
		try {
			Map<String, List<MetricScreen>> cachedScreens = new ConcurrentHashMap<String, List<MetricScreen>>();
			List<MetricScreen> entities = m_dao.findAll(MetricScreenEntity.READSET_METAINFO);

			for (MetricScreen entity : entities) {
				String screenName = entity.getName();
				List<MetricScreen> screens = cachedScreens.get(screenName);

				if (screens == null) {
					screens = new LinkedList<MetricScreen>();

					cachedScreens.put(screenName, screens);
				}

				screens.add(entity);
			}

			m_cachedScreens = cachedScreens;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
	}
}
