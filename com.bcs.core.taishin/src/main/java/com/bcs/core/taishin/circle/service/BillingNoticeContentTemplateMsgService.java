package com.bcs.core.taishin.circle.service;

import com.bcs.core.db.service.EntityManagerProviderService;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentLink;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgActionRepository;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentTemplateMsgRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ???
 */
@Slf4j(topic = "BNRecorder")
@Service
public class BillingNoticeContentTemplateMsgService {
    public static final String TEMPLATE_SYNC = "TEMPLATE_SYNC";
    @Autowired
    private BillingNoticeContentTemplateMsgRepository contentTemplateMsgRepository;
    @Autowired
    private BillingNoticeContentTemplateMsgActionRepository contentTemplateMsgActionRepository;
    @Autowired
    private BillingNoticeContentLinkService contentLinkService;
    @Resource
    EntityManagerProviderService providerService;

    protected LoadingCache<String, Map<String, List<String>>> dataCache;

    private ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder()
                    .namingPattern("BN-ReSync-Scheduled-%d")
                    .daemon(true).build()
    );


    public BillingNoticeContentTemplateMsgService() {
        scheduler.scheduleWithFixedDelay(this::process, 120, 30, TimeUnit.SECONDS);
        dataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, List<String>>>() {
                    @Override
                    public Map<String, List<String>> load(String key) {
                        return new HashMap<>();
                    }
                });
    }

    private void process() {
        try {
            // Check Data Sync
            boolean isReSyncData = DataSyncUtil.isReSyncData(TEMPLATE_SYNC);
            if (isReSyncData) {
                dataCache.invalidateAll();
                DataSyncUtil.syncDataFinish(TEMPLATE_SYNC);
            }
        } catch (Exception e) {
            log.error(ErrorRecord.recordError(e));
        }

    }

    public void getPreTemplateIds(String templateId, String account) {
        try {
            List<BillingNoticeContentTemplateMsg> contentTemplateMsgs = contentTemplateMsgRepository.findByParentTemplateId(templateId);
            for (BillingNoticeContentTemplateMsg contentTemplateMsg : contentTemplateMsgs) {
                contentTemplateMsg.setStatus(BillingNoticeContentTemplateMsg.STATUS_DELETE);
                contentTemplateMsg.setModifyUser(account);
                contentTemplateMsg.setModifyTime(new Date());
                contentTemplateMsgRepository.save(contentTemplateMsg);
            }
            contentTemplateMsgs = contentTemplateMsgRepository.findByTemplateId(templateId);
            for (BillingNoticeContentTemplateMsg contentTemplateMsg : contentTemplateMsgs) {
                contentTemplateMsg.setStatus(BillingNoticeContentTemplateMsg.STATUS_DELETE);
                contentTemplateMsg.setModifyUser(account);
                contentTemplateMsg.setModifyTime(new Date());
                contentTemplateMsgRepository.save(contentTemplateMsg);
            }

        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    public void setPreTitleTemplateToOff(String templateTitle, String account) {
        try {
            List<BillingNoticeContentTemplateMsg> contentTemplateMsgs = contentTemplateMsgRepository.findByTemplateTitle(templateTitle);
            for (BillingNoticeContentTemplateMsg contentTemplateMsg : contentTemplateMsgs) {
                log.info("ToOff:" + contentTemplateMsg.getTemplateId());
                contentTemplateMsg.setProductSwitch(false);
                contentTemplateMsg.setModifyUser(account);
                contentTemplateMsg.setModifyTime(new Date());
                contentTemplateMsgRepository.save(contentTemplateMsg);
            }

        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    /**
     * 取得樣板訊息更新前的actionId與LinkId
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, String>> getPreActionIdAndLinkId(String templateId) {
        List<Map<String, String>> list = new ArrayList<>();
        List<BillingNoticeContentTemplateMsgAction> contentTemplateMsgActions = contentTemplateMsgActionRepository.findNotDeletedTemplateId(templateId);

        for (BillingNoticeContentTemplateMsgAction contentTemplateMsgAction : contentTemplateMsgActions) {
            Map<String, String> map = new LinkedHashMap<>();
            String linkId = contentTemplateMsgAction.getLinkId();

            map.put("actionId", contentTemplateMsgAction.getTemplateIdAction());
            map.put("linkId", linkId);
            list.add(map);

            contentTemplateMsgAction.setStatus(BillingNoticeContentTemplateMsgAction.STATUS_DELETE);
            contentTemplateMsgActionRepository.save(contentTemplateMsgAction);
        }

        return list;
    }

    /**
     * 取得樣板訊息
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getContentTemplateMsg(String templateId) {
        try {
            Map<String, List<String>> result = dataCache.get(templateId);
            if (result != null && result.get(templateId) != null) {
                return result;
            }
        } catch (Exception e) {
        }

        String queryString = "SELECT BCS_BN_CONTENT_TEMPLATE.TEMPLATE_ID, "
                + "BCS_BN_CONTENT_TEMPLATE.PRODUCT_SWITCH, "
                + "BCS_BN_CONTENT_TEMPLATE.ALT_TEXT,"
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TYPE,"
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_IMAGE_ID,"
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TITLE,"
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_START_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_END_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TEXT, "
                + "BCS_BN_CONTENT_TEMPLATE_ACTION.ACTION_TYPE,"
                + "BCS_BN_CONTENT_TEMPLATE_ACTION.ACTION_LABEL,"
                + "BCS_BN_CONTENT_TEMPLATE_ACTION.ACTION_DATA,"
                + "BCS_BN_CONTENT_TEMPLATE_ACTION.ACTION_TEXT,"
                + "BCS_BN_CONTENT_LINK.LINK_URL, "
                + "BCS_BN_CONTENT_LINK.LINK_ID "
                + "FROM BCS_BN_CONTENT_TEMPLATE "
                + "LEFT JOIN BCS_BN_CONTENT_TEMPLATE_ACTION ON BCS_BN_CONTENT_TEMPLATE.TEMPLATE_ID = BCS_BN_CONTENT_TEMPLATE_ACTION.TEMPLATE_ID "
                + "LEFT JOIN BCS_BN_CONTENT_LINK ON BCS_BN_CONTENT_TEMPLATE_ACTION.LINK_ID = BCS_BN_CONTENT_LINK.LINK_ID "
                + "WHERE (BCS_BN_CONTENT_TEMPLATE.TEMPLATE_ID = ?1 OR BCS_BN_CONTENT_TEMPLATE.TEMPLATE_PARENT_ID = ?1) AND BCS_BN_CONTENT_TEMPLATE.STATUS <> 'DELETE' AND BCS_BN_CONTENT_TEMPLATE_ACTION.STATUS <> 'DELETE' "
                + "ORDER BY BCS_BN_CONTENT_TEMPLATE.TEMPLATE_LETTER, BCS_BN_CONTENT_TEMPLATE_ACTION.ACTION_LETTER";

        Query query = providerService.getEntityManager().createNativeQuery(queryString).setParameter(1, templateId);
        List<Object[]> list = query.getResultList();
        log.info("list:" + list);

        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Object[] o : list) {
            for (int i = 0, max = o.length; i < max; i++) {
                if (i == 0) {
                    List<String> dataList = map.get(o[0]);
                    if (dataList == null) {
                        map.put(o[0].toString(), new ArrayList<String>());
                        continue;
                    } else { //重覆的templateId，因為有多個action
                        for (int j = 9; j <= 14; j++) {
                            if (o[j] == null) {
                                dataList.add(null);
                            } else {
                                dataList.add(o[j].toString());
                            }
                        }
                        break;
                    }
                }

                List<String> dataList = map.get(o[0]);

                if (o[i] == null) {
                    dataList.add(null);
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }

        dataCache.put(templateId, map);
        return map;
    }

    /**
     * 取得樣板訊息所有清單
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getAllContentTemplateMsg() {
        String queryString = "SELECT BCS_BN_CONTENT_TEMPLATE.TEMPLATE_ID, "
                + "BCS_BN_CONTENT_TEMPLATE.PRODUCT_SWITCH, "
                + "BCS_BN_CONTENT_TEMPLATE.ALT_TEXT, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TYPE, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_IMAGE_ID, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TITLE, "
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_START_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_END_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.MODIFY_TIME, "
                + "BCS_ADMIN_USER.USER_NAME "
                + "FROM BCS_BN_CONTENT_TEMPLATE "
                + "LEFT JOIN BCS_ADMIN_USER ON BCS_BN_CONTENT_TEMPLATE.MODIFY_USER = BCS_ADMIN_USER.ACCOUNT "
                + "WHERE BCS_BN_CONTENT_TEMPLATE.STATUS = 'ACTIVE' AND BCS_BN_CONTENT_TEMPLATE.TEMPLATE_LEVEL <> 'COLUMN'"
                + "ORDER BY BCS_BN_CONTENT_TEMPLATE.MODIFY_TIME DESC";

        Query query = providerService.getEntityManager().createNativeQuery(queryString);
        List<Object[]> list = query.getResultList();

        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Object[] o : list) {
            for (int i = 0, max = o.length; i < max; i++) {
                if (i == 0) {
                    map.put(o[0].toString(), new ArrayList<>());
                    continue;
                }

                List<String> dataList = map.get(o[0]);
                if (o[i] == null) {
                    dataList.add("");
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }

        log.debug("{}", map);

        return map;
    }

    /**
     * 取得ProductOn樣板訊息所有清單
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getProductOnContentTemplateMsg() {
        String queryString = "SELECT BCS_BN_CONTENT_TEMPLATE.TEMPLATE_ID, "
                + "BCS_BN_CONTENT_TEMPLATE.PRODUCT_SWITCH, "
                + "BCS_BN_CONTENT_TEMPLATE.ALT_TEXT, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TYPE, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_IMAGE_ID, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TITLE, "
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_START_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_END_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.MODIFY_TIME, "
                + "BCS_ADMIN_USER.USER_NAME "
                + "FROM BCS_BN_CONTENT_TEMPLATE "
                + "LEFT JOIN BCS_ADMIN_USER ON BCS_BN_CONTENT_TEMPLATE.MODIFY_USER = BCS_ADMIN_USER.ACCOUNT "
                + "WHERE BCS_BN_CONTENT_TEMPLATE.STATUS = 'ACTIVE' AND BCS_BN_CONTENT_TEMPLATE.TEMPLATE_LEVEL <> 'COLUMN' AND BCS_BN_CONTENT_TEMPLATE.PRODUCT_SWITCH = 'True'"
                + "ORDER BY BCS_BN_CONTENT_TEMPLATE.MODIFY_TIME DESC";

        Query query = providerService.getEntityManager().createNativeQuery(queryString);
        List<Object[]> list = query.getResultList();

        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Object[] o : list) {
            for (int i = 0, max = o.length; i < max; i++) {
                if (i == 0) {
                    map.put(o[0].toString(), new ArrayList<>());
                    continue;
                }

                List<String> dataList = map.get(o[0]);
                if (o[i] == null) {
                    dataList.add("");
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }

        log.debug("{}", map);

        return map;
    }

    /**
     * 取得ProductOff樣板訊息所有清單
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getProductOffContentTemplateMsg() {
        String queryString = "SELECT BCS_BN_CONTENT_TEMPLATE.TEMPLATE_ID, "
                + "BCS_BN_CONTENT_TEMPLATE.PRODUCT_SWITCH, "
                + "BCS_BN_CONTENT_TEMPLATE.ALT_TEXT, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TYPE, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_IMAGE_ID, "
                + "BCS_BN_CONTENT_TEMPLATE.TEMPLATE_TITLE, "
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_START_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.CURFEW_END_TIME, "
                + "BCS_BN_CONTENT_TEMPLATE.MODIFY_TIME, "
                + "BCS_ADMIN_USER.USER_NAME "
                + "FROM BCS_BN_CONTENT_TEMPLATE "
                + "LEFT JOIN BCS_ADMIN_USER ON BCS_BN_CONTENT_TEMPLATE.MODIFY_USER = BCS_ADMIN_USER.ACCOUNT "
                + "WHERE BCS_BN_CONTENT_TEMPLATE.STATUS = 'ACTIVE' AND BCS_BN_CONTENT_TEMPLATE.TEMPLATE_LEVEL <> 'COLUMN' AND BCS_BN_CONTENT_TEMPLATE.PRODUCT_SWITCH = 'False'"
                + "ORDER BY BCS_BN_CONTENT_TEMPLATE.MODIFY_TIME DESC";

        Query query = providerService.getEntityManager().createNativeQuery(queryString);
        List<Object[]> list = query.getResultList();

        Map<String, List<String>> map = new LinkedHashMap<>();
        for (Object[] o : list) {
            for (int i = 0, max = o.length; i < max; i++) {
                if (i == 0) {
                    map.put(o[0].toString(), new ArrayList<>());
                    continue;
                }

                List<String> dataList = map.get(o[0]);
                if (o[i] == null) {
                    dataList.add("");
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }

        log.debug("{}", map);

        return map;
    }

    /**
     * 取得帳務通知成效清單
     */
    @SuppressWarnings("unchecked")
    public String getBNEffectsTotalPages(String startDate, String endDate) {
        String queryString = "SELECT COUNT(*) FROM ( "
                + " SELECT FORMAT(BNM.MODIFY_TIME, 'yyyy-MM-dd') AS 'Day',"
                + " BNM.ORIG_FILE_TYPE AS 'TTYPE',"
                + " BCT.TEMPLATE_ID AS 'TID',"
                + " BNM.SEND_TYPE AS 'STYPE',"
                + " SUM(CASE WHEN BND.STATUS = 'COMPLETE' THEN 1 ELSE 0 END) AS 'Complete',"
                + " SUM(CASE WHEN BND.STATUS != 'COMPLETE' THEN 1 ELSE 0 END) AS 'Fail',"
                + " DENSE_RANK() OVER ( ORDER BY FORMAT(BNM.MODIFY_TIME, 'yyyy-MM-dd') desc, "
                + "	BCT.TEMPLATE_TYPE, "
                + "	BCT.TEMPLATE_ID ) AS RowNum"
                + " FROM BCS_BILLING_NOTICE_MAIN BNM "
                + " LEFT JOIN BCS_BILLING_NOTICE_DETAIL BND ON BND.NOTICE_MAIN_ID = BNM.NOTICE_MAIN_ID"
                + " LEFT JOIN BCS_BN_CONTENT_TEMPLATE BCT ON BNM.TEMP_ID  = BCT.TEMPLATE_ID"
                + " WHERE BNM.MODIFY_TIME >= '" + startDate + "' "
                + " AND BNM.MODIFY_TIME < DATEADD(DAY, 1, '" + endDate + "') "
                + " GROUP BY FORMAT(BNM.MODIFY_TIME, 'yyyy-MM-dd'), "
                + " BNM.ORIG_FILE_TYPE,"
                + " BCT.TEMPLATE_TYPE,"
                + " BCT.TEMPLATE_ID,"
                + " BNM.SEND_TYPE"
                + ") AS result ";

        log.info("str1: " + queryString);

        List<Object[]> list = providerService.getEntityManager()
                .createNativeQuery(queryString)
                .getResultList();

        String listStr = list.toString();
        log.info("List1:" + list.toString());

        // Total = Empty set,  []  => 0
        if (listStr.length() <= 2) {
            return "0";
        }

        // Total < 10
        char c1 = listStr.charAt(listStr.length() - 2); // 個位數
        if (listStr.length() == 3) {
            return (c1 == '0') ? "0" : "1"; // [0] => 0 , [1] => 1
        }

        // Total >= 10
        if (c1 == '0') {
            return listStr.substring(1, listStr.length() - 2); // [430] => 43
        }
        char c10 = listStr.charAt(listStr.length() - 3); // 十位數
        return listStr.substring(1, listStr.length() - 3) + (++c10); // [431] => 44
    }

    /**
     * 取得帳務通知成效清單
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getBnEffects(String startDate, String endDate, Integer page) {
        int rowStart;
        int rowEnd;
        if (page == null) {
            rowStart = 0;
            rowEnd = Integer.MAX_VALUE;
        } else {
            page--;
            rowStart = page * 10;
            rowEnd = rowStart + 10;
        }

        String queryString =
                " SELECT FORMAT(BNM.MODIFY_TIME, 'yyyy-MM-dd') AS 'Day',"
                + " BNM.ORIG_FILE_TYPE AS 'TTYPE',"
                + " BCT.TEMPLATE_ID AS 'TID',"
                + " BNM.SEND_TYPE AS 'STYPE',"
                + " SUM(CASE WHEN BND.STATUS = 'COMPLETE' THEN 1 ELSE 0 END) AS 'Complete',"
                + " SUM(CASE WHEN BND.STATUS != 'COMPLETE' THEN 1 ELSE 0 END) AS 'Fail'"
                + " FROM  BCS_BILLING_NOTICE_MAIN BNM "
                + " LEFT JOIN BCS_BILLING_NOTICE_DETAIL BND ON BND.NOTICE_MAIN_ID = BNM.NOTICE_MAIN_ID"
                + " LEFT JOIN BCS_BN_CONTENT_TEMPLATE BCT ON BNM.TEMP_ID  = BCT.TEMPLATE_ID"
                + " WHERE BNM.MODIFY_TIME >= '" + startDate + "' "
                + " AND BNM.MODIFY_TIME < DATEADD(DAY, 1, '" + endDate + "') "
                + " GROUP BY FORMAT(BNM.MODIFY_TIME, 'yyyy-MM-dd'), "
                + " BNM.ORIG_FILE_TYPE ,"
                + "	BCT.TEMPLATE_TYPE, "
                + " BCT.TEMPLATE_ID,"
                + " BNM.SEND_TYPE" +
                " ORDER BY Day ASC" +
                " OFFSET " + rowStart + " ROWS" +
                " FETCH NEXT " + Math.abs(rowEnd - rowStart) + " ROWS ONLY";

        log.info("str1: " + queryString);

        List<Object[]> list = providerService.getEntityManager()
                .createNativeQuery(queryString)
                .getResultList();
        log.info("List1: " + list.toString());

        Map<String, List<String>> map = new LinkedHashMap<>();
        int count = 0;
        for (Object[] o : list) {
            count++;
            List<String> dataList = new ArrayList<>();
            map.put(Integer.toString(count), dataList);
            for (int i = 0, max = 6; i < max; i++) {
                if (o[i] == null) {
                    dataList.add("");
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }
        log.info("map1: " + map.toString());

        return map;
    }

    @SuppressWarnings("unchecked")
    public String getBnEffectsDetailTotalPages(String date, String templateName, String sendType) {
        String queryString = "select count(*) from ( "
                + "SELECT D.CREAT_TIME, M.ORIG_FILE_TYPE, D.TITLE, D.TEXT, D.STATUS, D.UID, "
                + "DENSE_RANK() OVER ( ORDER BY D.MODIFY_TIME desc, D.NOTICE_DETAIL_ID) AS RowNum "
                + "from BCS_BILLING_NOTICE_DETAIL D "
                + "join BCS_BILLING_NOTICE_MAIN M on D.NOTICE_MAIN_ID = M.NOTICE_MAIN_ID "
                + "join BCS_BN_CONTENT_TEMPLATE T on M.TEMP_ID  = T.TEMPLATE_ID "
                + "WHERE D.SEND_TIME >= '" + date + "' "
                + "AND D.SEND_TIME <  DATEADD(DAY, 1, '" + date + "') "
                + "AND T.TEMPLATE_ID = N'" + templateName + "' "
                + "AND M.SEND_TYPE = '" + sendType + "' "
                + ") as result ";
        log.info("str1: " + queryString);

        Query query = providerService.getEntityManager().createNativeQuery(queryString);
        List<Object[]> list = query.getResultList();
        String listStr = list.toString();
        log.info("List1:" + list.toString());

        // Total = Empty set,  []  => 0
        if (listStr.length() <= 2) {
            return "0";
        }

        // Total < 10
        char c1 = listStr.charAt(listStr.length() - 2); // 個位數
        if (listStr.length() == 3) {
            return (c1 == '0') ? "0" : "1"; // [0] => 0 , [1] => 1
        }

        // Total >= 10
        if (c1 == '0') {
            return listStr.substring(1, listStr.length() - 2); // [430] => 43
        }
        char c10 = listStr.charAt(listStr.length() - 3); // 十位數
        return listStr.substring(1, listStr.length() - 3) + (++c10); // [431] => 44
    }

    /**
     * 取得帳務通知成效清單
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> getBnEffectsDetail(String date, String templateName, String sendType, String bnType, Integer page) {
        int rowStart;
        int rowEnd;
        if (page == null) {
            rowStart = 0;
            // get all data
            rowEnd = Integer.MAX_VALUE;
        } else {
            // 1~199 => 0~198
            page--;
            rowStart = page * 10;
            // 10 as Size
            rowEnd = rowStart + 10;
        }

        log.info("getBNEffectsDetail:");
//        String queryString = "select * from ( "
//                + "SELECT D.CREAT_TIME, T.TEMPLATE_TYPE, D.TITLE, D.TEXT, D.STATUS, D.UID, "
//                + "DENSE_RANK() OVER ( ORDER BY D.MODIFY_TIME desc, D.NOTICE_DETAIL_ID) AS RowNum "
//                + "from BCS_BILLING_NOTICE_DETAIL D "
//                + "join BCS_BILLING_NOTICE_MAIN M on D.NOTICE_MAIN_ID = M.NOTICE_MAIN_ID "
//                + "join BCS_BN_CONTENT_TEMPLATE T on M.TEMP_ID  = T.TEMPLATE_ID "
//                + "WHERE D.SEND_TIME >= '" + date + "' "
//                + "AND D.SEND_TIME <  DATEADD(DAY, 1, '" + date + "') "
//                + "AND T.TEMPLATE_ID = N'" + templateName + "' "
//                + "AND M.SEND_TYPE = '" + sendType + "' "
//                + ") as result "
//                + "where RowNum >= ?1 and RowNum < ?2 ";

        String queryString = "select * from ( "
                + "SELECT D.CREAT_TIME, M.ORIG_FILE_TYPE, D.TITLE, D.TEXT, D.STATUS, D.UID, "
                + "DENSE_RANK() OVER ( ORDER BY D.MODIFY_TIME desc, D.NOTICE_DETAIL_ID) AS RowNum "
                + "from BCS_BILLING_NOTICE_DETAIL D "
                + "join BCS_BILLING_NOTICE_MAIN M on D.NOTICE_MAIN_ID = M.NOTICE_MAIN_ID "
                + "join BCS_BN_CONTENT_TEMPLATE T on M.TEMP_ID  = T.TEMPLATE_ID "
                + "WHERE D.SEND_TIME >= '" + date + "' "
                + "AND D.SEND_TIME <  DATEADD(DAY, 1, '" + date + "') "
                + "AND T.TEMPLATE_ID = '" + templateName + "' "
                + "AND M.SEND_TYPE = '" + sendType + "' "
                + "AND M.ORIG_FILE_TYPE = N'" + bnType + "' "
                + ") as result "
                + "where RowNum >= ?1 and RowNum < ?2 ";

        log.info("str1: " + queryString);

        Query query = providerService.getEntityManager().createNativeQuery(queryString).setParameter(1, rowStart).setParameter(2, rowEnd);
        List<Object[]> list = query.getResultList();
        log.info("List1: " + list.toString());

        Map<String, List<String>> map = new LinkedHashMap<>();
        int count = 0;
        for (Object[] o : list) {
            count++;
            List<String> dataList = new ArrayList<>();
            map.put(Integer.toString(count), dataList);
            for (int i = 0, max = 6; i < max; i++) {
                if (o[i] == null) {
                    dataList.add("");
                } else {
                    dataList.add(o[i].toString());
                }
            }
        }
        log.info("map1: " + map.toString());

        return map;
    }

    /**
     * 檢查有無重覆使用到UUID
     */
    public Boolean checkDuplicateUUID(String queryType, String uuid) {
        if ("1".equals(queryType)) {
            BillingNoticeContentTemplateMsg contentTemplateMsg = contentTemplateMsgRepository.findOne(uuid);
            return contentTemplateMsg != null;
        } else if ("2".equals(queryType)) {
            BillingNoticeContentTemplateMsgAction contentTemplateMsgAction = contentTemplateMsgActionRepository.findOne(uuid);
            return contentTemplateMsgAction != null;
        } else {
            BillingNoticeContentLink contentLink = contentLinkService.findOne(uuid);
            return contentLink != null;
        }
    }

    /**
     * 新增樣板訊息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createTemplateMsg(List<BillingNoticeContentTemplateMsg> contentTemplateMsgs, List<BillingNoticeContentTemplateMsgAction> contentTemplateMsgActions, List<BillingNoticeContentLink> contentLinks) {
        for (BillingNoticeContentTemplateMsg contentTemplateMsg : contentTemplateMsgs) {
            contentTemplateMsgRepository.save(contentTemplateMsg);
        }

        for (BillingNoticeContentTemplateMsgAction contentTemplateMsgAction : contentTemplateMsgActions) {
            contentTemplateMsgActionRepository.save(contentTemplateMsgAction);
        }

        contentLinkService.save(contentLinks);

        for (BillingNoticeContentTemplateMsg contentTemplateMsg : contentTemplateMsgs) {
            dataCache.refresh(contentTemplateMsg.getTemplateId());
            DataSyncUtil.settingReSync(TEMPLATE_SYNC);
        }
    }

    /**
     * 刪除圖文訊息
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateMsg(String templateId, String account) {
        // 只改變狀態
        List<BillingNoticeContentTemplateMsg> contentTemplateMsgs = contentTemplateMsgRepository.findByTemplateId(templateId);

        for (BillingNoticeContentTemplateMsg contentTemplateMsg : contentTemplateMsgs) {
            log.info("Delete:" + contentTemplateMsg.getTemplateId());
            contentTemplateMsg.setStatus(BillingNoticeContentTemplateMsg.STATUS_DELETE);
            contentTemplateMsg.setModifyUser(account);
            contentTemplateMsg.setModifyTime(new Date());
            contentTemplateMsgRepository.save(contentTemplateMsg);
        }

        for (BillingNoticeContentTemplateMsg contentTemplateMsg : contentTemplateMsgs) {
            dataCache.refresh(contentTemplateMsg.getTemplateId());
            DataSyncUtil.settingReSync(TEMPLATE_SYNC);
        }
    }

    /**
     * Stop Schedule : Wait for Executing Jobs to Finish
     */
    @PreDestroy
    public void destroy() {
        if (scheduler != null && !scheduler.isShutdown()) {
            log.debug("Shutdown....");
            scheduler.shutdown();
        }
    }
}
