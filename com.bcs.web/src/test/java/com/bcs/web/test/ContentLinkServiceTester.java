package com.bcs.web.test;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.report.service.ContentLinkReportService;
import com.bcs.core.utils.ObjectUtil;

public class ContentLinkServiceTester extends SpringJUnit4BaseTester {

	@Autowired
	ContentLinkService contentLinkService;
	@Autowired
	ContentLinkReportService contentLinkReportService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentLinkServiceTester.class);

	@Test
	public void countClickCountByLinkUrlAndTime() throws Exception {
		logger.info("countClickCountByLinkUrlAndTime");
		
		String linkUrl = "BcsPage:ChangeShopPage";
		String start = "2016-06-01";
		String end = "2016-07-27";
		List<Object[]> list =contentLinkService.countClickCountByLinkUrlAndTime(linkUrl, start, end);
		
		logger.info(ObjectUtil.objectToJsonStr(list));
	}

	@Test
	public void countClickCountByLinkIdAndTime() throws Exception {
		logger.info("countClickCountByLinkIdAndTime");
		
		String linkId = "4f56ee1c-b556-4a1d-86eb-5d8c8cd804e4";
		String start = "2016-06-01";
		String end = "2016-07-27";
		List<Object[]> list =contentLinkService.countClickCountByLinkIdAndTime(linkId, start, end);
		
		logger.info(ObjectUtil.objectToJsonStr(list));
	}

	@Test
	public void getLinkUrlReport() throws Exception {
		logger.info("getLinkUrlReport");

		String linkUrl = "BcsPage:ChangeShopPage";
		String startDate = "2016-06-01";
		String endDate = "2016-07-18";
		Map<String, Map<String, Long>> map = contentLinkReportService.getLinkUrlReport(startDate, endDate, linkUrl);
		
		logger.info(ObjectUtil.objectToJsonStr(map));
	}

	@Test
	public void getLinkIdReport() throws Exception {
		logger.info("getLinkUrlReport");

		String linkId = "4f56ee1c-b556-4a1d-86eb-5d8c8cd804e4";
		String startDate = "2016-06-01";
		String endDate = "2016-07-18";
		Map<String, Map<String, Long>> map = contentLinkReportService.getLinkIdReport(startDate, endDate, linkId);
		
		logger.info(ObjectUtil.objectToJsonStr(map));
	}
}
