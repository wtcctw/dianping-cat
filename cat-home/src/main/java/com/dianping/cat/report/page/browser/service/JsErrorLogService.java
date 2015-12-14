package com.dianping.cat.report.page.browser.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.Level;
import com.dianping.cat.report.page.browser.display.JsErrorDetailInfo;
import com.dianping.cat.web.JsErrorLog;
import com.dianping.cat.web.JsErrorLogContent;
import com.dianping.cat.web.JsErrorLogContentDao;
import com.dianping.cat.web.JsErrorLogContentEntity;
import com.dianping.cat.web.JsErrorLogDao;
import com.dianping.cat.web.JsErrorLogEntity;

public class JsErrorLogService {

	@Inject
	private JsErrorLogContentDao m_jsErrorLogContentlDao;

	@Inject
	private JsErrorLogDao m_jsErrorLogDao;

	public JsErrorDetailInfo queryJsErrorInfo(int id) {
		JsErrorDetailInfo info = new JsErrorDetailInfo();

		try {
			JsErrorLog jsErrorLog = m_jsErrorLogDao.findByPK(id, JsErrorLogEntity.READSET_FULL);
			JsErrorLogContent detail = m_jsErrorLogContentlDao.findByPK(id, JsErrorLogContentEntity.READSET_FULL);

			info.setErrorTime(jsErrorLog.getErrorTime());
			info.setLevel(Level.getNameByCode(jsErrorLog.getLevel()));
			info.setModule(jsErrorLog.getModule());
			info.setDetail(new String(detail.getContent(), "UTF-8"));
			info.setAgent(jsErrorLog.getBrowser());
			info.setDpid(jsErrorLog.getDpid());
		} catch (Exception e) {
			Cat.logError(e);
		}

		return info;
	}
	
	public List<JsErrorLog> queryJsErrorInfo(JsErrorQueryEntity query, int offset, int limit) throws DalException {
		Date startTime = query.buildStartTime();
		Date endTime = query.buildEndTime();
		int levelCode = query.buildLevel();
		String module = query.getModule();
		String dpid = query.getDpid();
		
	   List<JsErrorLog> result = m_jsErrorLogDao.findDataByTimeModuleLevelBrowser(startTime, endTime, module,
	            levelCode, null, dpid, offset, limit, JsErrorLogEntity.READSET_FULL);
      
	   return result;
	}
}
