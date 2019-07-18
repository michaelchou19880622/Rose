package com.bcs.core.taishin.circle.PNP.db.repository;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
//import com.bcs.core.taishin.circle.billingNotice.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;
import com.bcs.core.taishin.circle.PNP.ftp.PNPFTPType;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.google.common.collect.Lists;

@Repository
public class PnpRepositoryCustomImpl implements PnpRepositoryCustom {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@PersistenceContext
	private EntityManager entityManager;
	/** Logger */
	private static Logger logger = Logger.getLogger(PnpRepositoryCustomImpl.class);
	
	
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	public void batchInsertPnpDetailMitake(final List<PnpDetailMitake> list) {

		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
		String INSERT = "INSERT INTO BCS_PNP_DETAIL_MITAKE" + 
				"(CREAT_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME, PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME,"
				+ " [SOURCE],STATUS,  UID, LINE_PUSH_TIME, DEST_CATEGORY, DEST_NAME)" + 
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);" ;
		logger.info("batchInsertPnpDetailMitake start");
		List<List<PnpDetailMitake>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
		 for(final List<PnpDetailMitake> batch : batchLists) {  
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
						ps.setTimestamp(10, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null );
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
	
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	public void batchInsertPnpDetailMing(final List<PnpDetailMing> list) {

		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
		String INSERT = "INSERT INTO BCS_PNP_DETAIL_MING" + 
				"(CREAT_TIME, LINE_PUSH_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME, PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME, [SOURCE],"
				+ " STATUS, UID, SN, VARIABLE1, VARIABLE2, ACCOUNT1, ACCOUNT2, DETAIL_SCHEDULE_TIME, KEEP_SECOND)" + 
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		logger.info("batchInsertPnpDetailMing start");
		List<List<PnpDetailMing>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
		 for(final List<PnpDetailMing> batch : batchLists) {  
			 jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						PnpDetailMing detail = batch.get(i);
						//logger.info("batchInsertPnpDetailMing detail:" + detail);
						
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
						ps.setTimestamp(11, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null );
						ps.setTimestamp(12, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
						ps.setString(13, detail.getSource());
						ps.setString(14, detail.getStatus());
						ps.setString(15, detail.getUid());
						ps.setString(16, detail.getSN());
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
		 for(final List<PnpDetailUnica> batch : batchLists) {  
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
						ps.setTimestamp(10, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null );
						ps.setTimestamp(11, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
						ps.setString(12, detail.getSource());
						ps.setString(13, detail.getStatus());
						ps.setString(14, detail.getUid());
						ps.setTimestamp(15, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
						ps.setString(16, detail.getCampaignID());
						ps.setString(17, detail.getDestName());
						ps.setString(18, detail.getPID());
						ps.setString(19, detail.getProgramID());
						ps.setString(20, detail.getSN());
						ps.setString(21, detail.getSegmentID());
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
	
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	public void batchInsertPnpDetailEvery8d(final List<PnpDetailEvery8d> list) {

		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
		String INSERT = "INSERT INTO BCS_PNP_DETAIL_EVERY8D" + 
				"(CREAT_TIME, MODIFY_TIME, MSG, PHONE, PHONE_HASH, PNP_MAIN_ID, PNP_TIME, PROC_FLOW, PROC_STAGE, SEND_TIME, SMS_TIME, [SOURCE], STATUS, UID,LINE_PUSH_TIME, CAMPAIGN_ID,"
				+ " DEST_NAME, PID, PROGRAM_ID, SN, SEGMENT_ID, VARIABLE1, VARIABLE2)" + 
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);" ;
		logger.info("batchInserPnpDetailEvery8d start");
		List<List<PnpDetailEvery8d>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
		 for(final List<PnpDetailEvery8d> batch : batchLists) {  
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
						ps.setTimestamp(10, detail.getSendTime() != null ? new Timestamp(detail.getSendTime().getTime()) : null );
						ps.setTimestamp(11, detail.getSmsTime() != null ? new Timestamp(detail.getSmsTime().getTime()) : null);
						ps.setString(12, detail.getSource());
						ps.setString(13, detail.getStatus());
						ps.setString(14, detail.getUid());
						ps.setTimestamp(15, detail.getLinePushTime() != null ? new Timestamp(detail.getLinePushTime().getTime()) : null);
						ps.setString(16, detail.getCampaignID());
						ps.setString(17, detail.getDestName());
						ps.setString(18, detail.getPID());
						ps.setString(19, detail.getProgramID());
						ps.setString(20, detail.getSN());
						ps.setString(21, detail.getSegmentID());
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
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
	public List<? super PnpDetail>  updateStatus(PNPFTPType type , String procApName, String stage) {
		logger.debug(" begin PNP updateStatus:" + procApName + " type:" + type);
		try {
			List<BigInteger>  detailIds = findAndUpdateProcessForUpdate(type.getDetailTable(), stage);
			if (!detailIds.isEmpty()) {
				List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
				for (List<BigInteger> ids : batchDetailIds) {
					 List<? super PnpDetail> details = findPnpDetailById(type, ids);
					if (!details.isEmpty()) {
						return details;
					}
				}
			}else {
				logger.info( "stage:" + stage + " PNP updateStatus:" + procApName + " type:" + type + " detailIds isEmpty");
			}
			logger.debug(" end PNP updateStatus:" + procApName + " type:" + type);
		}catch(Exception e) {
			logger.error(e);
			throw e;
		}
		
		return new ArrayList<>();
	}
	
	/**
	 * 找出第一筆PNP detail (status = PROCESS and stage = 傳入參數)的 mainId
	 * 並更新PNP detail 的 mainId 等於上述的值且status = PROCESS and stage = 傳入參數 者 更新 status = SENDING
	 */
	@SuppressWarnings("unchecked")
	private List<BigInteger> findAndUpdateProcessForUpdate(String detailTable, String stage) {
		Date  now = Calendar.getInstance().getTime();
		String sqlString = "select  d.PNP_DETAIL_ID from " + detailTable + " d  "
				 + " where d.STATUS = :status and d.PROC_STAGE = :stage AND d.PNP_MAIN_ID  IN (select TOP 1 a.PNP_MAIN_ID from " + detailTable + " a " 
				 + " where a.STATUS = :status and a. PROC_STAGE = :stage Order by a.CREAT_TIME) "
				 + "update " + detailTable + "  set STATUS = :newStatus  , MODIFY_TIME = :modifyTime "
				 + " where STATUS = :status AND PROC_STAGE = :stage AND PNP_MAIN_ID  IN (select TOP 1 a.PNP_MAIN_ID from " + detailTable + "  a WITH(ROWLOCK)  "
				 + " where a.STATUS = :status and a. PROC_STAGE = :stage  Order by a.CREAT_TIME)  ";
		List<BigInteger> ids = (List<BigInteger>)entityManager.createNativeQuery(sqlString)
				.setParameter("stage", stage)
				.setParameter("status", AbstractPnpMainEntity.MSG_SENDER_STATUS_PROCESS)
				.setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING)
				.setParameter("modifyTime", now)
				.getResultList();
		
		return ids;
	}
	
	
	/**
	 * 找出BCS_PNP_DETAIL_X 第一筆 STATUS = 'CHECK_DELIVERY' AND PROC_STAGE = 'PNP'的MainId 
	 * 用此mainId 去找所有同樣MainId AND STATUS = 'CHECK_DELIVERY' AND PROC_STAGE = 'PNP' 的BCS_PNP_DETAIL_X 並更新STATUS
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
	public List<? super PnpDetail>  updateDelivertExpiredStatus(PNPFTPType type , String procApName, String stage) {
		logger.debug(" begin PNP updateDelivertExpiredStatus:" + procApName + " type:" + type);
		try {
			List<BigInteger>  detailIds = findAndUpdateDeliveryExpiredForUpdate(type.getDetailTable(), stage);
			if (!detailIds.isEmpty()) {
				List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
				for (List<BigInteger> ids : batchDetailIds) {
					 List<? super PnpDetail> details = findPnpDetailById(type, ids);
					if (!details.isEmpty()) {
						return details;
					}
				}
			}else {
				logger.info( "stage:" + stage + " PNP updateDelivertExpiredStatus:" + procApName + " type:" + type + " detailIds isEmpty");
			}
			logger.debug(" end PNP updateDelivertExpiredStatus:" + procApName + " type:" + type);
		}catch(Exception e) {
			logger.error(e);
			throw e;
		}
		
		return new ArrayList<>();
	}
	
	/**
	 * 找出第一筆PNP detail (status = CHECK_DELIVERY and stage = 傳入參數)的 mainId
	 * 並更新PNP detail 的 mainId 等於上述的值且status = CHECK_DELIVERY and stage = 傳入參數 者 更新PROC_STAGE = SMS , status = SENDING
	 */
	@SuppressWarnings("unchecked")
	private List<BigInteger> findAndUpdateDeliveryExpiredForUpdate(String detailTable, String stage) {
		Date  now = Calendar.getInstance().getTime();
		String sqlString = "select  d.PNP_DETAIL_ID from " + detailTable + " d  "
				 + " where d.STATUS = 'CHECK_DELIVERY' and d.PNP_DELIVERY_EXPIRE_TIME < getdate() "
				 + " AND d.PNP_MAIN_ID  IN (select TOP 1 a.PNP_MAIN_ID from " + detailTable + " a " 
				 + " where a.STATUS = 'CHECK_DELIVERY' and a. PNP_DELIVERY_EXPIRE_TIME < getdate() Order by a.CREAT_TIME) "
				 + "update " + detailTable + "  set PROC_STAGE = 'SMS' , STATUS = :newStatus  , MODIFY_TIME = :modifyTime "
				 + " where STATUS = 'CHECK_DELIVERY' AND PNP_DELIVERY_EXPIRE_TIME < getdate() AND PNP_MAIN_ID  IN (select TOP 1 a.PNP_MAIN_ID from " + detailTable + "  a WITH(ROWLOCK)  "
				 + " where a.STATUS = 'CHECK_DELIVERY' and a. PNP_DELIVERY_EXPIRE_TIME < getdate()  Order by a.CREAT_TIME)  ";
		List<BigInteger> ids = (List<BigInteger>)entityManager.createNativeQuery(sqlString)
				.setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING)
				.setParameter("modifyTime", now)
				.getResultList();
		
		return ids;
	}
	
	
	
	
	/**
	 * 移至com.bcs.core.bot.db.repository.MsgBotReceiveRepositoryImpl
	 */
//	@Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
//	public void updateStatus(String deliveryTags) {
//		logger.info("received PNP delivery : "+ deliveryTags +" updateStatu to pnp send completed!");
//		try {
//			String[] deliveryData = deliveryTags.split("\\;;", 5);
//			String source = deliveryData[1];
//			String mainId = deliveryData[2];
//			String detailId = deliveryData[3];
//			String hashPhone = deliveryData[4];
//			
//			String detailTable="";
//			switch (source) {
//				case AbstractPnpMainEntity.SOURCE_MITAKE:
//					detailTable = PNPFTPType.MITAKE.getDetailTable();
//					break;
//				case AbstractPnpMainEntity.SOURCE_EVERY8D:
//					detailTable = PNPFTPType.EVERY8D.getDetailTable();
//					break;
//				case AbstractPnpMainEntity.SOURCE_MING:
//					detailTable = PNPFTPType.MING.getDetailTable();
//					break;
//				case AbstractPnpMainEntity.SOURCE_UNICA:
//					detailTable = PNPFTPType.UNICA.getDetailTable();
//					break;
//			}
//			
//			Date  now = Calendar.getInstance().getTime();
//			String sqlString = 
//					 "update " + detailTable + "  set STATUS = :newStatus  , MODIFY_TIME = :modifyTime ,PNP_DELIVERY_TIME = :deliveryTime"
//					 + " where PNP_MAIN_ID =:mainId AND PNP_DETAIL_ID =:detailId";
//			List<BigInteger> ids = (List<BigInteger>)entityManager.createNativeQuery(sqlString)
//					.setParameter("mainId", mainId)
//					.setParameter("detailId", detailId)
//					.setParameter("newStatus", AbstractPnpMainEntity.DATA_CONVERTER_STATUS_COMPLETE)
//					.setParameter("modifyTime", now)
//					.setParameter("deliveryTime", now)
//					.getResultList();
//			
//		}catch(Exception e) {
//			logger.error(e);
//			throw e;
//		}
//		
//	}
	

	/**
	 * 找出第一個WAIT Main & STAGE = BC 並更新狀態為SENDING
	 * 該WAIT MainID的 PnpDetail , status = WAIT  並更新狀態為SENDING
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000, propagation = Propagation.REQUIRES_NEW)
	public List<? super PnpDetail> updateStatusByStageBC(PNPFTPType type, String procApName, Set<Long>  allMainIds) {
		logger.debug(" begin updateStatusByStageBC:" + procApName + " type:" + type);
		try {
			// 找出第一筆 WAIT MAIN 並更新狀態
			Long waitMainId = findAndUpdateFirstWaitMainByStageBC(procApName, type.getMainTable() );
			if (waitMainId != null) {
				allMainIds.add(waitMainId);
			}else {
				logger.info("updateStatusByStageBC waitMainId is null" + " type:" + type);
			}
			logger.debug("updateStatusByStageBC allMainIds:" + allMainIds + " type:" + type);
			if (!allMainIds.isEmpty()) {
				//  根據MAIN_ID 更新 Detail
				List<BigInteger>  detailIds = findAndUpdateDetailByMainAndStatus(allMainIds, type);
				if (!detailIds.isEmpty()) {
					List<List<BigInteger>> batchDetailIds = Lists.partition(detailIds, CircleEntityManagerControl.batchSize);
					for (List<BigInteger> ids : batchDetailIds) {
						 return findPnpDetailById(type, ids);
					}
				}
			}else {
				logger.info("updateStatusByStageBC:" + procApName + " type:" + type + " allMainIds isEmpty");
			}
			logger.debug(" end updateStatusByStageBC:" + procApName + " type:" + type);
		}catch(Exception e) {
			logger.error(e);
			throw e;
		}
		return new ArrayList<>();
	}
	
	/**
	 *  根據Main ID && STATUS = WAIT 找出PNP Detail 並更新 status 為SENDING
	 * @param mainIds
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<BigInteger> findAndUpdateDetailByMainAndStatus( Set<Long> mainIds, PNPFTPType type) {
		List<String>  statusList = new ArrayList<String>();
		statusList.add(AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT);
		Date  modifyTime = Calendar.getInstance().getTime();
		String detailTable = type.getDetailTable();
		
		String sqlString = "select  b.PNP_DETAIL_ID from " + detailTable + " b where  b.PNP_MAIN_ID in (:mainIds) and b.STATUS in (:status)  "
				+ "update " + detailTable + "  set STATUS = :newStatus , MODIFY_TIME = :modifyTime  where PNP_DETAIL_ID  IN "
				+ "	(select d.PNP_DETAIL_ID from " + detailTable + " d WITH(ROWLOCK) where  d.PNP_MAIN_ID in (:mainIds) and d.STATUS in (:status) )  ";
		List<BigInteger> details = (List<BigInteger>)entityManager.createNativeQuery(sqlString)
		.setParameter("status", statusList).setParameter("mainIds", mainIds)
		.setParameter("modifyTime", modifyTime)
		.setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING).getResultList();
		
		return details;
	}

	
	/**
	 * 找出第一個status = WAIT & STAGE = BC 的PNP Main 並更新status = SENDING
	 */
	@SuppressWarnings("unchecked")
	private Long findAndUpdateFirstWaitMainByStageBC(String procApName, String mainTable) {
		
		Date  modifyTime = Calendar.getInstance().getTime();
		String waitMainString = "select  TOP 1 m.PNP_MAIN_ID from  " + mainTable + " m  "
				+ "where m.STATUS = :status and m.PROC_STAGE in (:stage) Order by m.CREAT_TIME "
				 + "update " + mainTable + "  set STATUS = :newStatus , PROC_AP_NAME = :procApName , MODIFY_TIME = :modifyTime "
				 + "   where PNP_MAIN_ID  IN (select TOP 1 a.PNP_MAIN_ID from " + mainTable + " a WITH(ROWLOCK) "
				 + "		where a.STATUS = :status and a.PROC_STAGE in (:stage) Order by a.CREAT_TIME)  ";
		List<BigInteger> mains = (List<BigInteger>)entityManager.createNativeQuery(waitMainString)
		.setParameter("status", AbstractPnpMainEntity.DATA_CONVERTER_STATUS_WAIT)
		.setParameter("stage", AbstractPnpMainEntity.STAGE_BC)
		.setParameter("procApName", procApName)
		.setParameter("modifyTime", modifyTime)
		.setParameter("newStatus", AbstractPnpMainEntity.MSG_SENDER_STATUS_SENDING).getResultList();
		if (mains != null && !mains.isEmpty()) {
			return mains.get(0).longValue();
	    }
		
		return null;
	}
	
	/**
	 * 根據mainId 找出物件
	 */
	public PnpMain  findMainByMainId(PNPFTPType type ,Long mainId){
		
		if (type.equals(PNPFTPType.MITAKE)) {
			return findMainByMainIdMitake(mainId);
		}else if (type.equals(PNPFTPType.MING)) {
			return findMainByMainIdMing(mainId);
		}else if (type.equals(PNPFTPType.EVERY8D)) {
			return findMainByMainIdEvery8d(mainId);
		}else if (type.equals(PNPFTPType.UNICA)) {
			return findMainByMainIdUnica(mainId);
		}
		
		return null;
	}
	
	/**
	 * 找出PnpMainMitake
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private PnpMainMitake findMainByMainIdMitake(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_MITAKE m  where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainMitake> mains = entityManager.createNativeQuery(sqlString, PnpMainMitake.class)
				.setParameter("mainId", mainId)
				.getResultList();
		
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainMitake mainItem : mains) {
				return mainItem;
			}
		}
		
		return null;
	}
	
	/**
	 * 找出PnpMainEvery8d
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private PnpMainEvery8d findMainByMainIdEvery8d(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_EVERY8D m  "
				+ "where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainEvery8d> mains = entityManager.createNativeQuery(sqlString, PnpMainEvery8d.class)
		.setParameter("mainId", mainId)
		.getResultList();
		
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainEvery8d mainItem : mains) {
				return mainItem;
			}
	    }

		return null;
	}
	
	/**
	 * 找出PnpMainUnica
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private PnpMainUnica findMainByMainIdUnica(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_UNICA m  "
				+ "where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainUnica> mains = entityManager.createNativeQuery(sqlString, PnpMainUnica.class)
				.setParameter("mainId", mainId)
				.getResultList();
		
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainUnica mainItem : mains) {
				return mainItem;
			}
		}
		
		return null;
	}
	
	/**
	 * 找出PnpMainMing
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private PnpMainMing findMainByMainIdMing(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_MING m  "
				+ "where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainMing> mains = entityManager.createNativeQuery(sqlString, PnpMainMing.class)
				.setParameter("mainId", mainId)
				.getResultList();
		
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainMing mainItem : mains) {
				return mainItem;
			}
		}
		
		return null;
	}
	

	/**
	 * find PNP detail by Ids and PNPFTPType
	 * @param type
	 * @param ids
	 * @return
	 */
	private List<? super PnpDetail> findPnpDetailById(PNPFTPType type, List<BigInteger> ids) {
		if (type.equals(PNPFTPType.MITAKE)) {
			return findPnpDetailMitake(ids);
		}else if (type.equals(PNPFTPType.MING)) {
			return findPnpDetailMing(ids);
		}else if (type.equals(PNPFTPType.EVERY8D)) {
			return findPnpDetailEvery8d(ids);
		}else if (type.equals(PNPFTPType.UNICA)) {
			return findPnpDetailUnica(ids);
		}
		return new ArrayList<>();
	}
	
	/**
	 * 找出PnpDetailUnica
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private List<? super PnpDetail> findPnpDetailUnica(List<BigInteger> ids) {
		
		String sqlString = "select  * from BCS_PNP_DETAIL_UNICA   WHERE PNP_DETAIL_ID in (:ids) ";
		return entityManager.createNativeQuery(sqlString, PnpDetailUnica.class)
				.setParameter("ids", ids)
				.getResultList();
	}
	
	/**
	 * 找出PnpDetailEvery8d
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private List<? super PnpDetail> findPnpDetailEvery8d(List<BigInteger> ids) {
		
		String sqlString = "select  * from BCS_PNP_DETAIL_EVERY8D   WHERE PNP_DETAIL_ID in (:ids) ";
		return entityManager.createNativeQuery(sqlString, PnpDetailEvery8d.class)
				.setParameter("ids", ids)
				.getResultList();
	}
	
	/**
	 * 找出PnpDetailMing
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private List<? super PnpDetail> findPnpDetailMing(List<BigInteger> ids) {
		
		String sqlString = "select  * from BCS_PNP_DETAIL_MING   WHERE PNP_DETAIL_ID in (:ids) ";
		return entityManager.createNativeQuery(sqlString, PnpDetailMing.class)
				.setParameter("ids", ids)
				.getResultList();
	}
	
	/**
	 * 找出PnpDetailMitake
	 * @param ids
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	private List<? super PnpDetail> findPnpDetailMitake(List<BigInteger> ids) {
		
		String sqlString = "select  * from BCS_PNP_DETAIL_MITAKE   WHERE PNP_DETAIL_ID in (:ids) ";
		return entityManager.createNativeQuery(sqlString, PnpDetailMitake.class)
				.setParameter("ids", ids)
				.getResultList();
	}
	

}
