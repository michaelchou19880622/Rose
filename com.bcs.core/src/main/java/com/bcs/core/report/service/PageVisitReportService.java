package com.bcs.core.report.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.utils.DBResultUtil;

@Service
public class PageVisitReportService {
	@Autowired
	private RecordReportService recordReportService;
	@Autowired
	private UserTraceLogService userTraceLogService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(PageVisitReportService.class);

	/**
	 * Get PageUrl Report
	 * Result : Key Time
	 * 					Value : 	Key dataType
	 *										Value Count
	 *
	 * @param startDate
	 * @param endDate
	 * @param pageUrl
	 * @return Map<String, Map<String, BigInteger>>
	 * @throws Exception
	 */
	public Map<String, Map<String, Long>> getPageVisitReport(String startDate, String endDate, String pageUrl) throws Exception {

		// Validate
		if(StringUtils.isNotBlank(pageUrl)){
			// Pass
		}
		else{
			throw new Exception("Page URL Null");
		}
		
		if(StringUtils.isNotBlank(pageUrl) && StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date timeStart = sdf.parse(startDate);
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(timeStart);
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);

			endDate = sdf.format(calendarEnd.getTime());
			logger.debug("startDate:" + startDate);
			logger.debug("endDate:" + endDate);

			// Query By pageUrl
			String CONTENT_TYPE_PAGE_URL = RECORD_REPORT_TYPE.CONTENT_TYPE_PAGE_URL.toString();
			Map<String, Map<String, Long>> countPageUrlList = recordReportService
					.findRecordReportListByContentType(pageUrl,
							CONTENT_TYPE_PAGE_URL, startDate, endDate);
			
			Map<String, Map<String, Long>> result = new LinkedHashMap<String, Map<String, Long>>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);

					String startTimeStr = sdf.format(calendarStart.getTime());
					String breakTimeStr = sdf.format(calendarBreak.getTime());

					Map<String, Long> countMap = new HashMap<String, Long>();
					
					Map<String, Long> mapLinkUrl = countPageUrlList.get(startTimeStr);

					Long count = null; 
					Long distinctCount = null; 

					if(mapLinkUrl != null){
						count = mapLinkUrl.get(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_COUNT.toString());
						distinctCount = mapLinkUrl.get(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_DISTINCT_COUNT.toString());
					}
					if(count == null || distinctCount == null){
						
						Thread.sleep(100);
						
						List<Object[]> list = userTraceLogService.countByReferenceIdAndTime(LOG_TARGET_ACTION_TYPE.TARGET_MobilePage.toString(), 
								LOG_TARGET_ACTION_TYPE.ACTION_VisitPage.toString(), 
								pageUrl, 
								startTimeStr, 
								breakTimeStr);

						for(Object[] objArray : list){
							count = DBResultUtil.caseCountResult(objArray[0], false).longValue();
							distinctCount = DBResultUtil.caseCountResult(objArray[1], false).longValue();
						}
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										pageUrl,
										CONTENT_TYPE_PAGE_URL,
										RECORD_REPORT_TYPE.DATA_TYPE_PAGE_COUNT.toString(),
										count);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										pageUrl,
										CONTENT_TYPE_PAGE_URL,
										RECORD_REPORT_TYPE.DATA_TYPE_PAGE_DISTINCT_COUNT.toString(),
										distinctCount);
					}
					
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_COUNT.toString(), count);
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_DISTINCT_COUNT.toString(), distinctCount);
					
					result.put(sdf.format(calendarStart.getTime()), countMap);

					calendarStart.add(Calendar.DATE, 1);
				}
				else{
				
					break;
				}
			}
			
			return result;
		}
		
		return null;
	}
}
