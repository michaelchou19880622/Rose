package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.db.service.EntityManagerProviderService;
import com.bcs.core.taishin.circle.pnp.code.PnpFtpSourceEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStageEnum;
import com.bcs.core.taishin.circle.pnp.code.PnpStatusEnum;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpMain;
import com.bcs.core.utils.DataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Alan
 */
@Slf4j(topic = "PnpRecorder")
@Repository
public class PnpRepositoryCustomImpl implements PnpRepositoryCustom {

    @Resource
    EntityManagerProviderService providerService;

    @Override
    public int checkIsAllSent(PnpFtpSourceEnum type, long mainId){
        String sqlString = "SELECT COUNT(*) FROM " + type.detailTable +
                " WHERE PNP_MAIN_ID=:MAIN_ID AND STATUS IN (:STATUS_LIST)";
        int notSendCount = providerService.getEntityManager().createNativeQuery(sqlString)
                .setParameter("MAIN_ID", mainId)
                .setParameter("STATUS_LIST", Arrays.asList(PnpStatusEnum.FTP_MAIN_SAVE.value, PnpStatusEnum.PROCESS.value, PnpStatusEnum.SENDING))
                .getFirstResult();
        log.info("Not send count: {}", notSendCount);
        return notSendCount;
    }
    /**
     * 找出最近一筆 PNP 訊息狀態為 PROCESS 的 ID
     * 並更新 PNP 訊息狀態為 SENDING
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public List<PnpDetail> findAllBcToPnpDetail(PnpFtpSourceEnum type, PnpStageEnum stage, PnpStatusEnum bcStatus) {
        /* Find all detail by main id */
        String sqlString = "SELECT TOP 1000 A.PNP_DETAIL_ID FROM " + type.detailTable + " A " +
                " WHERE A.STATUS=:STATUS" +
                " AND A.BC_STATUS=:BC_STATUS" +
                " AND A.PROC_STAGE=:STAGE" +
                " AND A.PNP_MAIN_ID IN " +
                " (" +
                "   SELECT TOP 1 B.PNP_MAIN_ID FROM " + type.detailTable + " B " +
                "   WHERE B.STATUS=:STATUS " +
                "   AND B.BC_STATUS=:BC_STATUS " +
                "   AND B.PROC_STAGE=:STAGE " +
                "   ORDER BY B.CREAT_TIME" +
                " ) ";
        List<BigInteger> detailIdList = providerService.getEntityManager().createNativeQuery(sqlString)
                .setParameter("STATUS", PnpStatusEnum.PROCESS.value)
                .setParameter("BC_STATUS", bcStatus.value)
                .setParameter("STAGE", stage.value)
                .getResultList();

        if (detailIdList == null || detailIdList.isEmpty()) {
            log.info("Select Detail is not found!!");
            return Collections.emptyList();
        }
        log.info("detail id list: {}", DataUtils.toPrettyJsonUseJackson(detailIdList));

        String updateSql = "UPDATE " + type.detailTable +
                " SET STATUS=:NEW_STATUS, PNP_STATUS=:PNP_STATUS, MODIFY_TIME=:MODIFY_TIME" +
                " WHERE PNP_MAIN_ID IN (:IDS)";
        providerService.getEntityManager().createNativeQuery(updateSql)
                .setParameter("NEW_STATUS", PnpStatusEnum.SENDING.value)
                .setParameter("PNP_STATUS", PnpStatusEnum.PNP_SENDING.value)
                .setParameter("MODIFY_TIME", new Date())
                .setParameter("IDS", detailIdList)
                .executeUpdate();

