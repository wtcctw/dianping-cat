package com.dianping.cat.config.business;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.business.entity.BusinessItemConfig;
import com.dianping.cat.configuration.business.entity.BusinessReportConfig;
import com.dianping.cat.configuration.business.transform.DefaultSaxParser;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.config.BusinessConfigDao;
import com.dianping.cat.core.config.BusinessConfigEntity;

public class BusinessConfigManager implements Initializable {

	@Inject
	private BusinessConfigDao m_configDao;

	private Map<String, Set<String>> m_domains = new ConcurrentHashMap<String, Set<String>>();

	public final static String BASE_CONFIG = "base";

	public boolean insertBusinessConfigIfNotExist(String domain, String key, ConfigItem item) {
		// TODO: 多线程可能造成的问题

		try {
			if (!m_domains.containsKey(domain)) {
				BusinessReportConfig config = new BusinessReportConfig();
				config.setId(domain);

				BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);
				config.addBusinessItemConfig(businessItemConfig);

				BusinessConfig businessConfig = m_configDao.createLocal();
				businessConfig.setName(BASE_CONFIG);
				businessConfig.setDomain(domain);
				businessConfig.setContent(config.toString());
				businessConfig.setUpdatetime(new Date());
				m_configDao.insert(businessConfig);

				Set<String> itemIds = new HashSet<String>();
				itemIds.add(key);
				m_domains.put(domain, itemIds);
			} else {
				Set<String> itemIds = m_domains.get(domain);

				if (!itemIds.contains(key)) {
					BusinessConfig businessConfig = m_configDao.findByNameDomain(BASE_CONFIG, domain,
					      BusinessConfigEntity.READSET_FULL);

					BusinessReportConfig config = DefaultSaxParser.parse(businessConfig.getContent());
					BusinessItemConfig businessItemConfig = buildBusinessItemConfig(key, item);
					config.addBusinessItemConfig(businessItemConfig);

					businessConfig.setContent(config.toString());
					m_configDao.updateByPK(businessConfig, BusinessConfigEntity.UPDATESET_FULL);

					itemIds.add(key);
				}
			}

			return true;
		} catch (Exception e) {
			Cat.logError(e);
		}
		return false;
	}

	public BusinessReportConfig queryConfigByDomain(String domain) {
		try {
			BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain, BusinessConfigEntity.READSET_FULL);

			return DefaultSaxParser.parse(config.getContent());
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}
	}

	public boolean deleteConfig(String domain, String key) {
		try {
			BusinessConfig config = m_configDao.findByNameDomain(BASE_CONFIG, domain, BusinessConfigEntity.READSET_FULL);
			BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());

			businessReportConfig.removeBusinessItemConfig(key);
			config.setContent(businessReportConfig.toString());

			m_configDao.updateByPK(config, BusinessConfigEntity.UPDATESET_FULL);

			Set<String> itemIds = m_domains.get(domain);
			itemIds.remove(key);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	public boolean updateConfigByDomain(BusinessReportConfig config) {
		BusinessConfig proto = m_configDao.createLocal();

		proto.setDomain(config.getId());
		proto.setName(BASE_CONFIG);
		proto.setContent(config.toString());

		try {
			m_configDao.updateBaseConfigByDomain(proto, BusinessConfigEntity.UPDATESET_FULL);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	private BusinessItemConfig buildBusinessItemConfig(String key, ConfigItem item) {
		BusinessItemConfig config = new BusinessItemConfig();

		config.setId(key);
		config.setTitle(item.getTitle());
		config.setShowAvg(item.isShowAvg());
		config.setShowCount(item.isShowCount());
		config.setShowSum(item.isShowSum());
		config.setViewOrder(item.getViewOrder());
		return config;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			List<BusinessConfig> configs = m_configDao.findByName(BASE_CONFIG, BusinessConfigEntity.READSET_FULL);

			for (BusinessConfig config : configs) {
				BusinessReportConfig businessReportConfig = DefaultSaxParser.parse(config.getContent());
				String domain = businessReportConfig.getId();
				Set<String> itemIds = new HashSet<String>(businessReportConfig.getBusinessItemConfigs().keySet());

				m_domains.put(domain, itemIds);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

}
