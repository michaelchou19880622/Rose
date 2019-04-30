package com.bcs.web.test;

import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.bcs.core.api.test.SpringJUnit4BaseTester;
import com.bcs.core.db.entity.RecordReport;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;

public class RecordReportTester extends SpringJUnit4BaseTester {

	@Autowired
	RecordReportService recordReportService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(RecordReportTester.class);

	@Test
	public void saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime() throws Exception {
		logger.info("saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime");
		
		String recordTimeStr = "2016-07-12";
		String referenceId = "TEST";
		String contentType = RECORD_REPORT_TYPE.CONTENT_TYPE_INTERACTIVE.toString();
		String dataType = RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_BINDED_COUNT.toString();
		RecordReport record = recordReportService.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(recordTimeStr, referenceId, contentType, dataType, 5L);
		logger.info(record);

		String recordTimeStr14 = "2016-07-14";
		String referenceId14 = "TEST";
		RecordReport record2 = recordReportService.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(recordTimeStr14, referenceId14, contentType, dataType, 6L);
		logger.info(record2);
	}

	@Test
	public void findRecordReportList() throws Exception {
		logger.info("findRecordReportList");

		String referenceId = "TEST";
		String contentType = RECORD_REPORT_TYPE.CONTENT_TYPE_INTERACTIVE.toString();
		String dataType = RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_BINDED_COUNT.toString();
		
		Map<String, Long> map = recordReportService.findRecordReportList(referenceId, contentType, dataType);
		logger.info(map);
	}

	@Test
	public void findRecordReportByRecordTime() throws Exception {
		logger.info("findRecordReportByRecordTime");

		String referenceId = "TEST";
		String contentType = RECORD_REPORT_TYPE.CONTENT_TYPE_INTERACTIVE.toString();
		String dataType = RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_BINDED_COUNT.toString();

		String recordTimeStr14 = "2016-07-14";
		
		RecordReport record = recordReportService.findRecordReportByRecordTime(referenceId, contentType, dataType, recordTimeStr14);
		logger.info(record);
	}

	@Test
	public void findRecordReportListByRecordTime() throws Exception {
		logger.info("findRecordReportListByRecordTime");

		String startTimeStr = "2016-07-01";
		String endTimeStr = "2016-07-16";

		String referenceId = "TEST";
		String contentType = RECORD_REPORT_TYPE.CONTENT_TYPE_INTERACTIVE.toString();
		String dataType = RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_BINDED_COUNT.toString();
		
		Map<String, Long> map = recordReportService.findRecordReportListByRecordTime(referenceId, contentType, dataType, startTimeStr, endTimeStr);
		logger.info(map);
	}
}
