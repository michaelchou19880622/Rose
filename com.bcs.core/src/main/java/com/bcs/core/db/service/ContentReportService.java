package com.bcs.core.db.service;

import com.bcs.core.db.entity.PushReport;
import com.bcs.core.db.repository.ContentRichMsgDetailRepository;
import com.bcs.core.db.repository.PushReportRepository;
import com.bcs.core.utils.DBResultUtil;
import com.bcs.core.utils.DataUtils;
import com.bcs.core.utils.ErrorRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.Query;
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

@Slf4j
@Service
public class ContentReportService {
    private PushReportRepository pushReportRepository;
    private ContentRichMsgDetailRepository contentRichMsgDetailRepository;
    @Resource
    EntityManagerProviderService providerService;
    //將數字加入千分號

    private DecimalFormat df = new DecimalFormat(",###");

    @Autowired
    public ContentReportService(PushReportRepository pushReportRepository, ContentRichMsgDetailRepository contentRichMsgDetailRepository) {
        this.pushReportRepository = pushReportRepository;
        this.contentRichMsgDetailRepository = contentRichMsgDetailRepository;
    }

    /**
     * 取得發送報告所有清單
     */
    @SuppressWarnings("unchecked")
    public List<List<String>> getPushReportList(String startDate, String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        List<List<String>> resultList = new ArrayList<>();
        try {
            String queryString = "SELECT REPORT_RICH_ID, "
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
            //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
            c.add(Calendar.DATE, 1);
            //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
            c.add(Calendar.SECOND, -1);
            endDateObj = c.getTime();

            Query query = providerService.getEntityManager().createNativeQuery(queryString)
                    .setParameter(1, startDateObj)
                    .setParameter(2, endDateObj);

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
            String multiProductView = "";
            for (int i = 0; i < list.size(); i++) {
                Object[] objArray = list.get(i);
                List<String> dataList = new ArrayList<>();
                String dateString = objArray[1].toString();
                //時間只取得日期的部份
                dataList.add(dateString.substring(0, dateString.indexOf(' ')));
                //非richMsg
                if (objArray[0] == null || StringUtils.isBlank(objArray[0].toString())) {
                    for (int j = 2, max = objArray.length; j < max; j++) {
                        //REPORT_PUSH_TXT, REPORT_PUSH_URL, REPORT_PUSH_TRACKING_CODE
                        if (j >= 4 && j <= 6) {
                            this.addDataToListForTxt(dataList, objArray[j]);
                        } else {
                            this.addDataToList(dataList, objArray[j]);
                        }
                    }
                    dataList.add(12, "");
                } else {
                    if (StringUtils.isEmpty(multiTxt)) {
                        multiTxt = this.getStringFromObjectForTxt(objArray[4]);
                    }
                    if (StringUtils.isEmpty(multiUrl)) {
                        multiUrl = this.getStringFromObjectForTxt(objArray[5]);
                    }
                    if (StringUtils.isEmpty(multiTrackingCode)) {
                        multiTrackingCode = this.getStringFromObjectForTxt(objArray[6]);
                    }
                    if (StringUtils.isEmpty(multiClick)) {
                        multiClick = this.getStringFromObject(objArray[7]);
                    }
                    if (StringUtils.isEmpty(multiClickThrough)) {
                        multiClickThrough = this.getStringFromObject(objArray[8]);
                    }
                    if (StringUtils.isEmpty(multiVisit)) {
                        multiVisit = this.getStringFromObject(objArray[9]);
                    }
                    if (StringUtils.isEmpty(multiProductView)) {
                        multiProductView = this.getStringFromObject(objArray[10]);
                    }
                    if (StringUtils.isEmpty(multiReportId)) {
                        multiReportId = this.getStringFromObject(objArray[11]);
                    }
                    if ((i + 1) < list.size()) {
                        Object[] checkObjArray = list.get(i + 1);
                        if (objArray[13].equals(checkObjArray[13])) { //下一筆的reportMsgSendId與此筆相同
                            multiTxt += ";" + this.getStringFromObjectForTxt(checkObjArray[4]);
                            multiUrl += ";" + this.getStringFromObjectForTxt(checkObjArray[5]);
                            multiTrackingCode += ";" + this.getStringFromObjectForTxt(checkObjArray[6]);
                            multiClick += ";" + this.getStringFromObject(checkObjArray[7]);
                            multiClickThrough += ";" + this.getStringFromObject(checkObjArray[8]);
                            multiVisit += ";" + this.getStringFromObject(checkObjArray[9]);
                            multiProductView += ";" + this.getStringFromObject(checkObjArray[10]);
                            multiReportId += ";" + this.getStringFromObject(checkObjArray[11]);
                            continue;
                        }
                    }

                    for (int j = 2; j < 4; j++) {
                        this.addDataToList(dataList, objArray[j]);
                    }
                    //5~12多筆
                    dataList.add(multiTxt);
                    dataList.add(multiUrl);
                    dataList.add(multiTrackingCode);
                    dataList.add(multiClick);
                    dataList.add(multiClickThrough);
                    dataList.add(multiVisit);
                    dataList.add(multiProductView);
                    dataList.add(multiReportId);
                    this.addDataToList(dataList, objArray[12]); //imageId
                    this.addDataToListForTxt(dataList, objArray[0]); //richId
                    this.addDataToListForTxt(dataList, objArray[13]); //reportMsgSendId
                    this.addDataToListForTxt(dataList, objArray[14]); //REPORT_PUSH_TOTAL_CLICK
                    this.addDataToListForTxt(dataList, objArray[15]); //REPORT_PUSH_TOTAL_CLICK_THROUGH
                    this.addDataToListForTxt(dataList, objArray[16]); //REPORT_PUSH_TOTAL_DEEP
                    this.addDataToListForTxt(dataList, objArray[17]); //REPORT_PUSH_TOTAL_PRODUCT_VIEW
                    this.addDataToListForTxt(dataList, objArray[18]); //REPORT_PUSH_TOTAL_VISIT
                    this.addDataToListForTxt(dataList, objArray[19]); //REPORT_PUSH_CTR_VALUE

                    multiTxt = "";
                    multiUrl = "";
                    multiTrackingCode = "";
                    multiClick = "";
                    multiClickThrough = "";
                    multiVisit = "";
                    multiProductView = "";
                    multiReportId = "";
                }
                resultList.add(dataList);
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }

        log.debug(DataUtils.toPrettyJsonUseJackson(resultList));

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

    /**
     * 檢查Object的值並轉換相對應的值至list中
     */
    public void addDataToList(List<String> list, Object obj) {
        if (obj == null) {
            list.add(null);
        } else if (StringUtils.isNumeric(obj.toString())) {
            list.add(df.format(obj)); //將數字加入千分號
        } else {
            list.add(obj.toString());
        }
    }

    /**
     * 檢查Object的值並轉換相對應的值至list中
     */
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
    public PushReport getPushReportForEdit(Long reportId) {
        PushReport pushReport = pushReportRepository.findOne(reportId);
        log.debug(DataUtils.toPrettyJsonUseJackson(pushReport));
        return pushReport;
    }

    /**
     * 取得發送報告(Dialog用)
     */
    @SuppressWarnings("unchecked")
    public String[] getPushReportForDialog(Long reportId) {
        String queryString = "SELECT " +
                " REPORT_PUSH_TIME," +
                " REPORT_PUSH_TYPE," +
                " REPORT_PUSH_TXT," +
                " REPORT_PUSH_NUMBER," +
                " REPORT_PUSH_URL_CLICK," +
                " REPORT_PUSH_TOTAL_CLICK," +
                " REPORT_PUSH_CTR_VALUE," +
                " REPORT_PUSH_URL," +
                " REPORT_PUSH_TRACKING_CODE," +
                " REPORT_PUSH_URL_CLICK_THROUGH," +
                " REPORT_PUSH_TOTAL_CLICK_THROUGH," +
                " REPORT_PUSH_URL_VISIT," +
                " REPORT_PUSH_TOTAL_VISIT," +
                " REPORT_PUSH_URL_PRODUCT_VIEW," +
                " REPORT_PUSH_TOTAL_PRODUCT_VIEW," +
                " REPORT_PUSH_URL_DEEP," +
                " REPORT_PUSH_TOTAL_DEEP," +
                " REPORT_IMAGE_ID" +
                " FROM BCS_PUSH_REPORT" +
                " WHERE REPORT_ID=?1";
        Query query = providerService.getEntityManager().createNativeQuery(queryString)
                .setParameter(1, reportId);
        query.setHint("javax.persistence.query.timeout", 30000);
        List<Object[]> list = query.getResultList();

        Object[] object = list.get(0);
        String[] result = new String[object.length];

        String dateString = object[0].toString();
        dateString = dateString.substring(0, dateString.lastIndexOf('.'));
        //日期欄位
        result[0] = dateString;

        for (int i = 1; i < object.length; i++) {
            if (i == 2 || i == 7 || i == 8) {
                result[i] = this.getStringFromObjectForTxt(object[i]);
            } else {
                result[i] = this.getStringFromObject(object[i]);
            }
        }

        log.debug(DataUtils.toPrettyJsonUseJackson(result));

        return result;
    }

    /**
     * 取得發送報告(新增圖文訊息報告用)
     */
    public PushReport getPushReportForRichMsg(String richId) {
        PushReport pushReport = pushReportRepository.getOnlyOneByReportRichId(richId);

        if (pushReport != null) {
            pushReport.setReportPushUrl("");
            pushReport.setReportPushUrlClick(null);
            pushReport.setReportPushUrlClickThrough(null);
            pushReport.setReportPushUrlProductView(null);
            pushReport.setReportPushUrlVisit(null);
        }
        log.debug(DataUtils.toPrettyJsonUseJackson(pushReport));

        return pushReport;
    }

    /**
     * 新增與更新發送報告
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void savePushReport(PushReport pushReportData, String actionType, String userAccount) {
        try {
            log.info("savePushReport:" + pushReportData);
            List<PushReport> updateList = new ArrayList<>();
            PushReport pushReport;
            //變更
            if ("Edit".equals(actionType)) {
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
            } else {
                //新增
                pushReport = pushReportData;
                pushReport.setModifyTime(new Date());
                pushReport.setModifyUser(userAccount);
                updateList.add(pushReport);
            }

            log.info("ReportRichId:" + pushReport.getReportRichId());
            log.info("ReportMsgSendId:" + pushReport.getReportMsgSendId());
            if (StringUtils.isNotBlank(pushReport.getReportRichId())) {
                //屬於richMsg，還須更新其它連結的total資料
                Long reportId = pushReportData.getReportId();
                if (reportId == null) {
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
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 刪除發送報告
     */
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void deletePushReport(Long reportId) {
        try {
            pushReportRepository.delete(reportId);
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }

    /**
     * 取得發送報告所有清單(For Excel)
     */
    public List<PushReport> getReportList(String startDate, String endDate) {
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
            log.error(ErrorRecord.recordError(e));
        }

        log.debug(DataUtils.toPrettyJsonUseJackson(list));

        return list;
    }

    /**
     * 檢查Object的值並回傳相對應的字串值
     */
    public String getStringFromEntity(Object obj) {
        if (obj == null) {
            return "";
        }
        if (StringUtils.isNumeric(obj.toString())) {
            //將數字加入千分號
            return df.format(obj);
        }
        return obj.toString();

    }

    /**
     * 計算點擊率(週)
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

        if (totalPushNumber.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO.toString();
        }

        BigDecimal ctrValue = totalClick.divide(totalPushNumber, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")); //四捨五入至小數點第二位

        return ctrValue.toString();
    }

    /**
     * 取得點擊率(週)
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
            Query query = providerService.getEntityManager().createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
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
            query = providerService.getEntityManager().createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
            query.setHint("javax.persistence.query.timeout", 30000);
            List<Object[]> msgList = query.getResultList();

            String ctrValue = this.calculateClickRateForWeek(richMsgList, msgList);
            resultList.add(ctrValue); //本週

            //取得相差日數
            int days = (int) ((endDateObj.getTime() - startDateObj.getTime()) / (24 * 60 * 60 * 1000)) + 1;

            c.setTime(startDateObj);

            c.add(Calendar.DATE, -days); //上一週
            startDateObj = c.getTime();

            c.setTime(endDateObj);
            c.add(Calendar.DATE, -days);
            endDateObj = c.getTime();

            query = providerService.getEntityManager().createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
            query.setHint("javax.persistence.query.timeout", 30000);
            richMsgList = query.getResultList();

            query = providerService.getEntityManager().createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
            query.setHint("javax.persistence.query.timeout", 30000);
            msgList = query.getResultList();

            ctrValue = this.calculateClickRateForWeek(richMsgList, msgList);
            resultList.add(ctrValue); //上一週
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }

        return resultList;
    }

    /**
     * 計算點擊率(日)
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map<String, Map<String, BigDecimal>> calculateClickRateForDay(List<Object[]> richMsgList, List<Object[]> msgList) {
        Map<String, Map<String, BigDecimal>> map = new HashMap();

        for (Object[] data : richMsgList) {
            if (data[1] == null || data[2] == null) {
                continue;
            }

            Map<String, BigDecimal> dataMap = new HashMap();

            dataMap.put("totalClick", new BigDecimal(data[1].toString()));
            dataMap.put("totalPushNumber", new BigDecimal(data[2].toString()));
            dataMap.put("clickRate", BigDecimal.ZERO);
            map.put(data[0].toString(), dataMap);
        }

        for (Object[] data : msgList) {
            if (data[1] == null || data[2] == null) {
                continue;
            }

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
     */
    @SuppressWarnings({"unchecked"})
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
            Query query = providerService.getEntityManager().createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
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
            query = providerService.getEntityManager().createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
            query.setHint("javax.persistence.query.timeout", 30000);
            List<Object[]> msgList = query.getResultList();

            Map<String, Map<String, BigDecimal>> map = this.calculateClickRateForDay(richMsgList, msgList);

            resultList.add(map); //本週每日

            //取得相差日數
            int days = (int) ((endDateObj.getTime() - startDateObj.getTime()) / (24 * 60 * 60 * 1000)) + 1;

            c.setTime(startDateObj);
            c.add(Calendar.DATE, -days); //上一週
            startDateObj = c.getTime();

            c.setTime(endDateObj);
            c.add(Calendar.DATE, -days);
            endDateObj = c.getTime();

            query = providerService.getEntityManager().createNativeQuery(richMsgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
            query.setHint("javax.persistence.query.timeout", 30000);
            richMsgList = query.getResultList();

            query = providerService.getEntityManager().createNativeQuery(msgQueryString).setParameter(1, startDateObj).setParameter(2, endDateObj);
            query.setHint("javax.persistence.query.timeout", 30000);
            msgList = query.getResultList();

            map = this.calculateClickRateForDay(richMsgList, msgList);

            resultList.add(map); //本週每日
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }

        return resultList;
    }

    private static final String SYNC_FLAG = "SYNC_FLAG";

    /**
     * 計算某連結被使用者點擊的次數
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    public void countClickNumber(String linkId, Date clickTime) {
        log.info("countClickNumber:" + linkId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        try {
            if (linkId.length() != 36) {
                return;
            }

            synchronized (SYNC_FLAG) {
                List<String> richIds = contentRichMsgDetailRepository.getRichIdByLinkId(linkId);
                log.debug("richId:" + richIds);

                List<Object[]> list;
                //此linkId屬於richMsg
                if (CollectionUtils.isNotEmpty(richIds) && StringUtils.isNotBlank(richIds.get(0))) {
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
                    Query query = providerService.getEntityManager().createNativeQuery(queryString).setParameter(1, richIds.get(0));
                    query.setHint("javax.persistence.query.timeout", 30000);
                    list = query.getResultList();
                    log.debug("list:" + list);

                    if (CollectionUtils.isNotEmpty(list)) {

                        List<PushReport> updateList = new ArrayList<>();

                        Date lastTime = null;

                        for (Object[] objArray : list) {
                            String id = objArray[0].toString();
                            log.debug("LinkId:" + id);

                            Long reportMsgSendId = DBResultUtil.caseCountResult(objArray[8]).longValue();
                            log.debug("MSG_SEND_ID:" + reportMsgSendId);

                            String dateString = objArray[1].toString();
                            Date pushTime = DateUtils.parseDate(dateString.substring(0, dateString.lastIndexOf('.')), "yyyy-MM-dd HH:mm:ss");
                            pushTime = sdf.parse(sdf.format(pushTime));

                            if (clickTime.compareTo(pushTime) >= 0 && lastTime == null) {
                                lastTime = pushTime;
                            }

                            if (lastTime == null || lastTime.compareTo(pushTime) != 0) {
                                continue;
                            }

                            PushReport pushReport = pushReportRepository.findByReportMsgSendIdAndReportLinkId(reportMsgSendId, id);
                            if (pushReport == null) {
                                pushReport = new PushReport();
                                pushReport.setReportLinkId(id);
                                pushReport.setReportRichId(richIds.get(0));

                                pushReport.setReportPushTime(pushTime);
                                if (objArray[3] != null && StringUtils.isNotBlank(objArray[3].toString())) {
                                    pushReport.setReportPushType(objArray[3].toString());
                                }
                                if (objArray[4] != null && StringUtils.isNotBlank(objArray[4].toString())) {
                                    pushReport.setReportPushTxt(objArray[4].toString());
                                }
                                if (linkId.equals(objArray[0].toString())) {
                                    pushReport.setReportPushUrlClick(1L); //只有此次被點擊的連結會預設為1
                                } else {
                                    pushReport.setReportPushUrlClick(0L); //其它richMsg的連結預設為0
                                }
                                pushReport.setReportPushTotalClick(1L);
                                pushReport.setReportPushUrl(objArray[5].toString());

                                pushReport.setReportImageId(objArray[7].toString());
                            } else {
                                if (linkId.equals(id)) {
                                    //只有此次被點擊的連結次數會+1
                                    pushReport.setReportPushUrlClick(pushReport.getReportPushUrlClick() + 1);
                                }
                                //其它richMsg的連結只會total+1
                                pushReport.setReportPushTotalClick(pushReport.getReportPushTotalClick() + 1);
                            }

                            pushReport.setReportMsgSendId(reportMsgSendId);

                            Long totalClick = pushReport.getReportPushTotalClick();
                            long pushNumber = Long.parseLong(objArray[2].toString());
                            pushReport.setReportPushNumber(pushNumber);

                            // PushNumber can not be zero
                            if (BigDecimal.ZERO.compareTo(new BigDecimal(pushNumber)) == 0) {
                                pushNumber = 1L;
                            }
                            //四捨五入至小數點第二位
                            BigDecimal ctrValue = BigDecimal.valueOf(totalClick).divide(new BigDecimal(pushNumber), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                            pushReport.setReportPushCtrValue(ctrValue);

                            log.debug("pushReport:" + pushReport);
                            pushReport.setModifyTime(new Date());
                            pushReport.setModifyUser("System");
                            updateList.add(pushReport);
                        }
                        pushReportRepository.save(updateList);
                    }
                } else { //此linkId非richMsg
                    String queryString = "SELECT BCS_MSG_SEND_MAIN.SEND_TIME, "
                            + " BCS_MSG_SEND_MAIN.SEND_COUNT, "
                            + " BCS_MSG_SEND_MAIN.MSG_TAG, "
                            + " BCS_CONTENT_LINK.LINK_TITLE, "
                            + " BCS_CONTENT_LINK.LINK_URL, "
                            + " BCS_MSG_SEND_MAIN.MSG_SEND_ID "
                            + " FROM BCS_CONTENT_LINK "
                            + " LEFT JOIN BCS_MSG_DETAIL ON BCS_MSG_DETAIL.REFERENCE_ID = BCS_CONTENT_LINK.LINK_ID "
                            + " LEFT JOIN BCS_MSG_SEND_MAIN ON BCS_MSG_SEND_MAIN.MSG_SEND_ID = BCS_MSG_DETAIL.MSG_ID "
                            + " WHERE BCS_CONTENT_LINK.LINK_ID = ?1 "
                            + " AND MSG_TYPE = 'LINK' "
                            + " AND (MSG_PARENT_TYPE = 'BCS_MSG_SEND_MAIN' ) "
                            + " ORDER BY BCS_MSG_SEND_MAIN.SEND_TIME DESC";

                    Query query = providerService.getEntityManager().createNativeQuery(queryString).setParameter(1, linkId);
                    query.setHint("javax.persistence.query.timeout", 30000);
                    list = query.getResultList();

                    if (CollectionUtils.isNotEmpty(list)) {

                        Date lastTime = null;
                        for (Object[] objArray : list) {
                            log.debug("LinkId:" + linkId);
                            Long reportMsgSendId = DBResultUtil.caseCountResult(objArray[5]).longValue();
                            log.debug("MSG_SEND_ID:" + reportMsgSendId);

                            String dateString = objArray[0].toString();
                            Date pushTime = DateUtils.parseDate(dateString.substring(0, dateString.lastIndexOf('.')), "yyyy-MM-dd HH:mm:ss");
                            pushTime = sdf.parse(sdf.format(pushTime));

                            if (clickTime.compareTo(pushTime) >= 0 && lastTime == null) {
                                lastTime = pushTime;
                            }

                            if (lastTime == null || lastTime.compareTo(pushTime) != 0) {
                                continue;
                            }

                            PushReport pushReport = pushReportRepository.findByReportLinkId(linkId);
                            if (pushReport == null) {
                                pushReport = new PushReport();
                                pushReport.setReportLinkId(linkId);

                                pushReport.setReportPushTime(pushTime); //取得最早的發送時間

                                if (objArray[2] != null && StringUtils.isNotBlank(objArray[2].toString())) {
                                    pushReport.setReportPushType(objArray[2].toString());
                                }
                                pushReport.setReportPushUrlClick(1L);
                                pushReport.setReportPushTotalClick(1L);
                                pushReport.setReportPushUrl(objArray[4].toString());
                                if (objArray[3] != null && StringUtils.isNotBlank(objArray[3].toString())) {
                                    pushReport.setReportPushTxt(objArray[3].toString());
                                }
                            } else {
                                pushReport.setReportPushUrlClick(pushReport.getReportPushUrlClick() + 1);
                                pushReport.setReportPushTotalClick(pushReport.getReportPushTotalClick() + 1);
                            }

                            pushReport.setReportMsgSendId(reportMsgSendId);

                            Long totalClick = pushReport.getReportPushTotalClick();
                            long pushNumber = Long.parseLong(objArray[1].toString());
                            pushReport.setReportPushNumber(pushNumber);

                            // PushNumber can not be zero
                            if (BigDecimal.ZERO.compareTo(new BigDecimal(pushNumber)) == 0) {
                                pushNumber = 1L;
                            }

                            BigDecimal ctrValue = BigDecimal.valueOf(totalClick).divide(new BigDecimal(pushNumber), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));//四捨五入至小數點第二位
                            pushReport.setReportPushCtrValue(ctrValue);

                            log.debug("pushReport:" + pushReport);
                            pushReport.setModifyTime(new Date());
                            pushReport.setModifyUser("System");

                            pushReportRepository.save(pushReport);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }
    }
}
