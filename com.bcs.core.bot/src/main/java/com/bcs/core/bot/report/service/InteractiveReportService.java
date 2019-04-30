package com.bcs.core.bot.report.service;

import java.math.BigInteger;
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

import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.utils.DBResultUtil;

@Service
public class InteractiveReportService {
	@Autowired
	private RecordReportService recordReportService;
	@Autowired
	private MsgBotReceiveService msgBotReceiveService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(InteractiveReportService.class);

	public static final String COUNT_POSTFIX = "_Count"; 
	public static final String COUNT_DISTINCT_POSTFIX = "_DistinctCount"; 

	/**
	 * Get Interactive Report
	 * Result : Key Time
	 * 					Value : 	Key UserStatus
	 *										Value Count
	 * 
	 * @param startDate
	 * @param endDate
	 * @param iMsgId
	 * @param userStatus
	 * @return
	 * @throws Exception
	 */
	public Map<String, Map<String, BigInteger>> getInteractiveReport(String startDate, String endDate, Long iMsgId, String userStatus) throws Exception {

		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
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
			
			// Query CONTENT_TYPE_INTERACTIVE
			Map<String, Map<String, Long>> interactiveList = recordReportService
					.findRecordReportListByContentType(iMsgId.toString(),
							RECORD_REPORT_TYPE.CONTENT_TYPE_INTERACTIVE.toString(), startDate, endDate);
			
			Map<String, Map<String, BigInteger>> result = new LinkedHashMap<String, Map<String, BigInteger>>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);

					String startTimeStr = sdf.format(calendarStart.getTime());
					
					Map<String, Long> mapInteractive = interactiveList.get(startTimeStr);
					
					Map<String, BigInteger> countMap = new HashMap<String, BigInteger>();
					if (userStatus.equals("ALL")) {
						
						this.queryInteractiveRecord(countMap, mapInteractive, iMsgId, calendarStart, calendarBreak, LineUser.STATUS_UNBIND);

						this.queryInteractiveRecord(countMap, mapInteractive, iMsgId, calendarStart, calendarBreak, LineUser.STATUS_BINDED);
					}
					else if(userStatus.equals(LineUser.STATUS_BINDED) || userStatus.equals(LineUser.STATUS_UNBIND)){

						this.queryInteractiveRecord(countMap, mapInteractive, iMsgId, calendarStart, calendarBreak, userStatus);
					}
					else{
						throw new Exception("UserStatus Error");
					}
					
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
	 * Query Interactive Record
	 * 
	 * @param countMap
	 * @param mapInteractive
	 * @param iMsgId
	 * @param calendarStart
	 * @param calendarBreak
	 * @param userStatus
	 * @throws Exception
	 */
	private void queryInteractiveRecord(Map<String, BigInteger> countMap, 
			Map<String, Long> mapInteractive, 
			Long iMsgId, 
			Calendar calendarStart, 
			Calendar calendarBreak, 
			String userStatus) throws Exception{
		 
		String typeCount = null;
		String typeDistinct = null;
		if(LineUser.STATUS_UNBIND.equals(userStatus)){
			typeCount =  RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_UNBIND_COUNT.toString();
			typeDistinct = RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_UNBIND_DISTINCT_COUNT.toString();
		}
		else if(LineUser.STATUS_BINDED.equals(userStatus)){
			typeCount =  RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_BINDED_COUNT.toString();
			typeDistinct = RECORD_REPORT_TYPE.DATA_TYPE_INTERACTIVE_BINDED_DISTINCT_COUNT.toString();
		}
		else{
			throw new Exception("UserStatus Error");
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		String startTimeStr = sdf.format(calendarStart.getTime());
		
		BigInteger count = null; 
		BigInteger distinctCount = null; 
		if(mapInteractive != null){
			count = DBResultUtil.caseCountResult(mapInteractive.get(typeCount));
			distinctCount = DBResultUtil.caseCountResult(mapInteractive.get(typeDistinct));
		}
		if(count == null || distinctCount == null){

			Thread.sleep(100);
			
			List<Object[]> listUnbind = msgBotReceiveService.countReceiveByReferenceIdAndStatus(iMsgId.toString(), sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()), userStatus);

			for(Object[] objArray : listUnbind){
				count = DBResultUtil.caseCountResult(objArray[0]);
				distinctCount = DBResultUtil.caseCountResult(objArray[1]);
			}

			if(count == null){
				count = BigInteger.ZERO;
			}
			recordReportService
					.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
							startTimeStr,
							iMsgId.toString(),
							RECORD_REPORT_TYPE.CONTENT_TYPE_INTERACTIVE.toString(),
							typeCount, 
							count.longValue());
			
			if(distinctCount == null){
				distinctCount = BigInteger.ZERO;
			}
			recordReportService
					.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
							startTimeStr,
							iMsgId.toString(),
							RECORD_REPORT_TYPE.CONTENT_TYPE_INTERACTIVE.toString(),
							typeDistinct,
							distinctCount.longValue());
		}
		
		countMap.put(userStatus + COUNT_POSTFIX, count);
		countMap.put(userStatus + COUNT_DISTINCT_POSTFIX, distinctCount);
	}
}
