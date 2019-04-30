package com.bcs.core.db.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.PushReport;
import com.bcs.core.db.repository.ContentRichMsgDetailRepository;
import com.bcs.core.db.repository.PushReportRepository;
import com.bcs.core.utils.DBResultUtil;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ContentReportService {
	@Autowired
	private PushReportRepository pushReportRepository;
	@Autowired
	private ContentRichMsgDetailRepository contentRichMsgDetailRepository;
	@PersistenceContext
    EntityManager entityManager;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentReportService.class);
	
	private DecimalFormat df = new DecimalFormat(",###"); //將數字加入千分號
	
	/**
	 * 取得發送報告所有清單
     */
	@SuppressWarnings("unchecked")
	public List<List<String>> getPushReportList(String startDate, String endDate){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		List<List<String>> resultList = new ArrayList<>();
		try {
			String queryString = 
	    			"SELECT REPORT_RICH_ID, "
						+ "REPORT_PUSH_TIME, "
	    				+ "REPORT_PUSH_TYPE, "
	    				+ "REPORT_PUSH_NUMBER, "
						+ "REPORT_PUSH_TXT, "
						+ "REPORT_PUSH_URL, "
						+ "REPORT_PUSH_TRACKING_CODE, "
						+ "REPORT_PUSH_URL_CLICK, "
						+ "REPORT_PUSH_URL_CLICK_THROUGH, "
						+ "REPORT_PUSH_URL_VISIT, "
						+ "REPORT_PUSH_URL_PRODUCT_VIEW, "
						+ "REPORT_ID, "
						+ "REPORT_IMAGE_ID, "
						+ "REPORT_MSG_SEND_ID, "
						+ "REPORT_PUSH_TOTAL_CLICK, "
						+ "REPORT_PUSH_TOTAL_CLICK_THROUGH, "
						+ "REPORT_PUSH_TOTAL_DEEP, "
						+ "REPORT_PUSH_TOTAL_PRODUCT_VIEW, "
						+ "REPORT_PUSH_TOTAL_VISIT, "
						+ "REPORT_PUSH_CTR_VALUE "
	    			+ "FROM BCS_PUSH_REPORT "
	    			+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 "
	    			+ "ORDER BY REPORT_PUSH_TIME DESC, REPORT_RICH_ID";
	    	
			Date startDateObj = sdf.parse(startDate);
			Date endDateObj = sdf.parse(endDate);
			Calendar c = Calendar.getInstance();
			c.setTime(endDateObj);
			c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
			c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
			endDateObj = c.getTime();
			
	    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			List<Object[]> list = query.getResultList();
			
			//同一個richId的report會有同樣的pushTime
			String multiReportId = "";
			String multiTxt = "";
			String multiUrl = "";
			String multiTrackingCode = "";
			String multiClick = "";
			String multiClickThrough = "";
			String multiVisit = "";
			String multiProuctView = "";
			for (int i=0; i<list.size(); i++) {
				Object[] objs = list.get(i);
				List<String> dataList = new ArrayList<>();
				String dateString = objs[1].toString();
				dataList.add(dateString.substring(0, dateString.indexOf(" "))); //時間只取得日期的部份
				if (objs[0] == null || StringUtils.isBlank(objs[0].toString())) { //非richMsg
					for (int j=2, max=objs.length; j<max; j++) {
						if (j >=4 && j<=6) { //REPORT_PUSH_TXT, REPORT_PUSH_URL, REPORT_PUSH_TRACKING_CODE
							this.addDataToListForTxt(dataList, objs[j]);
						} else {
							this.addDataToList(dataList, objs[j]);
						}
					}
					dataList.add(12, "");
				} else {
					if (StringUtils.isEmpty(multiTxt)) multiTxt = this.getStringFromObjectForTxt(objs[4]);
					if (StringUtils.isEmpty(multiUrl)) multiUrl = this.getStringFromObjectForTxt(objs[5]);
					if (StringUtils.isEmpty(multiTrackingCode)) multiTrackingCode = this.getStringFromObjectForTxt(objs[6]);
					if (StringUtils.isEmpty(multiClick)) multiClick = this.getStringFromObject(objs[7]);
					if (StringUtils.isEmpty(multiClickThrough)) multiClickThrough = this.getStringFromObject(objs[8]);
					if (StringUtils.isEmpty(multiVisit)) multiVisit = this.getStringFromObject(objs[9]);
					if (StringUtils.isEmpty(multiProuctView)) multiProuctView = this.getStringFromObject(objs[10]);
					if (StringUtils.isEmpty(multiReportId)) multiReportId = this.getStringFromObject(objs[11]);
					if ((i + 1) < list.size()) {
						Object[] checkObjs = list.get(i+1);
						if (objs[13].equals(checkObjs[13])) { //下一筆的reportMsgSendId與此筆相同
							multiTxt = multiTxt + ";" + this.getStringFromObjectForTxt(checkObjs[4]);
							multiUrl = multiUrl + ";" + this.getStringFromObjectForTxt(checkObjs[5]);
							multiTrackingCode = multiTrackingCode + ";" + this.getStringFromObjectForTxt(checkObjs[6]);
							multiClick = multiClick + ";" + this.getStringFromObject(checkObjs[7]);
							multiClickThrough = multiClickThrough + ";" + this.getStringFromObject(checkObjs[8]);
							multiVisit = multiVisit + ";" + this.getStringFromObject(checkObjs[9]);
							multiProuctView = multiProuctView + ";" + this.getStringFromObject(checkObjs[10]);
							multiReportId = multiReportId + ";" + this.getStringFromObject(checkObjs[11]);
							continue;
						}
					}
					
					for (int j=2; j<4; j++) {
						this.addDataToList(dataList, objs[j]);
					}
					//5~12多筆
					dataList.add(multiTxt);
					dataList.add(multiUrl);
					dataList.add(multiTrackingCode);
					dataList.add(multiClick);
					dataList.add(multiClickThrough);
					dataList.add(multiVisit);
					dataList.add(multiProuctView);
					dataList.add(multiReportId);
					this.addDataToList(dataList, objs[12]); //imageId
					this.addDataToListForTxt(dataList, objs[0]); //richId
					this.addDataToListForTxt(dataList, objs[13]); //reportMsgSendId
					this.addDataToListForTxt(dataList, objs[14]); //REPORT_PUSH_TOTAL_CLICK
					this.addDataToListForTxt(dataList, objs[15]); //REPORT_PUSH_TOTAL_CLICK_THROUGH
					this.addDataToListForTxt(dataList, objs[16]); //REPORT_PUSH_TOTAL_DEEP
					this.addDataToListForTxt(dataList, objs[17]); //REPORT_PUSH_TOTAL_PRODUCT_VIEW
					this.addDataToListForTxt(dataList, objs[18]); //REPORT_PUSH_TOTAL_VISIT
					this.addDataToListForTxt(dataList, objs[19]); //REPORT_PUSH_CTR_VALUE
					
					multiTxt = "";
					multiUrl = "";
					multiTrackingCode = "";
					multiClick = "";
					multiClickThrough = "";
					multiVisit = "";
					multiProuctView = "";
					multiReportId = "";
				}
				resultList.add(dataList);
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
		logger.debug(resultList);
		
		return resultList;
	}
	
	//檢查Object的值並回傳相對應的字串值
	public String getStringFromObject(Object obj) {
		if (obj == null) {
			return "";
		} else if (StringUtils.isNumeric(obj.toString())) { 
			return df.format(obj); //將數字加入千分號
		} else {
			return obj.toString();
		}
	}
	
	public String getStringFromObjectForTxt(Object obj) {
		if (obj == null) {
			return "";
		} else {
			return obj.toString();
		}
	}
	
	//檢查Object的值並轉換相對應的值至list中
	public void addDataToList(List<String> list, Object obj) {
		if (obj == null) {
			list.add(null);
		} else if (StringUtils.isNumeric(obj.toString())) { 
			list.add(df.format(obj)); //將數字加入千分號
		} else {
			list.add(obj.toString());
		}
	}
	
	//檢查Object的值並轉換相對應的值至list中
	public void addDataToListForTxt(List<String> list, Object obj) {
		if (obj == null) {
			list.add(null);
		} else {
			list.add(obj.toString());
		}
	}
	
	/**
	 * 取得發送報告(Edit用)
     */
	public PushReport getPushReportForEdit(Long reportId){
		PushReport pushReport = pushReportRepository.findOne(reportId);
		
    	logger.debug(pushReport);
    	
		return pushReport;
	}
	
	/**
	 * 取得發送報告(Dialog用)
     */
	@SuppressWarnings("unchecked")
	public String[] getPushReportForDialog(Long reportId){
		String queryString = 
    			"SELECT REPORT_PUSH_TIME, "
    				+ "REPORT_PUSH_TYPE, "
					+ "REPORT_PUSH_TXT, "
					+ "REPORT_PUSH_NUMBER, "
					+ "REPORT_PUSH_URL_CLICK, "
					+ "REPORT_PUSH_TOTAL_CLICK, "
					+ "REPORT_PUSH_CTR_VALUE, "
					+ "REPORT_PUSH_URL, "
					+ "REPORT_PUSH_TRACKING_CODE, "
					+ "REPORT_PUSH_URL_CLICK_THROUGH, "
					+ "REPORT_PUSH_TOTAL_CLICK_THROUGH, "
					+ "REPORT_PUSH_URL_VISIT, "
					+ "REPORT_PUSH_TOTAL_VISIT, "
					+ "REPORT_PUSH_URL_PRODUCT_VIEW, "
					+ "REPORT_PUSH_TOTAL_PRODUCT_VIEW, "
					+ "REPORT_PUSH_URL_DEEP, "
					+ "REPORT_PUSH_TOTAL_DEEP, "
					+ "REPORT_IMAGE_ID "
    			+ "FROM BCS_PUSH_REPORT "
    			+ "WHERE REPORT_ID = ?1";
		Query query = entityManager.createNativeQuery(queryString).setParameter(1, reportId);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Object[] object = list.get(0);
		String[] result = new String[object.length];
		
		String dateString = object[0].toString();
		dateString = dateString.substring(0, dateString.lastIndexOf("."));
		result[0] = dateString; //日期欄位
		
		for (int i=1; i<object.length; i++) {
			if (i == 2 || i == 7 || i == 8) {
				result[i] = this.getStringFromObjectForTxt(object[i]);
			} else {
				result[i] = this.getStringFromObject(object[i]);
			}
		}
		
    	logger.debug(result);
    	
		return result;
	}
	
	/**
	 * 取得發送報告(新增圖文訊息報告用)
     */
	public PushReport getPushReportForRichMsg(String richId){
		PushReport pushReport = pushReportRepository.getOnlyOneByReportRichId(richId);
		
		if (pushReport != null) {
			pushReport.setReportPushUrl("");
			pushReport.setReportPushUrlClick(null);
			pushReport.setReportPushUrlClickThrough(null);
			pushReport.setReportPushUrlProductView(null);
			pushReport.setReportPushUrlVisit(null);
		}
    	logger.debug(pushReport);
    	
		return pushReport;
	}
    
    /**
	 * 新增與更新發送報告
     */
    @Transactional(rollbackFor=Exception.class, timeout = 30)
	public void savePushReport(PushReport pushReportData, String actionType, String userAccount){
    	try {
    		logger.info("savePushReport:" + pushReportData);
			List<PushReport> updateList = new ArrayList<PushReport>();
    		PushReport pushReport = new PushReport();
    		if (actionType.equals("Edit")) { //變更
    			pushReport = pushReportRepository.findOne(pushReportData.getReportId());
    			
    			pushReport.setReportPushTime(pushReportData.getReportPushTime());
    			pushReport.setReportPushType(pushReportData.getReportPushType());
    			pushReport.setReportImageId(pushReportData.getReportImageId());
    			pushReport.setReportPushNumber(pushReportData.getReportPushNumber());
    			pushReport.setReportPushCtrValue(pushReportData.getReportPushCtrValue());
    			pushReport.setReportPushTxt(pushReportData.getReportPushTxt());
    			pushReport.setReportPushUrl(pushReportData.getReportPushUrl());
    			pushReport.setReportPushTrackingCode(pushReportData.getReportPushTrackingCode());
    			pushReport.setReportPushUrlClick(pushReportData.getReportPushUrlClick());
    			pushReport.setReportPushUrlClickThrough(pushReportData.getReportPushUrlClickThrough());
    			pushReport.setReportPushUrlDeep(pushReportData.getReportPushUrlDeep());
    			pushReport.setReportPushUrlProductView(pushReportData.getReportPushUrlProductView());
    			pushReport.setReportPushUrlVisit(pushReportData.getReportPushUrlVisit());
    			pushReport.setReportPushTotalClick(pushReportData.getReportPushTotalClick());
    			pushReport.setReportPushTotalClickThrough(pushReportData.getReportPushTotalClickThrough());
    			pushReport.setReportPushTotalDeep(pushReportData.getReportPushTotalDeep());
    			pushReport.setReportPushTotalProductView(pushReportData.getReportPushTotalProductView());
    			pushReport.setReportPushTotalVisit(pushReportData.getReportPushTotalVisit());
    			
    			pushReport.setModifyTime(new Date());
    			pushReport.setModifyUser(userAccount);
    			
    			updateList.add(pushReport);
			} else { //新增
				pushReport = pushReportData;
				pushReport.setModifyTime(new Date());
				pushReport.setModifyUser(userAccount);
    			updateList.add(pushReport);
			}
			
    		logger.info("ReportRichId:" + pushReport.getReportRichId());
    		logger.info("ReportMsgSendId:" + pushReport.getReportMsgSendId());
			if (StringUtils.isNotBlank(pushReport.getReportRichId())) {
				//屬於richMsg，還須更新其它連結的total資料
				Long reportId = pushReportData.getReportId();
				if(reportId == null){
					reportId = 0L;
				}
				List<PushReport> list = pushReportRepository.findByReportMsgSendId(pushReport.getReportMsgSendId(), reportId);
				for (PushReport richMsgPushReport : list) {
					richMsgPushReport.setReportPushTotalClick(pushReportData.getReportPushTotalClick());
					richMsgPushReport.setReportPushCtrValue(pushReportData.getReportPushCtrValue());
					richMsgPushReport.setReportPushTotalClickThrough(pushReportData.getReportPushTotalClickThrough());
					richMsgPushReport.setReportPushTotalDeep(pushReportData.getReportPushTotalDeep());
					richMsgPushReport.setReportPushTotalProductView(pushReportData.getReportPushTotalProductView());
					richMsgPushReport.setReportPushTotalVisit(pushReportData.getReportPushTotalVisit());
					richMsgPushReport.setModifyTime(new Date());
					richMsgPushReport.setModifyUser(userAccount);
					updateList.add(richMsgPushReport);
				}
			}
			pushReportRepository.save(updateList);
    	} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
    	}
	}
    
	 /**
	 * 刪除發送報告
     */
    @Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deletePushReport(Long reportId){
    	try {
    		pushReportRepository.delete(reportId);
    	} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
    	}
    }
    
    /**
	 * 取得發送報告所有清單(For Excel)
     */
	public List<PushReport> getReportList(String startDate, String endDate){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		List<PushReport> list = new ArrayList<>();
		try {
			Date startDateObj = sdf.parse(startDate);
			Date endDateObj = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(endDateObj);
			calendarEnd.add(Calendar.DATE, 1);
			list = pushReportRepository.findByPushTimeOrderByPushTime(startDateObj, calendarEnd.getTime());
		} catch (ParseException e) {
			logger.error(ErrorRecord.recordError(e));
		}
		
    	logger.debug(list);
    	
		return list;
	}
    
	//檢查Object的值並回傳相對應的字串值
	public String getStringFromEntity(Object obj) {
		if (obj == null) {
			return "";
		} else if (StringUtils.isNumeric(obj.toString())) { 
			return df.format(obj); //將數字加入千分號
		} else {
			return obj.toString();
		}
	}
	
	/**
	 * 計算點擊率(週)
	 * @param richMsgList
	 * @param msgList
	 * @return String
	 */
	public String calculateClickRateForWeek(List<Object[]> richMsgList, List<Object[]> msgList) {
		//結果只會有一筆
		BigDecimal totalClick = BigDecimal.ZERO;
		BigDecimal totalPushNumber = BigDecimal.ZERO;
		
		//結果只會有一筆
		if (CollectionUtils.isNotEmpty(richMsgList) && richMsgList.get(0)[0] != null) {
			BigDecimal queryTotalClick = new BigDecimal(richMsgList.get(0)[0].toString());
			totalClick = totalClick.add(queryTotalClick);
			BigDecimal queryTotalPushNumber = new BigDecimal(richMsgList.get(0)[1].toString());
			totalPushNumber = totalPushNumber.add(queryTotalPushNumber);
		}
		
		//結果只會有一筆
		if (CollectionUtils.isNotEmpty(msgList) && msgList.get(0)[0] != null) {
			BigDecimal queryTotalClick = new BigDecimal(msgList.get(0)[0].toString());
			totalClick = totalClick.add(queryTotalClick);
			BigDecimal queryTotalPushNumber = new BigDecimal(msgList.get(0)[1].toString());
			totalPushNumber = totalPushNumber.add(queryTotalPushNumber);
		}
		
		if(totalPushNumber.equals(BigDecimal.ZERO)){
			return BigDecimal.ZERO.toString();
		}
		
		BigDecimal ctrValue = totalClick.divide(totalPushNumber, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")); //四捨五入至小數點第二位
		
		return ctrValue.toString();
	}
	
	/**
	 * 取得點擊率(週)
	 * @param startDate
	 * @param endDate
	 */
	@SuppressWarnings("unchecked")
	public List<String> getClickRateForWeek(String startDate, String endDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		List<String> resultList = new ArrayList<>();
		try {
			Date startDateObj = sdf.parse(startDate);
			Date endDateObj = sdf.parse(endDate);
			Calendar c = Calendar.getInstance();
			
			c.setTime(endDateObj);
			c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
			c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
			endDateObj = c.getTime();
			
			//richMsg
			String richMsgQueryString = 
	    			"SELECT SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
    					+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
	    			+ "FROM (SELECT DISTINCT REPORT_RICH_ID, REPORT_PUSH_TOTAL_CLICK, REPORT_PUSH_NUMBER "
	    					+ "FROM BCS_PUSH_REPORT "
	    					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 "
	    						+ "AND ISNULL(REPORT_RICH_ID, '') != '') AS richMsg ";
//			String richMsgQueryString = 
//	    			"SELECT SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
//    					+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
//	    			+ "FROM (SELECT DISTINCT REPORT_RICH_ID, REPORT_PUSH_TOTAL_CLICK, REPORT_PUSH_NUMBER "
//	    					+ "FROM BCS_PUSH_REPORT "
//	    					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 "
//	    						+ "AND IFNULL(REPORT_RICH_ID, '') != '') AS richMsg "; // MYSQL Difference
	    	Query query = entityManager.createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			List<Object[]> richMsgList = query.getResultList();
			
			//normalMsg
			String msgQueryString = 
					"SELECT SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
						+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
					+ "FROM BCS_PUSH_REPORT "
					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 "
						+ "AND ISNULL(REPORT_RICH_ID, '') = ''";
//			String msgQueryString = 
//					"SELECT SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
//						+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
//					+ "FROM BCS_PUSH_REPORT "
//					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 "
//						+ "AND IFNULL(REPORT_RICH_ID, '') = ''"; // MYSQL Difference
			query = entityManager.createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			List<Object[]> msgList = query.getResultList();
			
			String ctrValue = this.calculateClickRateForWeek(richMsgList, msgList);
			resultList.add(ctrValue); //本週
			
			//取得相差日數
			int days = (int)((endDateObj.getTime() - startDateObj.getTime()) / (24*60*60*1000)) + 1;
			
			c.setTime(startDateObj);
			
			c.add(Calendar.DATE, -days); //上一週
			startDateObj = c.getTime();
			
			c.setTime(endDateObj);
			c.add(Calendar.DATE, -days);
			endDateObj = c.getTime();
			
	    	query = entityManager.createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
	    	richMsgList = query.getResultList();
			
	    	query = entityManager.createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			msgList = query.getResultList();
			
			ctrValue = this.calculateClickRateForWeek(richMsgList, msgList);
			resultList.add(ctrValue); //上一週
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
		
		return resultList;
	}
	
	/**
	 * 計算點擊率(日)
	 * @param richMsgList
	 * @param msgList
	 * @return Map<String, Map<String, BigDecimal>>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<String, Map<String, BigDecimal>> calculateClickRateForDay(List<Object[]> richMsgList, List<Object[]> msgList) {
		Map<String, Map<String, BigDecimal>> map = new HashMap();
		
		for (Object[] data : richMsgList) {
			if (data[1] == null || data[2] == null) continue;
			
			Map<String, BigDecimal> dataMap = new HashMap();
			
			dataMap.put("totalClick", new BigDecimal(data[1].toString()));
			dataMap.put("totalPushNumber", new BigDecimal(data[2].toString()));
			dataMap.put("clickRate", BigDecimal.ZERO);
			map.put(data[0].toString(), dataMap);
		}
		
		for (Object[] data : msgList) {
			if (data[1] == null || data[2] == null) continue;
			
			Map<String, BigDecimal> dataMap = new HashMap();
			
			if (map.containsKey(data[0].toString())) {
				dataMap = map.get(data[0].toString());
				BigDecimal newTotalClick = dataMap.get("totalClick").add(new BigDecimal(data[1].toString()));
				dataMap.put("totalClick", newTotalClick);
				
				BigDecimal newTotalPushNumber = dataMap.get("totalPushNumber").add(new BigDecimal(data[2].toString()));
				dataMap.put("totalPushNumber", newTotalPushNumber);
			} else {
				dataMap.put("totalClick", new BigDecimal(data[1].toString()));
				dataMap.put("totalPushNumber", new BigDecimal(data[2].toString()));
				dataMap.put("clickRate", BigDecimal.ZERO);
				map.put(data[0].toString(), dataMap);
			}
		}
		
		Collection<Map<String, BigDecimal>> values = map.values();
		for (Map<String, BigDecimal> data : values) {
			//計算各個日期的點擊率
			BigDecimal ctr = data.get("totalClick").divide(data.get("totalPushNumber"), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")); //四捨五入至小數點第4位
			data.put("clickRate", ctr);
		}
		
		return map;
	}
	
	/**
	 * 取得點擊率(日)
	 * @param startDate
	 * @param endDate
	 */
	@SuppressWarnings({ "unchecked" })
	public List<Map<String, Map<String, BigDecimal>>> getClickRateForDay(String startDate, String endDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		List<Map<String, Map<String, BigDecimal>>> resultList = new ArrayList<>();
		try {
			Date startDateObj = sdf.parse(startDate);
			Date endDateObj = sdf.parse(endDate);
			Calendar c = Calendar.getInstance();
			
			c.setTime(endDateObj);
			c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
			c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
			endDateObj = c.getTime();
			
			//richMsg
			String richMsgQueryString = 
	    			"SELECT CONVERT(VARCHAR(10), REPORT_PUSH_TIME, 23) AS pushDate, "
    					+ "SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
    					+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
	    			+ "FROM (SELECT DISTINCT REPORT_RICH_ID, REPORT_PUSH_TIME, REPORT_PUSH_TOTAL_CLICK, REPORT_PUSH_NUMBER "
	    					+ "FROM BCS_PUSH_REPORT "
	    					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 AND ISNULL(REPORT_RICH_ID, '') != '') AS richMsg "
					+ "GROUP BY CONVERT(VARCHAR(10), REPORT_PUSH_TIME, 23) "
					+ "ORDER BY CONVERT(VARCHAR(10), REPORT_PUSH_TIME, 23) DESC ";
//			String richMsgQueryString = 
//	    			"SELECT DATE_FORMAT(REPORT_PUSH_TIME, '%Y-%m-%d') AS pushDate, "
//    					+ "SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
//    					+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
//	    			+ "FROM (SELECT DISTINCT REPORT_RICH_ID, REPORT_PUSH_TIME, REPORT_PUSH_TOTAL_CLICK, REPORT_PUSH_NUMBER "
//	    					+ "FROM BCS_PUSH_REPORT "
//	    					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 AND IFNULL(REPORT_RICH_ID, '') != '') AS richMsg "
//					+ "GROUP BY DATE_FORMAT(REPORT_PUSH_TIME, '%Y-%m-%d') "
//					+ "ORDER BY DATE_FORMAT(REPORT_PUSH_TIME, '%Y-%m-%d') DESC "; // MYSQL Difference
	    	Query query = entityManager.createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			List<Object[]> richMsgList = query.getResultList();
			
			//normalMsg
			String msgQueryString = 
					"SELECT CONVERT(VARCHAR(10), REPORT_PUSH_TIME, 23) AS pushDate, "
						+ "SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
						+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
					+ "FROM BCS_PUSH_REPORT "
					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 "
						+ "AND ISNULL(REPORT_RICH_ID, '') = '' "
					+ "GROUP BY CONVERT(VARCHAR(10), REPORT_PUSH_TIME, 23) "
					+ "ORDER BY CONVERT(VARCHAR(10), REPORT_PUSH_TIME, 23) DESC ";
//			String msgQueryString = 
//					"SELECT DATE_FORMAT(REPORT_PUSH_TIME, '%Y-%m-%d') AS pushDate, "
//						+ "SUM(REPORT_PUSH_TOTAL_CLICK) AS totalClick, "
//						+ "SUM(REPORT_PUSH_NUMBER) AS totalPushNumber "
//					+ "FROM BCS_PUSH_REPORT "
//					+ "WHERE REPORT_PUSH_TIME BETWEEN ?1 AND ?2 "
//						+ "AND IFNULL(REPORT_RICH_ID, '') = '' "
//					+ "GROUP BY DATE_FORMAT(REPORT_PUSH_TIME, '%Y-%m-%d') "
//					+ "ORDER BY DATE_FORMAT(REPORT_PUSH_TIME, '%Y-%m-%d') DESC "; // MYSQL Difference
			query = entityManager.createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			List<Object[]> msgList = query.getResultList();
			
			Map<String, Map<String, BigDecimal>> map = this.calculateClickRateForDay(richMsgList, msgList);
			
			resultList.add(map); //本週每日
			
			//取得相差日數
			int days = (int)((endDateObj.getTime() - startDateObj.getTime()) / (24*60*60*1000)) + 1;
			
			c.setTime(startDateObj);
			c.add(Calendar.DATE, -days); //上一週
			startDateObj = c.getTime();
			
			c.setTime(endDateObj);
			c.add(Calendar.DATE, -days);
			endDateObj = c.getTime();
			
			query = entityManager.createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			richMsgList = query.getResultList();
			
			query = entityManager.createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
			query.setHint("javax.persistence.query.timeout", 30000);
			msgList = query.getResultList();
			
			map = this.calculateClickRateForDay(richMsgList, msgList);
			
			resultList.add(map); //本週每日
		} catch (Exception e) {
    		logger.error(ErrorRecord.recordError(e));
		}
		
		return resultList;
	}

	private static final String SYNC_FLAG = "SYNC_FLAG";
	/**
	 * 計算某連結被使用者點擊的次數
	 * @param linkId
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void countClickNumber(String linkId, Date clickTime) {
		logger.info("countClickNumber:" + linkId);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		try {
			if(linkId.length() != 36){
				return;
			}
			
			synchronized (SYNC_FLAG) {
				List<String> richIds = contentRichMsgDetailRepository.getRichIdByLinkId(linkId);
				logger.debug("richId:" + richIds);
				
				List<Object[]> list = new ArrayList<Object[]>();
				//此linkId屬於richMsg
				if (richIds != null && richIds.size() > 0 && StringUtils.isNotBlank(richIds.get(0))) {
					String queryString = 
			    			"SELECT BCS_CONTENT_LINK.LINK_ID, "
		    					+ "BCS_MSG_SEND_MAIN.SEND_TIME, "
								+ "BCS_MSG_SEND_MAIN.SEND_COUNT, "
								+ "BCS_MSG_SEND_MAIN.MSG_TAG, "
								+ "BCS_CONTENT_LINK.LINK_TITLE, "
								+ "BCS_CONTENT_LINK.LINK_URL, "
								+ "BCS_CONTENT_RICH_DETAIL.RICH_DETAIL_LETTER, "
								+ "BCS_CONTENT_RICH.RICH_IMAGE_ID, "
								+ "BCS_MSG_SEND_MAIN.MSG_SEND_ID "
			    			+ "FROM BCS_CONTENT_LINK "
			    				+ "LEFT JOIN BCS_CONTENT_RICH_DETAIL ON BCS_CONTENT_RICH_DETAIL.LINK_ID = BCS_CONTENT_LINK.LINK_ID "
			    				+ "LEFT JOIN BCS_CONTENT_RICH ON BCS_CONTENT_RICH.RICH_ID = BCS_CONTENT_RICH_DETAIL.RICH_ID "
				    			+ "LEFT JOIN BCS_MSG_DETAIL ON BCS_MSG_DETAIL.REFERENCE_ID = BCS_CONTENT_RICH_DETAIL.RICH_ID "
				    			+ "LEFT JOIN BCS_MSG_SEND_MAIN ON BCS_MSG_SEND_MAIN.MSG_SEND_ID = BCS_MSG_DETAIL.MSG_ID "
			    			+ "WHERE BCS_CONTENT_RICH_DETAIL.RICH_ID = ?1 AND (BCS_CONTENT_RICH_DETAIL.STATUS <> 'DELETE' OR BCS_CONTENT_RICH_DETAIL.STATUS IS NULL)"
				    			+ "AND MSG_TYPE = 'RICH_MSG' "
				    			+ "AND (MSG_PARENT_TYPE = 'BCS_MSG_SEND_MAIN' ) "
			    			+ "ORDER BY BCS_MSG_SEND_MAIN.SEND_TIME DESC , BCS_CONTENT_RICH_DETAIL.RICH_DETAIL_LETTER";
			    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, richIds.get(0));
					query.setHint("javax.persistence.query.timeout", 30000);
					list = query.getResultList();
					logger.debug("list:" + list);
					
					if(list != null && list.size() > 0){
					
						List<PushReport> updateList = new ArrayList<>();
						
						Date lastTime = null;
						
						for (Object[] objs : list) {
							String LinkId = objs[0].toString();
							logger.debug("LinkId:" + LinkId);
							
							Long reportMsgSendId = DBResultUtil.caseCountResult(objs[8]).longValue();
							logger.debug("MSG_SEND_ID:" + reportMsgSendId);
							
							String dateString = objs[1].toString();
							Date pushTime = DateUtils.parseDate(dateString.substring(0, dateString.lastIndexOf(".")), "yyyy-MM-dd HH:mm:ss");
							pushTime = sdf.parse(sdf.format(pushTime));
							
							if(clickTime.compareTo(pushTime) >= 0 && lastTime == null){
								lastTime = pushTime;
							}
							
							if(lastTime == null || lastTime.compareTo(pushTime) != 0){
								continue;
							}
							
							PushReport pushReport = pushReportRepository.findByReportMsgSendIdAndReportLinkId(reportMsgSendId, LinkId);
							if (pushReport == null) {
								pushReport = new PushReport();
								pushReport.setReportLinkId(LinkId);
								pushReport.setReportRichId(richIds.get(0));
								
								pushReport.setReportPushTime(pushTime);
								if(objs[3] != null && StringUtils.isNotBlank(objs[3].toString())){
									pushReport.setReportPushType(objs[3].toString());
								}
								if(objs[4] != null && StringUtils.isNotBlank(objs[4].toString())){
									pushReport.setReportPushTxt(objs[4].toString());
								}
								if (linkId.equals(objs[0].toString())) {
									pushReport.setReportPushUrlClick(Long.valueOf(1)); //只有此次被點擊的連結會預設為1
								} else {
									pushReport.setReportPushUrlClick(Long.valueOf(0)); //其它richMsg的連結預設為0
								}
								pushReport.setReportPushTotalClick(Long.valueOf(1));
								pushReport.setReportPushUrl(objs[5].toString());
								
								pushReport.setReportImageId(objs[7].toString());
							} else {
								if (linkId.equals(LinkId)) {
									//只有此次被點擊的連結次數會+1
									pushReport.setReportPushUrlClick(pushReport.getReportPushUrlClick() + 1);
								}
								//其它richMsg的連結只會total+1
								pushReport.setReportPushTotalClick(pushReport.getReportPushTotalClick() + 1);
							}
							
							pushReport.setReportMsgSendId(reportMsgSendId);
							
							Long totalClick = pushReport.getReportPushTotalClick();
							Long pushNumber = Long.valueOf(objs[2].toString());
							pushReport.setReportPushNumber(pushNumber);
							
							// PushNumber can not be zero 
							if(BigDecimal.ZERO.compareTo(new BigDecimal(pushNumber)) == 0){
								pushNumber = 1L;
							}
							
							BigDecimal ctrValue = BigDecimal.valueOf(totalClick).divide(new BigDecimal(pushNumber), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));//四捨五入至小數點第二位
							pushReport.setReportPushCtrValue(ctrValue);
		
							logger.debug("pushReport:" + pushReport);
							pushReport.setModifyTime(new Date());
							pushReport.setModifyUser("System");
							updateList.add(pushReport);
						}
						pushReportRepository.save(updateList);
					}
				} else { //此linkId非richMsg
					String queryString = 
			    			"SELECT BCS_MSG_SEND_MAIN.SEND_TIME, "
								+ "BCS_MSG_SEND_MAIN.SEND_COUNT, "
								+ "BCS_MSG_SEND_MAIN.MSG_TAG, "
								+ "BCS_CONTENT_LINK.LINK_TITLE, "
								+ "BCS_CONTENT_LINK.LINK_URL, "
								+ "BCS_MSG_SEND_MAIN.MSG_SEND_ID "
			    			+ "FROM BCS_CONTENT_LINK "
			    				+ "LEFT JOIN BCS_MSG_DETAIL ON BCS_MSG_DETAIL.REFERENCE_ID = BCS_CONTENT_LINK.LINK_ID "
				    			+ "LEFT JOIN BCS_MSG_SEND_MAIN ON BCS_MSG_SEND_MAIN.MSG_SEND_ID = BCS_MSG_DETAIL.MSG_ID "
			    			+ "WHERE BCS_CONTENT_LINK.LINK_ID = ?1 "
				    			+ "AND MSG_TYPE = 'LINK' "
				    			+ "AND (MSG_PARENT_TYPE = 'BCS_MSG_SEND_MAIN' ) "
			    			+ "ORDER BY BCS_MSG_SEND_MAIN.SEND_TIME DESC";
			    	
					Query query = entityManager.createNativeQuery(queryString).setParameter(1, linkId);
					query.setHint("javax.persistence.query.timeout", 30000);
			    	list = query.getResultList();
			    	
			    	if(list != null && list.size() > 0){
						
						Date lastTime = null;
						for (Object[] objs : list) {
							logger.debug("LinkId:" + linkId);
							Long reportMsgSendId = DBResultUtil.caseCountResult(objs[5]).longValue();
							logger.debug("MSG_SEND_ID:" + reportMsgSendId);
							
							String dateString = objs[0].toString();
							Date pushTime = DateUtils.parseDate(dateString.substring(0, dateString.lastIndexOf(".")), "yyyy-MM-dd HH:mm:ss");
							pushTime = sdf.parse(sdf.format(pushTime));
							
							if(clickTime.compareTo(pushTime) >= 0 && lastTime == null){
								lastTime = pushTime;
							}
							
							if(lastTime == null || lastTime.compareTo(pushTime) != 0){
								continue;
							}

							PushReport pushReport = pushReportRepository.findByReportLinkId(linkId);
							if (pushReport == null) {
								pushReport = new PushReport();
								pushReport.setReportLinkId(linkId);
								
								pushReport.setReportPushTime(pushTime); //取得最早的發送時間

								if(objs[2] != null && StringUtils.isNotBlank(objs[2].toString())){
									pushReport.setReportPushType(objs[2].toString());
								}
								pushReport.setReportPushUrlClick(Long.valueOf(1));
								pushReport.setReportPushTotalClick(Long.valueOf(1));
								pushReport.setReportPushUrl(objs[4].toString());
								if(objs[3] != null && StringUtils.isNotBlank(objs[3].toString())){
									pushReport.setReportPushTxt(objs[3].toString());
								}
							} else {
								pushReport.setReportPushUrlClick(pushReport.getReportPushUrlClick() + 1);
								pushReport.setReportPushTotalClick(pushReport.getReportPushTotalClick() + 1);
							}
							
							pushReport.setReportMsgSendId(reportMsgSendId);
							
							Long totalClick = pushReport.getReportPushTotalClick();
							Long pushNumber = Long.valueOf(objs[1].toString());
							pushReport.setReportPushNumber(pushNumber);
							
							// PushNumber can not be zero 
							if(BigDecimal.ZERO.compareTo(new BigDecimal(pushNumber)) == 0){
								pushNumber = 1L;
							}
							
							BigDecimal ctrValue = BigDecimal.valueOf(totalClick).divide(new BigDecimal(pushNumber), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));//四捨五入至小數點第二位
							pushReport.setReportPushCtrValue(ctrValue);

							logger.debug("pushReport:" + pushReport);
							pushReport.setModifyTime(new Date());
							pushReport.setModifyUser("System");
							
							pushReportRepository.save(pushReport);
						}
			    	}
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}
}