        String selectSql = "SELECT * FROM " + type.detailTable + " WHERE PNP_DETAIL_ID IN (:IDS)";
        return providerService.getEntityManager().createNativeQuery(selectSql, type.detailClass)
                .setParameter("IDS", detailIdList)
                .getResultList();
    }

    /**
     * Wait -> Sending
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public List<PnpDetail> findAllDetail(Long mainId, PnpFtpSourceEnum type) {
        String detailTable = type.detailTable;

        String sql = "SELECT PNP_DETAIL_ID FROM " + detailTable +
                " WHERE PNP_MAIN_ID=:MAIN_ID" +
                " AND STATUS IN (:STATUS_LIST)";

        List<BigInteger> detailIdList = (List<BigInteger>) providerService.getEntityManager().createNativeQuery(sql)
                .setParameter("MAIN_ID", mainId)
                .setParameter("STATUS_LIST", Arrays.asList(PnpStatusEnum.FTP_MAIN_SAVE.value, PnpStatusEnum.PROCESS.value))
                .getResultList();

        if (detailIdList.isEmpty()) {
            log.info("Detail id list is empty by mainId is {}", mainId);
            return Collections.emptyList();
        }
        log.info("Detail id list size: {}\n" +
                "value: {}", detailIdList.size(), DataUtils.toPrettyJsonUseJackson(detailIdList));

        String updateSql = "UPDATE " + detailTable +
                " SET STATUS=:NEW_STATUS," +
                " MODIFY_TIME=:MODIFY_TIME" +
                " WHERE PNP_DETAIL_ID IN(:IDS)" +
                " AND STATUS IN (:STATUS_LIST)";

        providerService.getEntityManager().createNativeQuery(updateSql)
                .setParameter("NEW_STATUS", PnpStatusEnum.SENDING.value)
                .setParameter("IDS", detailIdList)
                .setParameter("MODIFY_TIME", new Date())
                .setParameter("STATUS_LIST", Arrays.asList(PnpStatusEnum.FTP_MAIN_SAVE.value, PnpStatusEnum.PROCESS.value))
                .executeUpdate();

        log.info("Updated WAIT and PROCESS to SENDING!!");

        String selectSql = "SELECT * FROM " + detailTable + " WHERE PNP_DETAIL_ID IN (:ids)";

        return providerService.getEntityManager().createNativeQuery(selectSql, type.detailClass)
                .setParameter("ids", detailIdList)
                .getResultList();
    }


    /**
     * 找出第一個status = WAIT & STAGE = BC 的PNP Main 並更新status = SENDING
     */
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public List<PnpMain> findAllMain(String procApName, PnpFtpSourceEnum type) {
        String waitMainString = "SELECT TOP 100 PNP_MAIN_ID FROM " + type.mainTable +
                " WHERE STATUS NOT IN (:STATUS_LIST)" +
                " AND PROC_AP_NAME=:PROC_AP_NAME" +
                " ORDER BY CREAT_TIME";

        List<BigInteger> mainList = (List<BigInteger>) providerService.getEntityManager().createNativeQuery(waitMainString)
                .setParameter("STATUS_LIST", Arrays.asList(PnpStatusEnum.COMPLETE.value))
                .setParameter("PROC_AP_NAME", procApName)
                .getResultList();

        if (mainList == null || mainList.isEmpty()) {
            log.info("Main list is empty!!");
            return Collections.emptyList();
        }

        log.info("Main id list: {}", DataUtils.toPrettyJsonUseJackson(mainList));

        String updateSql = " UPDATE " + type.mainTable +
                " SET STATUS=:NEW_STATUS, MODIFY_TIME=:MODIFY_TIME " +
                " WHERE PNP_MAIN_ID IN (:IDS)";

        int i = providerService.getEntityManager().createNativeQuery(updateSql)
                .setParameter("MODIFY_TIME", new Date())
                .setParameter("IDS", mainList)
                .setParameter("NEW_STATUS", PnpStatusEnum.SENDING.value)
                .executeUpdate();

        if (i > 0) {
            log.info("Update Main Status: [WAIT] to [BC][SENDING]: {}", i);
        }

        String selectSql = "SELECT * FROM " + type.mainTable + " WHERE PNP_MAIN_ID IN (:ids)";

        return providerService.getEntityManager().createNativeQuery(selectSql, type.mainClass)
                .setParameter("ids", mainList)
                .getResultList();

    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(rollbackFor = Exception.class)
    public List<PnpMain> findMainById(PnpFtpSourceEnum type, Long mainId) {
        String sqlString = "select * from " + type.mainTable + " where PNP_MAIN_ID = :MAIN_ID";
        return providerService.getEntityManager().createNativeQuery(sqlString, type.mainClass)
                .setParameter("MAIN_ID", mainId)
                .getResultList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int countByStatusList(List<String> statusList, PnpFtpSourceEnum type, long mainId) {
        String selectSql = "SELECT COUNT(PNP_DETAIL_ID) FROM " + type.detailTable +
                " WHERE PNP_MAIN_ID=:MAIN_ID" +
                " AND STATUS IN (:STATUS_LIST)";

        return providerService.getEntityManager().createNativeQuery(selectSql)
                .setParameter("MAIN_ID", mainId)
                .setParameter("STATUS_LIST", statusList)
                .getFirstResult();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMainToComplete(long mainId, PnpFtpSourceEnum type, PnpStatusEnum status) {
        log.info("Update main to {}, main id is {}", status.value, mainId);
        String updateSql = " UPDATE " + type.mainTable +
                " SET STATUS=:NEW_STATUS, MODIFY_TIME=:MODIFY_TIME" +
                " WHERE PNP_MAIN_ID =:ID";

        return providerService.getEntityManager().createNativeQuery(updateSql)
                .setParameter("MODIFY_TIME", new Date())
                .setParameter("ID", mainId)
                .setParameter("NEW_STATUS", status.value)
                .executeUpdate();
    }
}
