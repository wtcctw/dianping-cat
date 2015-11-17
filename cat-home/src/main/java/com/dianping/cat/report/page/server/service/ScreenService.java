package com.dianping.cat.report.page.server.service;

import java.util.LinkedList;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.MetricScreen;
import com.dianping.cat.home.dal.report.MetricScreenDao;
import com.dianping.cat.home.dal.report.MetricScreenEntity;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;

public class ScreenService {

	@Inject
	private MetricScreenDao m_dao;

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

}
