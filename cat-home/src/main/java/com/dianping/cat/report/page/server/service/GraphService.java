package com.dianping.cat.report.page.server.service;

import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.report.MetricGraph;
import com.dianping.cat.home.dal.report.MetricGraphDao;
import com.dianping.cat.home.dal.report.MetricGraphEntity;
import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;

public class GraphService {

	@Inject
	private MetricGraphDao m_dao;

	public boolean deleteBeforeDate(Date date) {

		return true;
	}

	public boolean deleteById(int id) {
		boolean ret = true;
		MetricGraph entity = m_dao.createLocal();

		entity.setId(id);

		try {
			m_dao.deleteByPK(entity);
		} catch (DalException e) {
			ret = false;

			Cat.logError(e);
		}
		return ret;
	}

	public boolean insert(Graph graph) {
		boolean ret = true;
		MetricGraph entity = m_dao.createLocal();

		entity.setName(graph.getId());
		entity.setContent(graph.toString());

		try {
			m_dao.insert(entity);
		} catch (DalException e) {
			ret = false;

			Cat.logError(e);
		}

		return ret;
	}

	public Graph queryById(int id) {
		try {
			MetricGraph entity = m_dao.findByPK(id, MetricGraphEntity.READSET_FULL);
			String xml = entity.getContent();
			Graph graph = DefaultSaxParser.parse(xml);

			return graph;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}

	public Graph queryByName(String name) {
		try {
			MetricGraph entity = m_dao.findByName(name, MetricGraphEntity.READSET_FULL);
			String xml = entity.getContent();
			Graph graph = DefaultSaxParser.parse(xml);

			return graph;
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}

}
