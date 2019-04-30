package com.bcs.web.test;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.service.ContentReportService;

public class ContentReportServiceTester extends SpringJUnit4BaseTester {

	@Autowired
	ContentReportService contentReportService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentReportServiceTester.class);

	@Test
	public void countClickNumber() throws Exception {
		logger.info("countClickNumber");
		
		String linkId = "cbea8395-0160-4a88-9040-2591d9203f45";
		
		Date clickTime = DateUtils.parseDate("2016-07-23 14:55:49", "yyyy-MM-dd HH:mm:ss");
		
		contentReportService.countClickNumber(linkId, clickTime);
		contentReportService.countClickNumber(linkId, clickTime);
		contentReportService.countClickNumber(linkId, clickTime);
		contentReportService.countClickNumber(linkId, clickTime);
		contentReportService.countClickNumber(linkId, clickTime);
		
		Date clickTimeb = DateUtils.parseDate("2016-07-29 14:55:49", "yyyy-MM-dd HH:mm:ss");
		
		contentReportService.countClickNumber(linkId, clickTimeb);
		contentReportService.countClickNumber(linkId, clickTimeb);
		contentReportService.countClickNumber(linkId, clickTimeb);
	}
}
