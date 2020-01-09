package com.bcs.core.taishin.circle.db.repository;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ???
 */
@Slf4j
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
        log.info("Update Status: {}", procApName);
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
            List<BigInteger> detailIds = findAndUpdateDetailByMainAndStatus(allMainIds);

            /* Valid is empty */
            if (detailIds.isEmpty()) {
                log.debug("Detail Id List is Empty!!");
                return new Object[]{Collections.emptySet(), Collections.emptyList()};
            }

            /* 4. Find Detail object list by Detail id list */
            return new Object[]{allMainIds, findDetailByIds(detailIds)};
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
    }

    private List<BillingNoticeDetail> findDetailByIds(List<BigInteger> detailIds) {
        List<BillingNoticeDetail> detailList = new ArrayList<>();
        List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
        for (List<BigInteger> ids : batchDetailIds) {
            List<BillingNoticeDetail> details = billingNoticeDetailRepository.findByNoticeDetailIdIn(ids);
            if (!details.isEmpty()) {
                detailList.addAll(details);
            }
        }
        return detailList;
    }

    private Set<Long> findRetryMainSet(String procApName, List<String> tempIds) {
        Set<Long> retrySet = new HashSet<>();
        Long retryMainId = findAndUpdateFirstRetryDetailOnMain(procApName, tempIds);
        if (retryMainId != null) {
            retrySet.add(retryMainId);
        }
        return retrySet;
    }

    private Set<Long> findWaitMainSet(String procApName, List<String> tempIds) {
        Set<Long> waitSet = new HashSet<>();
        Long waitMainId = findAndUpdateFirstWaitMain(procApName, tempIds);
        if (waitMainId != null) {
            waitSet.add(waitMainId);
        } else {
            log.debug("BCS_BILLING_NOTICE updateStatus waitMainId is null");
        }
        return waitSet;
    }

