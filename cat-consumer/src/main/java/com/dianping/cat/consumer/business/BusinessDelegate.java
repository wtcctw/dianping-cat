package com.dianping.cat.consumer.business;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.business.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.business.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;

public class BusinessDelegate implements ReportDelegate<BusinessReport> {

	@Override
	public void afterLoad(Map<String, BusinessReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, BusinessReport> reports) {
	}

	@Override
	public byte[] buildBinary(BusinessReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public BusinessReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public String buildXml(BusinessReport report) {
		return report.toString();
	}

	@Override
	public String getDomain(BusinessReport report) {
		return report.getDomain();
	}

	@Override
	public BusinessReport makeReport(String domain, long startTime, long duration) {
		BusinessReport report = new BusinessReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public BusinessReport mergeReport(BusinessReport old, BusinessReport other) {
		return null;
	}

	@Override
	public BusinessReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}

	@Override
	public boolean createHourlyTask(BusinessReport report) {
		return true;
	}

}
