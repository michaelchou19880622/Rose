package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.db.entity.AbstractPnpMainEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.bcs.core.utils.DataUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author ???
 */
@Slf4j
@Repository
public class PnpRepositoryCustomImpl implements PnpRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 找出BCS_PNP_DETAIL_X 第一筆 STATUS = 'PROCESS' AND PROC_STAGE = 'PNP' or 'SMS' 的MainId
     * 用此mainId 去找所有同樣MainId AND STATUS = 'PROCESS' AND PROC_STAGE = 'PNP'  or 'SMS' 的BCS_PNP_DETAIL_X 並更新STATUS
     *
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpPNPMsgService#sendingPnpMain()
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public List<? super PnpDetail> updateStatus(PNPFTPType type, String processApName, String stage) {
        log.debug(" begin PNP updateStatus:" + processApName + " type:" + type);
        try {
            /* Stage = PNP */
            List<BigInteger> detailIds = findAndUpdateProcessForUpdate(type.getDetailTable(), stage);
            if (!detailIds.isEmpty()) {
                log.info("Update Detail Status: [PROCESS] to [PNP][SENDING]");
                List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
                for (List<BigInteger> ids : batchDetailIds) {

                    List<? super PnpDetail> details = findPnpDetailById(type, ids);
                    if (!details.isEmpty()) {
                        return details;
                    }
                }
            } else {
                log.info("stage:" + stage + " PNP updateStatus:" + processApName + " type:" + type + " detailIds isEmpty");
            }
            log.debug(" end PNP updateStatus:" + processApName + " type:" + type);
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
        return Collections.emptyList();
    }

    /**
     * 找出最近一筆 PNP 訊息狀態為 PROCESS 的 ID
     * 並更新 PNP 訊息狀態為 SENDING
     *
     * @see PnpRepositoryCustomImpl#updateStatus
     */
    @SuppressWarnings("unchecked")
    private List<BigInteger> findAndUpdateProcessForUpdate(String detailTable, String stage) {
        String sqlString = "select d.PNP_DETAIL_ID from " + detailTable + " d " +
                " where d.STATUS = :status " +
                "  and d.PROC_STAGE = :stage " +
                "  and d.PNP_MAIN_ID in " +
                " (" +
                "   select top 1 a.PNP_MAIN_ID from " + detailTable + " a " +
                "   where a.STATUS = :status " +
                "   and a.BC_STATUS = :bc_status " +
                "   and a.PROC_STAGE = :stage " +
                "   order by a.CREAT_TIME" +
                " ) " +
                " update " + detailTable + "" +
                " set STATUS = :newStatus, PNP_STATUS = :pnp_Status, MODIFY_TIME = :modifyTime " +
                " where STATUS = :status " +
                "   and PROC_STAGE = :stage " +
                "   and PNP_MAIN_ID in " +
                " ( " +
                "   select top 1 a.PNP_MAIN_ID from " + detailTable + " a with(ROWLOCK) " +
                "   where a.STATUS = :status " +
                "   and a.BC_STATUS = :bc_status " +
                "   and a.PROC_STAGE = :stage" +
                "   order by a.CREAT_TIME" +
                " ) ";
        return (List<BigInteger>) entityManager.createNativeQuery(sqlString)
                .setParameter("stage", stage)
                .setParameter("status", AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS)
                .setParameter("bc_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_PNP_PROCESS)
                .setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING)
                .setParameter("pnp_Status", AbstractPnpMainEntity.MSG_SENDER_STATUS_PNP_SENDING)
                .setParameter("modifyTime", new Date())
                .getResultList();
    }

    /**
     * 找出最近一筆 PNP 訊息狀態為 PROCESS 的 ID
     * 並更新 PNP 訊息狀態為 SENDING
     *
     * @see PnpRepositoryCustomImpl#updateStatus
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<BigInteger> findBcToSmsIdList(String detailTable) {
        String sql = String.format(
                " SELECT " +
                        "     d.PNP_DETAIL_ID " +
                        " FROM " +
                        "     %s d" +
                        " WHERE " +
                        "     d.PROC_STAGE = 'SMS'" +
                        "     AND d.BC_STATUS = %s"
                , detailTable, AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_SMS_PROCESS);
        return (List<BigInteger>) entityManager.createNativeQuery(sql).getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BigInteger> updateBcToSms(String detailTable, List<BigInteger> idList) {
        String sql = String.format(
                " UPDATE %s SET" +
                        "    STATUS = :sms_status," +
                        "    SMS_STATUS = :sms_status," +
                        "    MODIFY_TIME = :now" +
                        " WHERE PROC_STAGE = 'SMS'" +
                        "    AND PNP_DETAIL_ID in (:ids)", detailTable);
        return (List<BigInteger>) entityManager.createNativeQuery(sql)
                .setParameter("bc_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_SMS_PROCESS)
                .setParameter("sms_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_SENDING)
                .setParameter("now", new Date())
                .setParameter("ids", idList)
                .getResultList();
    }

    /**
     * Find Detail Status is CHECK_DELIVERY
     * Update Status to Sending
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<BigInteger> findDeliveryExpiredDetailId(String detailTable) {
        String sql = String.format(
                " SELECT" +
                        "   d.PNP_DETAIL_ID " +
                        " FROM" +
                        "    %s d" +
                        " WHERE" +
                        "    d.PNP_STATUS = 'CHECK_DELIVERY'" +
                        "    AND d.PNP_DELIVERY_EXPIRE_TIME <= :now", detailTable);
        log.info(sql);

        EntityManager entityManager1 = JPAUtil.getEntityManagerFactory().createEntityManager();
        log.info("getFactory");
        EntityTransaction entityTransaction = entityManager1.getTransaction();
        entityTransaction.begin();
        log.info("begin");

        List<BigInteger> list = (List<BigInteger>) entityManager1.createNativeQuery(sql)
                .setParameter("now", new Date()).getResultList();
        entityTransaction.commit();
        log.info("commit");

        entityManager1.close();
        log.info("close");

        JPAUtil.shutdown();
        log.info("shutdown");

        return list;
    }

    /**
     * Find Detail Status is CHECK_DELIVERY
     * Update Status to Sending
     */
    @Override
    public int updateDeliveryExpired(String detailTable, List<BigInteger> idList) {
        String sql = String.format(
                " UPDATE" +
                        "    %s" +
                        " SET" +
                        "    PROC_STAGE = 'SMS'," +
                        "    STATUS = :sms_status," +
                        "    PNP_STATUS = :pnp_status," +
                        "    SMS_STATUS = :sms_status," +
                        "    MODIFY_TIME = :now" +
                        " WHERE" +
                        "    DETAIL_ID IN (:ids)", detailTable);
        log.info(sql);
        log.info("ids: " + DataUtils.toNormalJson(idList));
        return (int) entityManager.createNativeQuery(sql)
                .setParameter("pnp_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_PNP_FAIL_SMS_PROCESS)
                .setParameter("sms_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_SENDING)
                .setParameter("now", new Date())
                .setParameter("ids", idList).getSingleResult();
    }

    /**
     * 找出第一個WAIT Main & STAGE = BC 並更新狀態為SENDING
     * 該WAIT MainID的 PnpDetail , status = WAIT  並更新狀態為SENDING
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public List<? super PnpDetail> updateStatusByStageBc(PNPFTPType type, String procApName, Set<Long> allMainIds) {
        log.info(String.format("ProcessApName: %s, Type: %s", procApName, type));
        try {
            // 找出第一筆 WAIT MAIN 並更新狀態為Sending
            Long waitMainId = findAndUpdateFirstWaitMainByStageBc(procApName, type.getMainTable());
            if (waitMainId != null) {
                allMainIds.add(waitMainId);
            } else {
                log.info(String.format("Type %s Main Id Not Found with Status is Wait.", type));
                return Collections.emptyList();
            }

            if (!allMainIds.isEmpty()) {
                log.info(String.format("All BC Sending Type [%s] Mid List: %s", type, allMainIds.toString()));
                //  根據MAIN_ID 更新 Detail
                List<BigInteger> detailIds = findAndUpdateDetailByMainAndStatus(allMainIds, type);
                if (!detailIds.isEmpty()) {
                    log.info("Update Detail Status: [WAIT] to [BC][SENDING]");
                    List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
                    return findPnpDetailById(type, batchDetailIds.get(0));
                }
            } else {
                log.info(String.format("All BC Sending Type [%s] Mid List: is Empty!!", type));
            }
        } catch (Exception e) {
            log.error("Exception", e);
            throw e;
        }
        return Collections.emptyList();
    }

    /**
     * 根據Main ID && STATUS = WAIT 找出PNP Detail 並更新 status 為SENDING
     */
    @SuppressWarnings("unchecked")
    private List<BigInteger> findAndUpdateDetailByMainAndStatus(Set<Long> mainIds, PNPFTPType type) {
        List<String> statusList = new ArrayList<>();
        statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
        Date modifyTime = Calendar.getInstance().getTime();
        String detailTable = type.getDetailTable();

        String sql = String.format(" SELECT b.PNP_DETAIL_ID FROM %s b " +
                " WHERE b.PNP_MAIN_ID in(:mainIds)" +
                " AND b.STATUS in(:status)" +

                " UPDATE %s SET " +
                "     STATUS = :newStatus, " +
                "     BC_STATUS = :bc_status, " +
                "     MODIFY_TIME = :modifyTime " +
                " WHERE " +
                "     PNP_DETAIL_ID IN( " +
                "         SELECT d.PNP_DETAIL_ID FROM %s d WITH (ROWLOCK) " +
                "         WHERE d.PNP_MAIN_ID in(:mainIds) " +
                "         AND d.STATUS in(:status) " +
                " ); ", detailTable, detailTable, detailTable);

        return (List<BigInteger>) entityManager.createNativeQuery(sql)
                .setParameter("status", statusList).setParameter("mainIds", mainIds)
                .setParameter("modifyTime", modifyTime)
                .setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING)
                .setParameter("bc_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_SENDING)
                .getResultList();
    }


    /**
     * 找出第一個status = WAIT & STAGE = BC 的PNP Main 並更新status = SENDING
     */
    @SuppressWarnings("unchecked")
    private Long findAndUpdateFirstWaitMainByStageBc(String procApName, String mainTable) {
        String waitMainString = "select  TOP 1 m.PNP_MAIN_ID from  " + mainTable + " m  " +
                " where m.STATUS = :status " +
                "     and m.PROC_STAGE " +
                "     in (:stage) " +
                " order by m.CREAT_TIME " +

                " update " + mainTable +
                " set STATUS = :newStatus, PROC_AP_NAME = :procApName, MODIFY_TIME = :modifyTime " +
                "   where PNP_MAIN_ID" +
                "       in (" +
                "              select TOP 1 a.PNP_MAIN_ID from " + mainTable + " a WITH(ROWLOCK) " +
                "              where a.STATUS = :status " +
                "                  and a.PROC_STAGE " +
                "                  in (:stage) " +
                "              order by a.CREAT_TIME" +
                "          )";
        List<BigInteger> mains = (List<BigInteger>) entityManager.createNativeQuery(waitMainString)
                .setParameter("status", AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT)
                .setParameter("stage", AbstractPnpMainEntity.STAGE_BC)
                .setParameter("procApName", procApName)
                .setParameter("modifyTime", new Date())
                .setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_SENDING).getResultList();
        if (mains == null || mains.isEmpty()) {
            return null;
        }
        log.info("Update Main Status: [WAIT] to [BC][SENDING]");
        return mains.get(0).longValue();
    }

    /**
     * 根據mainId 找出物件
     */
    @Override
    public PnpMain findMainByMainId(PNPFTPType type, Long mainId) {
        log.info("Find Main By MainId : " + type);
        switch (type) {
            case MITAKE:
                return findMainByMainIdMitake(mainId);
            case MING:
                return findMainByMainIdMing(mainId);
            case EVERY8D:
                return findMainByMainIdEvery8d(mainId);
            case UNICA:
                return findMainByMainIdUnica(mainId);
            default:
                return null;
        }
    }

    /**
     * 找出PnpMainMitake
     *
     * @see this#findMainByMainId
     */
    @SuppressWarnings("unchecked")
    private PnpMainMitake findMainByMainIdMitake(Long mainId) {

        String sqlString = "select  m.* from BCS_PNP_MAIN_MITAKE m where m.PNP_MAIN_ID = :mainId ";
        List<PnpMainMitake> mains = entityManager.createNativeQuery(sqlString, PnpMainMitake.class)
                .setParameter("mainId", mainId)
                .getResultList();
        return mains == null || mains.isEmpty() ? null : mains.get(0);
    }

    /**
     * 找出PnpMainEvery8d
     *
     * @see this#findMainByMainId
     */
    @SuppressWarnings("unchecked")
    private PnpMainEvery8d findMainByMainIdEvery8d(Long mainId) {

        String sqlString = "select  m.* from BCS_PNP_MAIN_EVERY8D m where m.PNP_MAIN_ID = :mainId ";
        List<PnpMainEvery8d> mains = entityManager.createNativeQuery(sqlString, PnpMainEvery8d.class)
                .setParameter("mainId", mainId)
                .getResultList();
        return mains == null || mains.isEmpty() ? null : mains.get(0);
    }

    /**
     * 找出PnpMainUnica
     *
     * @see this#findMainByMainId
     */
    @SuppressWarnings("unchecked")
    private PnpMainUnica findMainByMainIdUnica(Long mainId) {

        String sqlString = "select  m.* from BCS_PNP_MAIN_UNICA m where m.PNP_MAIN_ID = :mainId ";
        List<PnpMainUnica> mains = entityManager.createNativeQuery(sqlString, PnpMainUnica.class)
                .setParameter("mainId", mainId)
                .getResultList();
        return mains == null || mains.isEmpty() ? null : mains.get(0);
    }

    /**
     * 找出PnpMainMing
     *
     * @see this#findMainByMainId
     */
    @SuppressWarnings("unchecked")
    private PnpMainMing findMainByMainIdMing(Long mainId) {
        String sqlString = "select  m.* from BCS_PNP_MAIN_MING m where m.PNP_MAIN_ID = :mainId ";
        List<PnpMainMing> mains = entityManager.createNativeQuery(sqlString, PnpMainMing.class)
                .setParameter("mainId", mainId)
                .getResultList();
        return mains == null || mains.isEmpty() ? null : mains.get(0);
    }


    /**
     * find PNP detail by Ids and PNPFTPType
     *
     * @param type 前方來源系統種類
     * @param ids  id list
     * @return PnpDetail List
     */
    @Override
    public List<? super PnpDetail> findPnpDetailById(PNPFTPType type, List<BigInteger> ids) {
        switch (type) {
            case MITAKE:
                return findPnpDetailMitake(ids);
            case MING:
                return findPnpDetailMing(ids);
            case EVERY8D:
                return findPnpDetailEvery8d(ids);
            case UNICA:
                return findPnpDetailUnica(ids);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Unica格式
     * Find PnpDetailList By Ids
     *
     * @param ids id list
     * @return PnpDetail list
     * @see this#findPnpDetailById
     */
    @SuppressWarnings("unchecked")
    private List<? super PnpDetail> findPnpDetailUnica(List<BigInteger> ids) {
        String sqlString = "select * from BCS_PNP_DETAIL_UNICA WHERE PNP_DETAIL_ID in (:ids) ";
        List<? super PnpDetail> detailList = new ArrayList<>();
        List<List<BigInteger>> partitionList = Lists.partition(ids, 1000);
        for (List<BigInteger> idList : partitionList) {
            log.info(sqlString);
            List<PnpDetailUnica> resultList = entityManager.createNativeQuery(sqlString, PnpDetailUnica.class)
                    .setParameter("ids", idList)
                    .getResultList();
            detailList.addAll(resultList);
        }
        return detailList;
    }

    /**
     * 互動格式
     * Find PnpDetailList By Ids
     *
     * @param ids id list
     * @return PnpDetail list
     * @see this#findPnpDetailById
     */
    @SuppressWarnings("unchecked")
    private List<? super PnpDetail> findPnpDetailEvery8d(List<BigInteger> ids) {
        String sqlString = "select * from BCS_PNP_DETAIL_EVERY8D WHERE PNP_DETAIL_ID in (:ids) ";
        List<? super PnpDetail> detailList = new ArrayList<>();
        List<List<BigInteger>> partitionList = Lists.partition(ids, 1000);
        for (List<BigInteger> idList : partitionList) {
            log.info(sqlString);
            List<PnpDetailUnica> resultList = entityManager.createNativeQuery(sqlString, PnpDetailEvery8d.class)
                    .setParameter("ids", idList)
                    .getResultList();
            detailList.addAll(resultList);
        }
        return detailList;
    }

    /**
     * 明宣格式
     * Find PnpDetailList By Ids
     *
     * @param ids id list
     * @return PnpDetail list
     * @see this#findPnpDetailById
     */
    @SuppressWarnings("unchecked")
    private List<? super PnpDetail> findPnpDetailMing(List<BigInteger> ids) {
        String sqlString = "select * from BCS_PNP_DETAIL_MING WHERE PNP_DETAIL_ID in (:ids) ";
        List<? super PnpDetail> detailList = new ArrayList<>();
        List<List<BigInteger>> partitionList = Lists.partition(ids, 1000);
        for (List<BigInteger> idList : partitionList) {
            log.info(sqlString);
            List<PnpDetailUnica> resultList = entityManager.createNativeQuery(sqlString, PnpDetailMing.class)
                    .setParameter("ids", idList)
                    .getResultList();
            detailList.addAll(resultList);
        }
        return detailList;
    }

    /**
     * 山竹格式
     * Find PnpDetailList By Ids
     *
     * @param ids id list
     * @return PnpDetail list
     * @see this#findPnpDetailById
     */
    @SuppressWarnings("unchecked")
    private List<? super PnpDetail> findPnpDetailMitake(List<BigInteger> ids) {
        String sqlString = "select * from BCS_PNP_DETAIL_MITAKE WHERE PNP_DETAIL_ID in (:ids) ";
        List<? super PnpDetail> detailList = new ArrayList<>();
        List<List<BigInteger>> partitionList = Lists.partition(ids, 1000);
        for (List<BigInteger> idList : partitionList) {
            log.info(sqlString);
            List<PnpDetailUnica> resultList = entityManager.createNativeQuery(sqlString, PnpDetailMitake.class)
                    .setParameter("ids", idList)
                    .getResultList();
            detailList.addAll(resultList);
        }
        return detailList;
    }
}
