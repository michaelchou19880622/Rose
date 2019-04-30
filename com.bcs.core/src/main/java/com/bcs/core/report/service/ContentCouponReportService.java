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

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.utils.DBResultUtil;
import com.bcs.core.utils.ObjectUtil;

@Service
public class ContentCouponReportService {
	@Autowired
	private RecordReportService recordReportService;
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentCouponReportService.class);

	/**
	 * Get Coupon Get Report
	 * Result : Key Time
	 * 					Value : 	Key dataType
	 *										Value Count
	 *
	 * @param startDate
	 * @param endDate
	 * @param couponId
	 * @return Map<String, Map<String, BigInteger>>
	 * @throws Exception
	 */
	public Map<String, Map<String, Long>> getCouponGetReport(String startDate, String endDate, String couponId) throws Exception {

		return this.getCouponReport(RECORD_REPORT_TYPE.CONTENT_TYPE_COUPON_GET, ActionUserCoupon.ACTION_TYPE_GET, startDate, endDate, couponId);
	}
	
	/**
	 * Get Coupon Use Report
	 * Result : Key Time
	 * 					Value : 	Key dataType
	 *										Value Count
	 *
	 * @param startDate
	 * @param endDate
	 * @param couponId
	 * @return Map<String, Map<String, BigInteger>>
	 * @throws Exception
	 */
	public Map<String, Map<String, Long>> getCouponUseReport(String startDate, String endDate, String couponId) throws Exception {

		return this.getCouponReport(RECORD_REPORT_TYPE.CONTENT_TYPE_COUPON_USE, ActionUserCoupon.ACTION_TYPE_USE, startDate, endDate, couponId);
	}
	
	private Map<String, Map<String, Long>> getCouponReport(RECORD_REPORT_TYPE reportType, String actionType, String startDate, String endDate, String couponId) throws Exception{

		// Validate
		if(couponId != null && StringUtils.isNotBlank(couponId.toString())){
			ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
			if(contentCoupon != null){
				// Pass
			}
			else{
				throw new Exception("CouponId Error");
			}
		}
		else{
			throw new Exception("CouponId Null");
		}
		
		if(couponId != null && StringUtils.isNotBlank(couponId.toString()) && StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
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

			// Query By Coupon
			String CONTENT_TYPE = reportType.toString();
			Map<String, Map<String, Long>> countCouponList = recordReportService
					.findRecordReportListByContentType(couponId.toString(),
							CONTENT_TYPE, startDate, endDate);
			
			Map<String, Map<String, Long>> result = new LinkedHashMap<String, Map<String, Long>>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);

					String startTimeStr = sdf.format(calendarStart.getTime());

					Map<String, Long> countMap = new HashMap<String, Long>();
					
					Map<String, Long> mapCoupon = countCouponList.get(startTimeStr);

					Long count = null; 
					Long distinctCount = null; 

					if(mapCoupon != null){
						count = mapCoupon.get(RECORD_REPORT_TYPE.DATA_TYPE_COUPON_COUNT.toString());
						distinctCount = mapCoupon.get(RECORD_REPORT_TYPE.DATA_TYPE_COUPON_DISTINCT_COUNT.toString());
					}
					if(count == null || distinctCount == null){
						logger.debug("calendarStart:" + calendarStart.getTime());
						logger.debug("calendarBreak:" + calendarBreak.getTime());
						List<Object[]> list = actionUserCouponService.countMidByCouponIdAndActionTypeAndTime(couponId, actionType, calendarStart.getTime(), calendarBreak.getTime());

						logger.debug("countMidByCouponIdAndActionTypeAndTime:" + ObjectUtil.objectToJsonStr(list));
						for(Object[] objArray : list){
							count = DBResultUtil.caseCountResult(objArray[0], false).longValue();
							distinctCount = DBResultUtil.caseCountResult(objArray[1], false).longValue();
						}
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										couponId.toString(),
										CONTENT_TYPE,
										RECORD_REPORT_TYPE.DATA_TYPE_COUPON_COUNT.toString(),
										count);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										couponId.toString(),
										CONTENT_TYPE,
										RECORD_REPORT_TYPE.DATA_TYPE_COUPON_DISTINCT_COUNT.toString(),
										distinctCount);
					}
					
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_COUPON_COUNT.toString(), count);
					countMap.put(RECORD_REPORT_TYPE.DATA_TYPE_COUPON_DISTINCT_COUNT.toString(), distinctCount);
					
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
