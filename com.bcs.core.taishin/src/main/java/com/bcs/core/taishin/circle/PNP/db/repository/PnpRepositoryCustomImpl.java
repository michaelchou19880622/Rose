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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author ???
 */
@Repository
public class PnpRepositoryCustomImpl implements PnpRepositoryCustom {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @PersistenceContext
    private EntityManager entityManager;
    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(PnpRepositoryCustomImpl.class);


    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000)
    public void batchInsertPnpDetailMitake(final List<PnpDetailMitake> list) {

        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
        String INSERT = "INSERT INTO BCS_PNP_DETAIL_MITAKE" +
                "(CREAT_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME, PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME,"
                + " [SOURCE],STATUS,  UID, LINE_PUSH_TIME, DEST_CATEGORY, DEST_NAME)" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        logger.info("batchInsertPnpDetailMitake start");
        List<List<PnpDetailMitake>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
        for (final List<PnpDetailMitake> batch : batchLists) {
            jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PnpDetailMitake detail = batch.get(i);
                    ps.setTimestamp(1, now);
                    ps.setTimestamp(2, now);
                    ps.setString(3, detail.getMsg());
                    ps.setString(4, detail.getPhone());
                    ps.setString(5, detail.getPhoneHash());
                    ps.setLong(6, detail.getPnpMainId());
                    ps.setTimestamp(7, detail.getPnpTime() != null ? new Timestamp(detail.getPnpTime().getTime()) : null);
                    ps.setString(8, detail.getProcFlow());
                    ps.setString(9, detail.getProcStage());
                    ps.setTimestamp(10, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null);
                    ps.setTimestamp(11, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
                    ps.setString(12, detail.getSource());
                    ps.setString(13, detail.getStatus());
                    ps.setString(14, detail.getUid());
                    ps.setTimestamp(15, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
                    ps.setString(16, detail.getDestCategory());
                    ps.setString(17, detail.getDestName());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
        logger.info("batchInsertPnpDetailMitake end");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000)
    public void batchInsertPnpDetailMing(final List<PnpDetailMing> list) {

        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
        String INSERT = "INSERT INTO BCS_PNP_DETAIL_MING" +
                " (CREAT_TIME, LINE_PUSH_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME, PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME, [SOURCE]," +
                " STATUS, UID, SN, VARIABLE1, VARIABLE2, ACCOUNT1, ACCOUNT2, DETAIL_SCHEDULE_TIME, KEEP_SECOND)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        logger.info("batchInsertPnpDetailMing start");
        List<List<PnpDetailMing>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
        for (final List<PnpDetailMing> batch : batchLists) {
            jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PnpDetailMing detail = batch.get(i);
                    ps.setTimestamp(1, now);
                    ps.setTimestamp(2, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
                    ps.setTimestamp(3, now);
                    ps.setString(4, detail.getMsg());
                    ps.setString(5, detail.getPhone());
                    ps.setString(6, detail.getPhoneHash());
                    ps.setLong(7, detail.getPnpMainId());
                    ps.setTimestamp(8, detail.getPnpTime() != null ? new Timestamp(detail.getPnpTime().getTime()) : null);
                    ps.setString(9, detail.getProcFlow());
                    ps.setString(10, detail.getProcStage());
                    ps.setTimestamp(11, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null);
                    ps.setTimestamp(12, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
                    ps.setString(13, detail.getSource());
                    ps.setString(14, detail.getStatus());
                    ps.setString(15, detail.getUid());
                    ps.setString(16, detail.getSn());
                    ps.setString(17, detail.getVariable1());
                    ps.setString(18, detail.getVariable2());
                    ps.setString(19, detail.getAccount1());
                    ps.setString(20, detail.getAccount2());
                    ps.setString(21, detail.getDetailScheduleTime());
                    ps.setString(22, detail.getKeepSecond());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
        logger.info("batchInsertPnpDetailMing end");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000)
    public void batchInsertPnpDetailUnica(final List<PnpDetailUnica> list) {

        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
        String INSERT = "INSERT INTO BCS_PNP_DETAIL_UNICA" +
                "(CREAT_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME,  PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME, [SOURCE], "
                + "STATUS, UID, LINE_PUSH_TIME, CAMPAIGN_ID, DEST_NAME, PID, PROGRAM_ID, SN, SEGMENT_ID, VARIABLE1, VARIABLE2)" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        logger.info("batchInserPnpDetailUnica start");
        List<List<PnpDetailUnica>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
        for (final List<PnpDetailUnica> batch : batchLists) {
            jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PnpDetailUnica detail = batch.get(i);
                    ps.setTimestamp(1, now);
                    ps.setTimestamp(2, now);
                    ps.setString(3, detail.getMsg());
                    ps.setString(4, detail.getPhone());
                    ps.setString(5, detail.getPhoneHash());
                    ps.setLong(6, detail.getPnpMainId());
                    ps.setTimestamp(7, detail.getPnpTime() != null ? new Timestamp(detail.getPnpTime().getTime()) : null);
                    ps.setString(8, detail.getProcFlow());
                    ps.setString(9, detail.getProcStage());
                    ps.setTimestamp(10, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null);
                    ps.setTimestamp(11, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
                    ps.setString(12, detail.getSource());
                    ps.setString(13, detail.getStatus());
                    ps.setString(14, detail.getUid());
                    ps.setTimestamp(15, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
                    ps.setString(16, detail.getCampaignId());
                    ps.setString(17, detail.getDestName());
                    ps.setString(18, detail.getPid());
                    ps.setString(19, detail.getProgramId());
                    ps.setString(20, detail.getSn());
                    ps.setString(21, detail.getSegmentId());
                    ps.setString(22, detail.getVariable1());
                    ps.setString(23, detail.getVariable2());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
        logger.info("batchInsertPnpDetailUnica end");
    }

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000)
    public void batchInsertPnpDetailEvery8d(final List<PnpDetailEvery8d> list) {

        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
        String INSERT = "INSERT INTO BCS_PNP_DETAIL_EVERY8D" +
                "(CREAT_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME, PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME, [SOURCE], STATUS, UID,LINE_PUSH_TIME, CAMPAIGN_ID,"
                + " DEST_NAME, PID, PROGRAM_ID, SN, SEGMENT_ID, VARIABLE1, VARIABLE2)" +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        logger.info("batchInserPnpDetailEvery8d start");
        List<List<PnpDetailEvery8d>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
        for (final List<PnpDetailEvery8d> batch : batchLists) {
            jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PnpDetailEvery8d detail = batch.get(i);
                    ps.setTimestamp(1, now);
                    ps.setTimestamp(2, now);
                    ps.setString(3, detail.getMsg());
                    ps.setString(4, detail.getPhone());
                    ps.setString(5, detail.getPhoneHash());
                    ps.setLong(6, detail.getPnpMainId());
                    ps.setTimestamp(7, detail.getPnpTime() != null ? new Timestamp(detail.getPnpTime().getTime()) : null);
                    ps.setString(8, detail.getProcFlow());
                    ps.setString(9, detail.getProcStage());
                    ps.setTimestamp(10, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null);
                    ps.setTimestamp(11, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
                    ps.setString(12, detail.getSource());
                    ps.setString(13, detail.getStatus());
                    ps.setString(14, detail.getUid());
                    ps.setTimestamp(15, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
                    ps.setString(16, detail.getCampaignId());
                    ps.setString(17, detail.getDestName());
                    ps.setString(18, detail.getPid());
                    ps.setString(19, detail.getProgramId());
                    ps.setString(20, detail.getSn());
                    ps.setString(21, detail.getSegmentId());
                    ps.setString(22, detail.getVariable1());
                    ps.setString(23, detail.getVariable2());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
        logger.info("batchInsertPnpDetailEvery8d end");
    }

    /**
     * 找出BCS_PNP_DETAIL_X 第一筆 STATUS = 'PROCESS' AND PROC_STAGE = 'PNP' or 'SMS' 的MainId
     * 用此mainId 去找所有同樣MainId AND STATUS = 'PROCESS' AND PROC_STAGE = 'PNP'  or 'SMS' 的BCS_PNP_DETAIL_X 並更新STATUS
     *
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpPNPMsgService#sendingPnpMain()
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public List<? super PnpDetail> updateStatus(PNPFTPType type, String processApName, String stage) {
        logger.debug(" begin PNP updateStatus:" + processApName + " type:" + type);
        try {
            /* Stage = PNP */
            List<BigInteger> detailIds = findAndUpdateProcessForUpdate(type.getDetailTable(), stage);
            if (!detailIds.isEmpty()) {
                logger.info("Update Detail Status: [PROCESS] to [PNP][SENDING]");
                List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
                for (List<BigInteger> ids : batchDetailIds) {

                    List<? super PnpDetail> details = findPnpDetailById(type, ids);
                    if (!details.isEmpty()) {
                        return details;
                    }
                }
            } else {
                logger.info("stage:" + stage + " PNP updateStatus:" + processApName + " type:" + type + " detailIds isEmpty");
            }
            logger.debug(" end PNP updateStatus:" + processApName + " type:" + type);
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
        return Collections.emptyList();
    }

    /**
     * 找出BCS_PNP_DETAIL_X 第一筆 STATUS = 'PROCESS' AND PROC_STAGE = 'PNP' or 'SMS' 的MainId
     * 用此mainId 去找所有同樣MainId AND STATUS = 'PROCESS' AND PROC_STAGE = 'PNP'  or 'SMS' 的BCS_PNP_DETAIL_X 並更新STATUS
     *
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpSMSMsgService#sendingSmsMain()
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public List<? super PnpDetail> updateStatusForSms(PNPFTPType type, String processApName, String stage) {
        logger.debug(" begin PNP updateStatus:" + processApName + " type:" + type);
        try {
            /* Stage = SMS */
            List<BigInteger> detailIds = findAndUpdateProcessForUpdateForSms(type.getDetailTable(), stage);
            if (!detailIds.isEmpty()) {
                logger.info("Update Detail Status: [PROCESS] to [SMS][SENDING]");
                List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
                for (List<BigInteger> ids : batchDetailIds) {

                    List<? super PnpDetail> details = findPnpDetailById(type, ids);
                    if (!details.isEmpty()) {
                        return details;
                    }
                }
            } else {
                logger.info("stage:" + stage + " PNP updateStatus:" + processApName + " type:" + type + " detailIds isEmpty");
            }
            logger.debug(" end PNP updateStatus:" + processApName + " type:" + type);
        } catch (Exception e) {
            logger.error(e);
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
    @SuppressWarnings("unchecked")
    private List<BigInteger> findAndUpdateProcessForUpdateForSms(String detailTable, String stage) {
        String sql = String.format(
                " SELECT d.PNP_DETAIL_ID FROM %s d" +
                        " WHERE 1=1" +
                        "    AND d.PROC_STAGE = :stage" +
                        "    AND d.PNP_MAIN_ID in" +
                        "    (" +
                        "        SELECT top 1 a.PNP_MAIN_ID FROM %s a" +
                        "        WHERE 1=1" +
                        "            AND a.BC_STATUS = :bc_status" +
                        "            AND a.PROC_STAGE = :stage" +
                        "        ORDER BY a.CREAT_TIME" +
                        "    )" +
                        " UPDATE %s SET" +
                        "    STATUS = :status," +
                        "    SMS_STATUS = :sms_Status," +
                        "    MODIFY_TIME = :modifyTime" +
                        " WHERE 1=1" +
                        "    AND PROC_STAGE = :stage" +
                        "    AND PNP_MAIN_ID in(" +
                        "        SELECT top 1 a.PNP_MAIN_ID FROM %s a WITH (ROWLOCK)" +
                        "        WHERE 1=1" +
                        "            AND a.BC_STATUS = :bc_status" +
                        "            AND a.PROC_STAGE = :stage" +
                        "        ORDER BY a.CREAT_TIME" +
                        " )", detailTable, detailTable, detailTable, detailTable);
        return (List<BigInteger>) entityManager.createNativeQuery(sql)
                .setParameter("stage", stage)
                .setParameter("status", AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS)
                .setParameter("bc_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_BC_FAIL_SMS_PROCESS)
                .setParameter("sms_Status", AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_SENDING)
                .setParameter("modifyTime", new Date())
                .getResultList();
    }


    /**
     * Find Detail Status is CheckDeliver and Change Status to SMS Sending
     *
     * @see com.bcs.core.taishin.circle.PNP.scheduler.PnpSMSMsgService
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public List<? super PnpDetail> updateDeliveryExpiredStatus(PNPFTPType type, String procApName, String stage) {
        logger.debug(" begin PNP updateDeliveryExpiredStatus:" + procApName + " type:" + type);
        try {
            List<BigInteger> detailIds = findAndUpdateDeliveryExpiredForUpdate(type.getDetailTable(), stage);
            if (!detailIds.isEmpty()) {
                logger.info("Update Status: [PNP][CHECK_DELIVERY] to [PNP][FAIL] and [SMS][SENDING]");
                List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
                for (List<BigInteger> ids : batchDetailIds) {
                    List<? super PnpDetail> details = findPnpDetailById(type, ids);
                    if (!details.isEmpty()) {
                        return details;
                    }
                }
            } else {
                logger.info("stage:" + stage + " PNP updateDeliveryExpiredStatus:" + procApName + " type:" + type + " detailIds isEmpty");
            }
            logger.debug(" end PNP updateDeliveryExpiredStatus:" + procApName + " type:" + type);
        } catch (Exception e) {
            logger.error(e);
            throw e;
        }
        return Collections.emptyList();
    }

    /**
     * Find Detail Status is CHECK_DELIVERY
     * Update Status to Sending
     *
     * @see this#updateDeliveryExpiredStatus
     */
    @SuppressWarnings("unchecked")
    private List<BigInteger> findAndUpdateDeliveryExpiredForUpdate(String detailTable, String stage) {
        String sql = String.format(
                " SELECT d.PNP_DETAIL_ID FROM %s d" +
                " WHERE 1=1" +
                "     AND d.STATUS = :CHECK_DELIVERY" +
                "     AND d.PNP_DELIVERY_EXPIRE_TIME <= :now" +
                "     AND d.PNP_MAIN_ID in" +
                "     (" +
                "        SELECT TOP 1 a.PNP_MAIN_ID FROM %s a" +
                "        WHERE 1=1" +
                "            AND a.STATUS = :CHECK_DELIVERY" +
                "            AND a.PNP_DELIVERY_EXPIRE_TIME <= :now" +
                "        ORDER BY a.CREAT_TIME" +
                "     )" +
                " UPDATE %s SET" +
                "     PROC_STAGE = :stage," +
                "     STATUS = :newStatus," +
                "     PNP_STATUS = :pnp_status," +
                "     SMS_STATUS = :sms_status," +
                "     MODIFY_TIME = :modifyTime" +
                " WHERE" +
                "     STATUS = :CHECK_DELIVERY" +
                "     AND PNP_DELIVERY_EXPIRE_TIME <= :now" +
                "     AND PNP_MAIN_ID in(" +
                "        SELECT TOP 1 a.PNP_MAIN_ID FROM %s a WITH (ROWLOCK)" +
                "        WHERE 1=1" +
                "            AND a.STATUS = :CHECK_DELIVERY" +
                "            AND a.PNP_DELIVERY_EXPIRE_TIME <= :now" +
                "        ORDER BY a.CREAT_TIME" +
                "     )", detailTable, detailTable ,detailTable, detailTable);
        return (List<BigInteger>) entityManager.createNativeQuery(sql)
                .setParameter("stage", stage)
                .setParameter("CHECK_DELIVERY", AbstractPnpMainEntity.MSG_SENDER_STATUS_CHECK_DELIVERY)
                .setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING)
                .setParameter("pnp_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_PNP_FAIL_SMS_PROCESS)
                .setParameter("sms_status", AbstractPnpMainEntity.MSG_SENDER_STATUS_SMS_SENDING)
                .setParameter("now", new Date())
                .setParameter("modifyTime", new Date())
                .getResultList();
    }

    /**
     * 找出第一個WAIT Main & STAGE = BC 並更新狀態為SENDING
     * 該WAIT MainID的 PnpDetail , status = WAIT  並更新狀態為SENDING
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
    public List<? super PnpDetail> updateStatusByStageBC(PNPFTPType type, String procApName, Set<Long> allMainIds) {
        logger.info(String.format("ProcessApName: %s, Type: %s", procApName, type));
        try {
            // 找出第一筆 WAIT MAIN 並更新狀態為Sending
            Long waitMainId = findAndUpdateFirstWaitMainByStageBc(procApName, type.getMainTable());
            if (waitMainId != null) {
                allMainIds.add(waitMainId);
            } else {
                logger.info(String.format("Type %s Main Id Not Found with Status is Wait.", type));
                return Collections.emptyList();
            }

            if (!allMainIds.isEmpty()) {
                logger.info(String.format("All BC Sending Type [%s] Mid List: %s", type, allMainIds.toString()));
                //  根據MAIN_ID 更新 Detail
                List<BigInteger> detailIds = findAndUpdateDetailByMainAndStatus(allMainIds, type);
                if (!detailIds.isEmpty()) {
                    logger.info("Update Detail Status: [WAIT] to [BC][SENDING]");
                    List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
                    return findPnpDetailById(type, batchDetailIds.get(0));
                }
            } else {
                logger.info(String.format("All BC Sending Type [%s] Mid List: is Empty!!", type));
            }
        } catch (Exception e) {
            logger.error(e);
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

//        String sqlString = "select  b.PNP_DETAIL_ID from " + detailTable + " b where  b.PNP_MAIN_ID in (:mainIds) and b.STATUS in (:status)  "
//                + "update " + detailTable + "  set STATUS = :newStatus , MODIFY_TIME = :modifyTime  where PNP_DETAIL_ID  IN "
//                + "	(select d.PNP_DETAIL_ID from " + detailTable + " d WITH(ROWLOCK) where  d.PNP_MAIN_ID in (:mainIds) and d.STATUS in (:status) )  ";

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
        logger.info("Update Main Status: [WAIT] to [BC][SENDING]");
        return mains.get(0).longValue();
    }

    /**
     * 根據mainId 找出物件
     */
    @Override
    public PnpMain findMainByMainId(PNPFTPType type, Long mainId) {
        logger.info("Find Main By MainId : " + type);
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

        String sqlString = "select  m.* from BCS_PNP_MAIN_MITAKE m  where m.PNP_MAIN_ID = :mainId ";
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

        String sqlString = "select  m.* from BCS_PNP_MAIN_EVERY8D m  "
                + "where m.PNP_MAIN_ID = :mainId ";
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
    private List<? super PnpDetail> findPnpDetailById(PNPFTPType type, List<BigInteger> ids) {
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
        return entityManager.createNativeQuery(sqlString, PnpDetailUnica.class)
                .setParameter("ids", ids)
                .getResultList();
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
        return entityManager.createNativeQuery(sqlString, PnpDetailEvery8d.class)
                .setParameter("ids", ids)
                .getResultList();
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
        return entityManager.createNativeQuery(sqlString, PnpDetailMing.class)
                .setParameter("ids", ids)
                .getResultList();
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
        return entityManager.createNativeQuery(sqlString, PnpDetailMitake.class)
                .setParameter("ids", ids)
                .getResultList();
    }
}
