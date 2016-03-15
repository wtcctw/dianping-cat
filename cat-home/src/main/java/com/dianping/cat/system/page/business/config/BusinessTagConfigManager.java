package com.dianping.cat.system.page.business.config;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.config.BusinessConfig;
import com.dianping.cat.core.config.BusinessConfigDao;
import com.dianping.cat.core.config.BusinessConfigEntity;
import com.dianping.cat.home.business.entity.BusinessTagConfig;
import com.dianping.cat.home.business.entity.Tag;
import com.dianping.cat.home.business.transform.DefaultSaxParser;

public class BusinessTagConfigManager implements Initializable {

	@Inject
	private BusinessConfigDao m_configDao;

	public final static String TAG_CONFIG = "tag";

	private BusinessTagConfig m_tagConfig;

	public Set<String> findAllTags() {
		return m_tagConfig.getTags().keySet();
	}

	public Tag findTag(String id) {
		return m_tagConfig.findTag(id);
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			List<BusinessConfig> result = m_configDao.findByName(TAG_CONFIG, BusinessConfigEntity.READSET_FULL);

			if (result.size() > 0) {
				BusinessConfig config = result.get(0);
				m_tagConfig = DefaultSaxParser.parse(config.getContent());
			} else {
				m_tagConfig = new BusinessTagConfig();

				BusinessConfig config = m_configDao.createLocal();
				config.setName(TAG_CONFIG);
				config.setDomain(Constants.CAT);
				config.setContent(m_tagConfig.toString());
				config.setUpdatetime(new Date());

				m_configDao.insert(config);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

}
