package com.bcs.web.test;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.bot.report.export.ExportToExcelForContentPushReport;
import com.bcs.core.bot.report.export.ExportToExcelForKeywordReport;
import com.bcs.core.bot.report.export.ExportToExcelUserGroup;
import com.bcs.core.bot.report.service.InteractiveReportService;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.resource.CoreConfigReader;

public class ExportReportToExcelTester extends SpringJUnit4BaseTester {
	
	@Autowired
	private InteractiveReportService interactiveReportService;
	@Autowired
	private ExportToExcelForContentPushReport exportToExcelForContentPushReport;
	@Autowired
	private ExportToExcelForKeywordReport exportToExcelForKeywordReport;
	@Autowired
	private ExportToExcelUserGroup exportToExcelUserGroup;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ExportReportToExcelTester.class);

//	@Test
	public void exportReportToExcelUserGroup() throws Exception {
		logger.info("exportReportToExcelUserGroup");
		
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		
		Date date = new Date();
		String fileName = "PushReportList-" + date.getTime() + ".xlsx";
		
		String startDate = "2016-06-27";
		String endDate = "2016-07-12";
		
		exportToExcelUserGroup.exportToExcelUserGroup(filePath, startDate, endDate, fileName);
	}

//	@Test
	public void exportToExcel() throws Exception {
		logger.info("exportReportToExcelUserGroup");
		
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		
		Date date = new Date();
		String fileName = "PushReportList-" + date.getTime() + ".xlsx";
		
		String startDate = "2016-06-27";
		String endDate = "2016-07-12";
		
		exportToExcelForContentPushReport.exportToExcel(filePath, startDate, endDate, fileName);
	}

//	@Test
	public void getInteractiveReport() throws Exception{
		String startDate = "2016-05-01";
		String endDate = "2016-07-12";
		
//		Map<String, Map<String, BigInteger>> result = interactiveReportService.getInteractiveReport(startDate, endDate, 12L, "ALL");

		Map<String, Map<String, BigInteger>> result = interactiveReportService.getInteractiveReport(startDate, endDate, 12L, LineUser.STATUS_BINDED);
		
		logger.info(result);
	}

	@Test
	public void exportToExcelForKeywordReport() throws Exception {
		logger.info("exportReportToExcelUserGroup");
		
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		
		Date date = new Date();
		String fileName = "PushReportList-" + date.getTime() + ".xlsx";

		String startDate = "2016-05-01";
		String endDate = "2016-07-12";
		
//		exportToExcelForKeywordReport.exportToExcelForKeywordReport(filePath, fileName, startDate, endDate, 12L, LineUser.STATUS_BINDED);
//		exportToExcelForKeywordReport.exportToExcelForKeywordReport(filePath, fileName, startDate, endDate, 12L, LineUser.STATUS_UNBIND);
		exportToExcelForKeywordReport.exportToExcelForKeywordReport(filePath, fileName, startDate, endDate, 12L, "ALL");
	}
}
