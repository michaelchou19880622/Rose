package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpStageEnum;
import com.bcs.core.taishin.circle.PNP.code.PnpStatusEnum;
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
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
    public List<? super PnpDetail> updateStatus(PnpFtpSourceEnum type, String processApName, PnpStageEnum stage, PnpStatusEnum bcStatus) {
        log.info("begin PNP updateStatus: {}, type: {}", processApName, type);
        try {
            /* Stage = PNP */
            List<BigInteger> detailIds = findAndUpdateProcessForUpdate(type.detailTable, stage, bcStatus);
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
            log.debug("end PNP updateStatus:" + processApName + " type:" + type);
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
    private List<BigInteger> findAndUpdateProcessForUpdate(String detailTable, PnpStageEnum stage, PnpStatusEnum bcStatus) {
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
                .setParameter("stage", stage.value)
                .setParameter("status", PnpStatusEnum.PROCESS.value)
                .setParameter("bc_status", bcStatus.value)
                .setParameter("newStatus", PnpStatusEnum.SENDING.value)
                .setParameter("pnp_Status", PnpStatusEnum.PNP_SENDING.value)
                .setParameter("modifyTime", new Date())
                .getResultList();
    }

    /**
     * 找出第一個WAIT Main & STAGE = BC 並更新狀態為SENDING
     * 該WAIT MainID的 PnpDetail , status = WAIT  並更新狀態為SENDING
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public List<? super PnpDetail> updateStatusByStageBc(PnpFtpSourceEnum type, String procApName, Set<Long> allMainIds) {
        log.info(String.format("ProcessApName: %s, Type: %s", procApName, type));
        try {
            // 找出第一筆 WAIT MAIN 並更新狀態為Sending
            Long waitMainId = findAndUpdateFirstWaitMainByStageBc(procApName, type.mainTable);
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
    private List<BigInteger> findAndUpdateDetailByMainAndStatus(Set<Long> mainIds, PnpFtpSourceEnum type) {
        List<String> statusList = new ArrayList<>();
        statusList.add(PnpStatusEnum.FTP_MAIN_SAVE.value);
        Date modifyTime = Calendar.getInstance().getTime();
        String detailTable = type.detailTable;

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
                .setParameter("newStatus", PnpStatusEnum.SENDING.value)
                .setParameter("bc_status", PnpStatusEnum.BC_SENDING.value)
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
                .setParameter("status", PnpStatusEnum.FTP_MAIN_SAVE.value)
                .setParameter("stage", PnpStageEnum.BC.value)
                .setParameter("procApName", procApName)
                .setParameter("modifyTime", new Date())
                .setParameter("newStatus", PnpStatusEnum.BC_SENDING.value).getResultList();
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
    public PnpMain findMainByMainId(PnpFtpSourceEnum type, Long mainId) {
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
    public List<? super PnpDetail> findPnpDetailById(PnpFtpSourceEnum type, List<BigInteger> ids) {
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
