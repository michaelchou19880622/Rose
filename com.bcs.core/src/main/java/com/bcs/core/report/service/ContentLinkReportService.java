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

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.utils.DBResultUtil;

@Service
public class ContentLinkReportService {
	@Autowired
	private RecordReportService recordReportService;
	@Autowired
	private ContentLinkService contentLinkService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentLinkReportService.class);

	/**
	 * Get LinkUrl Report
	 * Result : Key Time
	 * 					Value : 	Key dataType
	 *										Value Count
	 *
	 * @param startDate
	 * @param endDate
	 * @param linkUrl
	 * @return Map<String, Map<String, BigInteger>>
	 * @throws Exception
	 */
	public Map<String, Map<String, Long>> getLinkUrlReport(String startDate, String endDate, String linkUrl) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date fisrtTime = new Date();
		
		// Validate
		if(StringUtils.isNotBlank(linkUrl)){
			List<ContentLink> links = contentLinkService.findByLinkUrl(linkUrl);
			if(links != null && links.size() > 0){
				// Pass
				for(ContentLink link : links){
					if(fisrtTime.getTime() > link.getModifyTime().getTime()){
						fisrtTime = link.getModifyTime();
					}
				}
				
				fisrtTime = sdf.parse(sdf.format(fisrtTime));
				logger.info("Link fisrtTime:" + sdf.format(fisrtTime.getTime()));
			}
			else{
				throw new Exception("Link URL Error");
			}
		}
		else{
			throw new Exception("Link URL Null");
		}
		
		if(StringUtils.isNotBlank(linkUrl) && StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
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

			// Query By linkUrl
			String CONTENT_TYPE_LINK_URL = RECORD_REPORT_TYPE.CONTENT_TYPE_LINK_URL.toString();
			Map<String, Map<String, Long>> countLinkUrlList = recordReportService
					.findRecordReportListByContentType(linkUrl,
							CONTENT_TYPE_LINK_URL, startDate, endDate);
			
			Map<String, Map<String, Long>> result = new LinkedHashMap<String, Map<String, Long>>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			
			Map<String, Long> clickMapCount = null;
			
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);

					String startTimeStr = sdf.format(calendarStart.getTime());
//					String breakTimeStr = sdf.format(calendarBreak.getTime());

					Map<String, Long> countMap = new HashMap<String, Long>();
					
					Map<String, Long> mapLinkUrl = countLinkUrlList.get(startTimeStr);

					Long count = null; 
					Long distinctCount = null; 

					if(mapLinkUrl != null){
						count = mapLinkUrl.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString());
						distinctCount = mapLinkUrl.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString());
					}
					if(count == null || distinctCount == null){
//						List<Object[]> list = contentLinkService.countClickCountByLinkUrlAndTime(linkUrl, startTimeStr, breakTimeStr);
//
//						for(Object[] objArray : list){
//							count = DBResultUtil.caseCountResult(objArray[0], false).longValue();
//							distinctCount = DBResultUtil.caseCountResult(objArray[1], false).longValue();
//						}
						
						if(calendarStart.getTime().getTime() >= fisrtTime.getTime()){

							if(clickMapCount == null){ 

								Thread.sleep(100);
								
								logger.info("countClickCountByLinkId queryTime:" + sdf.format(calendarStart.getTime()));
								List<Object[]> listCountDistinct = contentLinkService.countClickCountByLinkUrl(linkUrl, sdf.format(calendarStart.getTime()));
								clickMapCount = new HashMap<String, Long>();
								for(Object[] objArray : listCountDistinct){
									String timeDay = (String) objArray[0];
									clickMapCount.put(timeDay + "Count", DBResultUtil.caseCountResult(objArray[1], false).longValue()) ;
									clickMapCount.put(timeDay + "CountDistinct", DBResultUtil.caseCountResult(objArray[2], false).longValue()) ;
								}
							}
							
							count = clickMapCount.get(startTimeStr + "Count");
							distinctCount = clickMapCount.get(startTimeStr + "CountDistinct");
						}

						if(count == null){
							count = 0L;
						}
						if(distinctCount == null){
							distinctCount = 0L;
						}

						if(calendarStart.getTime().getTime() >= fisrtTime.getTime()){
							recordReportService
									.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
											startTimeStr,
											linkUrl,
											CONTENT_TYPE_LINK_URL,
											RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString(),
											count);
							
							recordReportService
									.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
											startTimeStr,
											linkUrl,
											CONTENT_TYPE_LINK_URL,
											RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString(),
											distinctCount);
						}
					}
					
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString(), count);
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString(), distinctCount);
					
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
	
	/**
	 * Get LinkId Report
	 * Result : Key Time
	 * 					Value : 	Key dataType
	 *										Value Count
	 *
	 * @param startDate
	 * @param endDate
	 * @param linkUrl
	 * @return Map<String, Map<String, BigInteger>>
	 * @throws Exception
	 */
	public Map<String, Map<String, Long>> getLinkIdReport(String startDate, String endDate, String linkId) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date fisrtTime = new Date();
		
		// Validate
		if(StringUtils.isNotBlank(linkId)){

			ContentLink link = contentLinkService.findOne(linkId);
			if(link != null){
				// Pass
				if(fisrtTime.getTime() > link.getModifyTime().getTime()){
					fisrtTime = link.getModifyTime();
				}
				
				fisrtTime = sdf.parse(sdf.format(fisrtTime));
				logger.info("Link fisrtTime:" + sdf.format(fisrtTime.getTime()));
			}
			else{
				throw new Exception("Link URL Error");
			}
		}
		else{
			throw new Exception("Link Id Null");
		}
		
		if(StringUtils.isNotBlank(linkId) && StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
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

			// Query By linkUrl
			String CONTENT_TYPE_LINK_ID = RECORD_REPORT_TYPE.CONTENT_TYPE_LINK_ID.toString();
			Map<String, Map<String, Long>> countLinkUrlList = recordReportService
					.findRecordReportListByContentType(linkId,
							CONTENT_TYPE_LINK_ID, startDate, endDate);
			
			Map<String, Map<String, Long>> result = new LinkedHashMap<String, Map<String, Long>>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			
			Map<String, Long> clickMapCount = null;
			
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);

					String startTimeStr = sdf.format(calendarStart.getTime());