//    /**
//     * 根據NOTICE_DETAIL_ID找出BillingNoticeDetail
//     *
//     * @param ids
//     * @return
//     */
//    @SuppressWarnings("unchecked")
//    public List<BillingNoticeDetail> findBillingNoticeDetailById(List<BigInteger> ids) {
//        String sqlString = "select  * from BCS_BILLING_NOTICE_DETAIL WHERE NOTICE_DETAIL_ID in (:ids) ";
//        List<BillingNoticeDetail> details = entityManager.createNativeQuery(sqlString, BillingNoticeDetail.class)
//                .setParameter("ids", ids).getResultList();
//        return details;
//    }


    /**
     * 找出第一個RETRY DETAIL 的 BillingNoticeMain 並更新狀態
     */
    @SuppressWarnings("unchecked")
    private Long findAndUpdateFirstRetryDetailOnMain(String procApName, List<String> tempIds) {
        Date modifyTime = Calendar.getInstance().getTime();
        String sqlString = "select  Top 1 m.NOTICE_MAIN_ID from BCS_BILLING_NOTICE_DETAIL b, BCS_BILLING_NOTICE_MAIN m  "
                + "where  m.NOTICE_MAIN_ID = b.NOTICE_MAIN_ID  and b.STATUS = :status and m.TEMP_ID in (:tempIds) Order by b.CREAT_TIME "
                + "update BCS_BILLING_NOTICE_MAIN  set STATUS = :newStatus , PROC_AP_NAME = :procApName , MODIFY_TIME = :modifyTime "
                + " where NOTICE_MAIN_ID  IN (select TOP 1 a.NOTICE_MAIN_ID from BCS_BILLING_NOTICE_MAIN a WITH(ROWLOCK) , BCS_BILLING_NOTICE_DETAIL d "
                + " where a.NOTICE_MAIN_ID = d.NOTICE_MAIN_ID  and d.STATUS = :status and a.TEMP_ID in (:tempIds) Order by d.CREAT_TIME)  ";

        List<BigInteger> mains = (List<BigInteger>) entityManager.createNativeQuery(sqlString)
                .setParameter("status", BillingNoticeMain.NOTICE_STATUS_RETRY)
                .setParameter("tempIds", tempIds)
                .setParameter("procApName", procApName)
                .setParameter("modifyTime", modifyTime)
                .setParameter("newStatus", BillingNoticeMain.NOTICE_STATUS_SENDING)
                .getResultList();
        if (mains != null && !mains.isEmpty()) {
            return mains.get(0).longValue();
        }

        return null;
    }


    /**
     * 找出第一個WAIT BillingNoticeMain 並更新狀態
     */
    private Long findAndUpdateFirstWaitMain(String procApName, List<String> tempIds) {
        Date modifyTime = Calendar.getInstance().getTime();
        // 找出第一個WAIT BillingNoticeMain 並更新
        String waitMainString = "select  TOP 1 m.NOTICE_MAIN_ID from  BCS_BILLING_NOTICE_MAIN m  "
                + "where m.STATUS = :status and m.TEMP_ID in (:tempIds) Order by m.CREAT_TIME "
                + "update BCS_BILLING_NOTICE_MAIN  set STATUS = :newStatus , PROC_AP_NAME = :procApName , MODIFY_TIME = :modifyTime "
                + "   where NOTICE_MAIN_ID  IN (select TOP 1 a.NOTICE_MAIN_ID from BCS_BILLING_NOTICE_MAIN a WITH(ROWLOCK) "
                + "		where a.STATUS = :status and a.TEMP_ID in (:tempIds) Order by a.CREAT_TIME)  ";
        List<BigInteger> mainList = (List<BigInteger>) entityManager.createNativeQuery(waitMainString)
                .setParameter("status", BillingNoticeMain.NOTICE_STATUS_WAIT)
                .setParameter("tempIds", tempIds)
                .setParameter("procApName", procApName)
                .setParameter("modifyTime", modifyTime)
                .setParameter("newStatus", BillingNoticeMain.NOTICE_STATUS_SENDING).getResultList();
        /*TODO ALAN */
//        List<BigInteger> mainList = billingNoticeMainRepository.findAndUpdateFirstWaitMain(
//                BillingNoticeMain.NOTICE_STATUS_WAIT,
//                tempIds,
//                procApName,
//                new Date(),
//                BillingNoticeMain.NOTICE_STATUS_SENDING
//        );
        if (mainList != null && !mainList.isEmpty()) {
            return mainList.get(0).longValue();
        }
        return null;
    }

    /**
     * 查詢並更新WAIT/RERTY 明細
     *
     * @param mainIds
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<BigInteger> findAndUpdateDetailByMainAndStatus(Set<Long> mainIds) {
        List<String> statusList = new ArrayList<String>();
        statusList.add(BillingNoticeMain.NOTICE_STATUS_WAIT);
        statusList.add(BillingNoticeMain.NOTICE_STATUS_RETRY);
        Date modifyTime = Calendar.getInstance().getTime();

        String sqlString = "select  b.NOTICE_DETAIL_ID from BCS_BILLING_NOTICE_DETAIL b where  b.NOTICE_MAIN_ID in (:mainIds) and b.STATUS in (:status)  "
                + "update BCS_BILLING_NOTICE_DETAIL  set STATUS = :newStatus , MODIFY_TIME = :modifyTime  where NOTICE_DETAIL_ID  IN "
                + "	(select d.NOTICE_DETAIL_ID from BCS_BILLING_NOTICE_DETAIL d WITH(ROWLOCK) where  d.NOTICE_MAIN_ID in (:mainIds) and d.STATUS in (:status) )  ";
        List<BigInteger> details = (List<BigInteger>) entityManager.createNativeQuery(sqlString)
                .setParameter("status", statusList).setParameter("mainIds", mainIds)
                .setParameter("modifyTime", modifyTime)
                .setParameter("newStatus", BillingNoticeMain.NOTICE_STATUS_SENDING).getResultList();

        return details;
    }

}
