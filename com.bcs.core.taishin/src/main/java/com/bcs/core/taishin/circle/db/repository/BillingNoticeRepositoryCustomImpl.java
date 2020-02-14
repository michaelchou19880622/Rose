package com.bcs.core.taishin.circle.db.repository;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ???
 */
@Slf4j(topic = "BNRecorder")
@Repository
public class BillingNoticeRepositoryCustomImpl implements BillingNoticeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private BillingNoticeMainRepository billingNoticeMainRepository;
    private BillingNoticeDetailRepository billingNoticeDetailRepository;

    @Autowired
    public BillingNoticeRepositoryCustomImpl(
            BillingNoticeMainRepository billingNoticeMainRepository,
            BillingNoticeDetailRepository billingNoticeDetailRepository) {
        this.billingNoticeMainRepository = billingNoticeMainRepository;
        this.billingNoticeDetailRepository = billingNoticeDetailRepository;
    }

    /**
     * 更新狀態
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public Object[] updateStatus(String procApName, List<String> tempIds) {
        log.info("Process Ap Name: {}", procApName);
        try {
            Set<Long> allMainIds = new HashSet<>();
            /* 1. Find wait main set and update status to sending */
            allMainIds.addAll(findWaitMainSet(procApName, tempIds));
            /* 2. Find retry main set and update status to sending */
            allMainIds.addAll(findRetryMainSet(procApName, tempIds));

            /* Valid is empty */
            if (allMainIds.isEmpty()) {
                log.info("Main Id List is Empty!!");
                return new Object[]{Collections.emptySet(), Collections.emptyList()};
            }

            /* 3. Find Detail id list by Main id list */
            List<BillingNoticeDetail> detailList = findAllWaitRetryDetailByMainId(allMainIds);

            if (detailList.isEmpty()) {
                log.info("Detail List is Empty!!");
                return new Object[]{Collections.emptySet(), Collections.emptyList()};
            }
            /* 4. Find Detail object list by Detail id list */
            return new Object[]{allMainIds, detailList};
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
    }


    private Set<Long> findRetryMainSet(String procApName, List<String> tempIds) {
        Set<Long> retrySet = new HashSet<>();
        List<Long> retryMainIdList = findAndUpdateFirstRetryDetailOnMain(procApName, tempIds);
        retryMainIdList.forEach(retryMainId -> {
            if (retryMainId != null) {
                retrySet.add(retryMainId);
            } else {
                log.debug("BCS_BILLING_NOTICE updateStatus retryMainId is null");
            }

        });
        return retrySet;
    }

    private Set<Long> findWaitMainSet(String procApName, List<String> tempIds) {
        Set<Long> waitSet = new HashSet<>();
        List<Long> waitMainIdList = findAndUpdateFirstWaitMain(procApName, tempIds);
        waitMainIdList.forEach(waitMainId -> {
            if (waitMainId != null) {
                waitSet.add(waitMainId);
            } else {
                log.debug("BCS_BILLING_NOTICE updateStatus waitMainId is null");
            }
        });
        return waitSet;
    }

    /**
     * 找出第一個RETRY DETAIL 的 BillingNoticeMain 並更新狀態
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public List<Long> findAndUpdateFirstRetryDetailOnMain(String procApName, List<String> tempIds) {
        try {
            String sqlString = " SELECT TOP 100 M.NOTICE_MAIN_ID" +
                    " FROM BCS_BILLING_NOTICE_DETAIL B, BCS_BILLING_NOTICE_MAIN M" +
                    " WHERE M.PROC_AP_NAME=:procApName" +
                    " AND M.NOTICE_MAIN_ID=B.NOTICE_MAIN_ID" +
                    " AND B.STATUS=:status" +
                    " AND M.TEMP_ID in (:tempIds)" +
                    " ORDER BY B.CREAT_TIME DESC";

            List<BigInteger> mainList = (List<BigInteger>) entityManager.createNativeQuery(sqlString)
                    .setParameter("procApName", procApName)
                    .setParameter("status", BillingNoticeMain.NOTICE_STATUS_RETRY)
                    .setParameter("tempIds", tempIds)
                    .getResultList();

            if (mainList.isEmpty()) {
                return Collections.emptyList();
            }

            String updateString = "UPDATE BCS_BILLING_NOTICE_MAIN" +
                    " SET STATUS=:status, MODIFY_TIME=:modifyTime" +
                    " WHERE NOTICE_MAIN_ID IN (:ids)";

            int modifyCount = entityManager.createNativeQuery(updateString)
                    .setParameter("modifyTime", new Date())
                    .setParameter("status", BillingNoticeMain.NOTICE_STATUS_SENDING)
                    .setParameter("ids", mainList)
                    .executeUpdate();

            log.info("Update main retry to sending!! {}", modifyCount);

            if (mainList.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> list = new ArrayList<>();
            mainList.forEach(v -> list.add(Long.parseLong(v.toString())));
            mainList.clear();
            return list;
        } catch (Exception e) {
            log.error("SQLException", e);
            throw e;
        }
    }


    /**
     * 找出第一個WAIT BillingNoticeMain 並更新狀態
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> findAndUpdateFirstWaitMain(String procApName, List<String> tempIds) {
        try {
            String waitMainString = " SELECT TOP 100 NOTICE_MAIN_ID" +
                    " FROM BCS_BILLING_NOTICE_MAIN" +
                    " WHERE PROC_AP_NAME=:procApName" +
                    " AND STATUS=:status" +
                    " AND TEMP_ID IN (:tempIds)" +
                    " ORDER BY CREAT_TIME DESC";

            log.info("Wait String: {}", waitMainString);

            List<BigInteger> mainList = entityManager.createNativeQuery(waitMainString)
                    .setParameter("procApName", procApName)
                    .setParameter("status", BillingNoticeMain.NOTICE_STATUS_WAIT)
                    .setParameter("tempIds", tempIds)
                    .getResultList();

            if (mainList.isEmpty()) {
                return Collections.emptyList();
            }

            String updateString = "UPDATE BCS_BILLING_NOTICE_MAIN" +
                    " SET STATUS=:status, MODIFY_TIME=:modifyTime" +
                    " WHERE NOTICE_MAIN_ID IN (:ids)";
            log.info("Update String: {}", updateString);

            int modifyCount = entityManager.createNativeQuery(updateString)
                    .setParameter("modifyTime", new Date())
                    .setParameter("status", BillingNoticeMain.NOTICE_STATUS_SENDING)
                    .setParameter("ids", mainList)
                    .executeUpdate();

            log.info("Update main wait to sending!! {}", modifyCount);

            if (mainList.isEmpty()) {
                return Collections.emptyList();
            }
            List<Long> list = new ArrayList<>();
            mainList.forEach(v -> list.add(Long.parseLong(v.toString())));
            mainList.clear();
            return list;
        } catch (Exception e) {
            log.error("SQLException", e);
            throw e;
        }
    }

    /**
     * 查詢並更新WAIT/RETRY 明細
     */
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public List<BillingNoticeDetail> findAllWaitRetryDetailByMainId(Set<Long> mainIds) {
        String selectSql = "SELECT NOTICE_DETAIL_ID FROM BCS_BILLING_NOTICE_DETAIL" +
                " WHERE NOTICE_MAIN_ID IN (:IDS) " +
                " AND STATUS IN (:STATUS_LIST)";

        log.info(selectSql);

        List<BigInteger> detailIdList = entityManager.createNativeQuery(selectSql)
                .setParameter("STATUS_LIST", Arrays.asList(BillingNoticeMain.NOTICE_STATUS_WAIT, BillingNoticeMain.NOTICE_STATUS_RETRY))
                .setParameter("IDS", mainIds)
                .getResultList();

        if (detailIdList.isEmpty()) {
            log.info("Detail list is empty!!");
            return Collections.emptyList();
        }
        String updateSql = "UPDATE BCS_BILLING_NOTICE_DETAIL" +
                " SET STATUS=:NEW_STATUS, MODIFY_TIME=:MODIFY_TIME " +
                " WHERE NOTICE_DETAIL_ID IN (:IDS)";
        log.info(updateSql);
        int i = entityManager.createNativeQuery(updateSql)
                .setParameter("IDS", detailIdList)
                .setParameter("MODIFY_TIME", new Date())
                .setParameter("NEW_STATUS", BillingNoticeMain.NOTICE_STATUS_SENDING)
                .executeUpdate();
        if (i > 0) {
            log.info("Update Wait and retry to sending!! {}", i);
        }

        String selectSql2 = "SELECT * FROM BCS_BILLING_NOTICE_DETAIL WHERE NOTICE_DETAIL_ID IN (:IDS)";
        log.info(selectSql);
        return entityManager.createNativeQuery(selectSql2, BillingNoticeDetail.class)
                .setParameter("IDS", detailIdList)
                .getResultList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreNotSendDetail() {
        String updateSql = "UPDATE BCS_BILLING_NOTICE_DETAIL" +
                " SET STATUS=:NEW_STATUS, MODIFY_TIME=:NOW" +
                " WHERE STATUS=:OLD_STATUS";
        log.info(updateSql);
        int i = entityManager.createNativeQuery(updateSql)
                .setParameter("OLD_STATUS", BillingNoticeMain.NOTICE_STATUS_SENDING)
                .setParameter("NEW_STATUS", BillingNoticeMain.NOTICE_STATUS_WAIT)
                .setParameter("NOW", new Date())
                .executeUpdate();
        if (i > 0) {
            log.info("Restore Billing-Notice detail status sending to wait!! {}", i);
        }

    }

}