//					String breakTimeStr = sdf.format(calendarBreak.getTime());

					Map<String, Long> countMap = new HashMap<String, Long>();
					
					Map<String, Long> mapLinkUrl = countLinkUrlList.get(startTimeStr);

					Long count = null; 
					Long distinctCount = null; 

					if(mapLinkUrl != null){
						count = mapLinkUrl.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString());
						distinctCount = mapLinkUrl.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString());
					}
					if(count == null || distinctCount == null){
//						List<Object[]> list = contentLinkService.countClickCountByLinkIdAndTime(linkId, startTimeStr, breakTimeStr);
//
//						for(Object[] objArray : list){
//							count = DBResultUtil.caseCountResult(objArray[0], false).longValue();
//							distinctCount = DBResultUtil.caseCountResult(objArray[1], false).longValue();
//						}

						if(calendarStart.getTime().getTime() >= fisrtTime.getTime()){

							if(clickMapCount == null){ 

								logger.info("countClickCountByLinkId queryTime:" + sdf.format(calendarStart.getTime()));
								List<Object[]> listCountDistinct = contentLinkService.countClickCountByLinkId(linkId, sdf.format(calendarStart.getTime()));
								clickMapCount = new HashMap<String, Long>();
								for(Object[] objArray : listCountDistinct){
									String timeDay = (String) objArray[0];
									clickMapCount.put(timeDay + "Count", DBResultUtil.caseCountResult(objArray[1], false).longValue()) ;
									clickMapCount.put(timeDay + "CountDistinct", DBResultUtil.caseCountResult(objArray[2], false).longValue()) ;
								}
							}
							
							count = clickMapCount.get(startTimeStr + "Count");
							distinctCount = clickMapCount.get(startTimeStr + "CountDistinct");
						}

						if(count == null){
							count = 0L;
						}
						if(distinctCount == null){
							distinctCount = 0L;
						}

						if(calendarStart.getTime().getTime() >= fisrtTime.getTime()){
							recordReportService
									.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
											startTimeStr,
											linkId,
											CONTENT_TYPE_LINK_ID,
											RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString(),
											count);
							
							recordReportService
									.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
											startTimeStr,
											linkId,
											CONTENT_TYPE_LINK_ID,
											RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString(),
											distinctCount);
						}
					}
					
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString(), count);
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString(), distinctCount);
					
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
