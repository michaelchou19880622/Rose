package com.bcs.core.taishin.circle.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * The interface Billing notice main repository.
 * @author ???
 */
public interface BillingNoticeMainRepository extends EntityRepository<BillingNoticeMain, Long> {

    /**
     * Find by status list.
     *
     * @param status the status
     * @return the list
     */
    @Transactional(readOnly = true, timeout = 30)
    List<BillingNoticeMain> findByStatus(String status);

    /**
     * Find by status and template ids list.
     *
     * @param status  the status
     * @param tempIds the temp ids
     * @return the list
     */
    @Query("select x from BillingNoticeMain x where x.status in (?1) and x.tempId in (?2) ")
    List<BillingNoticeMain> findByStatusAndTemplateIds(List<String> status, List<String> tempIds);

    /**
     * Update billing notice main status.
     *
     * @param status       the status
     * @param modifyTime   the modify time
     * @param noticeMainId the notice main id
     */
    @Modifying
    @Query("update BillingNoticeMain x set x.status = ?1 , x.modifyTime = ?2 where x.noticeMainId = ?3 ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    void updateBillingNoticeMainStatus(String status, Date modifyTime, Long noticeMainId);

    /**
     * Update status and proc ap name.
     *
     * @param status       the status
     * @param procApName   the proc ap name
     * @param modifyTime   the modify time
     * @param noticeMainId the notice main id
     */
    @Modifying
    @Query("update BillingNoticeMain x set x.status = ?1 , x.procApName = ?2 , x.modifyTime = ?3  where x.noticeMainId in (?4)  ")
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    void updateStatusAndProcApName(String status, String procApName, Date modifyTime, List<Long> noticeMainId);

    /**
     * Find and update first wait main list.
     *
     * @param status     the status
     * @param tempIds    the temp ids
     * @param procApName the proc ap name
     * @param modifyTime the modify time
     * @param newStatus  the new status
     * @return the list
     */
    @Modifying
    @Query(value = "SELECT TOP 1 M.NOTICE_MAIN_ID FROM BCS_BILLING_NOTICE_MAIN M " +
            " WHERE 1=1 " +
            " AND M.STATUS = ?1 " +
            " AND M.TEMP_ID IN (?2) " +
            " ORDER BY M.CREAT_TIME " +

            " UPDATE BCS_BILLING_NOTICE_MAIN SET " +
            " STATUS = ?5, " +
            " PROC_AP_NAME = ?3," +
            " MODIFY_TIME = ?4" +
            " WHERE NOTICE_MAIN_ID IN (SELECT TOP 1 A.NOTICE_MAIN_ID FROM BCS_BILLING_NOTICE_MAIN A WITH(ROWLOCK)" +
            " WHERE 1=1 " +
            " AND A.STATUS = ?1 " +
            " AND A.TEMP_ID IN (?2) " +
            " ORDER BY A.CREAT_TIME)", nativeQuery = true)
    @Transactional(rollbackFor = Exception.class)
    List<BigInteger> findAndUpdateFirstWaitMain(String status, List<String> tempIds, String procApName, Date modifyTime, String newStatus);

}
