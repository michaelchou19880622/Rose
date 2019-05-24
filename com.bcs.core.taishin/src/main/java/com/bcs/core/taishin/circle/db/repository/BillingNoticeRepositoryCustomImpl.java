package com.bcs.core.taishin.circle.db.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;
import com.bcs.core.taishin.circle.db.entity.CircleEntityManagerControl;
import com.google.common.collect.Lists;

@Repository
public class BillingNoticeRepositoryCustomImpl implements BillingNoticeRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BillingNoticeRepositoryCustomImpl.class);
	
	
	/**
	 * 批次新增BillingNoticeDetail
	 */
	@Transactional(rollbackFor = Exception.class, timeout = 3000)
	public void batchInsertBillingNoticeDetail(final List<BillingNoticeDetail> list) {

		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		final Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
		logger.info(" batchInsertBillingNoticeDetail start");
		String INSERT = "INSERT INTO BCS_BILLING_NOTICE_DETAIL " + 
				"(CREAT_TIME, MODIFY_TIME, MSG_TYPE, NOTICE_MAIN_ID,  STATUS, [TEXT], TITLE, UID)" + 
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
		
		int count = 0;
		List<List<BillingNoticeDetail>> batchLists = Lists.partition(list, CircleEntityManagerControl.batchSize);
		 for(final List<BillingNoticeDetail> batch : batchLists) {  
			 jdbcTemplate.batchUpdate(INSERT, new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						BillingNoticeDetail detail = batch.get(i);
						ps.setTimestamp(1, now);
						ps.setTimestamp(2, now);
						ps.setString(3, detail.getMsgType());
						ps.setLong(4, detail.getNoticeMainId());
						ps.setString(5, detail.getStatus());
						ps.setString(6, detail.getText());
						ps.setString(7, detail.getTitle());
						ps.setString(8, detail.getUid());
					}
					@Override
					public int getBatchSize() {
						return batch.size();
					}
				});
			 count++;
			 logger.info(" batchInsertBillingNoticeDetail batch:" + count);
		 }
		logger.info(" batchInsertBillingNoticeDetail end");
	}
	
	/**
	 * 找出第一個retry detail 準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public BillingNoticeDetail findFirstDetailByStatusForUpdate(String status, List<String> tempIds) {		
		String sqlString = "select  b.* from BCS_BILLING_NOTICE_DETAIL b, BCS_BILLING_NOTICE_MAIN m  "
				+ "where  m.NOTICE_MAIN_ID = b.NOTICE_MAIN_ID  and b.STATUS = :status and m.TEMP_ID in (:tempIds) Order by b.CREAT_TIME ";
		//logger.info("sqlString1: " + sqlString);
		
		List<BillingNoticeDetail> details = entityManager.createNativeQuery(sqlString, BillingNoticeDetail.class)
		.setParameter("status", status)
		.setParameter("tempIds", tempIds).setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(BillingNoticeDetail detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
				
				//logger.info("detail in update1: " + detail.toString());
				return detail;
			}
	    }

		return null;
	}
	
	
	/**
	 * 找出第一個WAIT BillingNoticeMain 準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public BillingNoticeMain findFirstMainByStatusForUpdate(String status, List<String> tempIds) {
		
		String sqlString = "select  m.* from  BCS_BILLING_NOTICE_MAIN m  "
				+ "where m.STATUS = :status and m.TEMP_ID in (:tempIds) Order by m.CREAT_TIME ";
		List<BillingNoticeMain> mains = entityManager.createNativeQuery(sqlString, BillingNoticeMain.class)
		.setParameter("status", status)
		.setParameter("tempIds", tempIds).setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(BillingNoticeMain mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
	    }

		return null;
	}
	
	/**
	 * 找出 BillingNoticeMain 的detail準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<BillingNoticeDetail> findDetailByStatusForUpdate(List<String> status, Long mainId) {
		
		String sqlString = "select  b.* from BCS_BILLING_NOTICE_DETAIL b, BCS_BILLING_NOTICE_MAIN m  "
				+ "where  m.NOTICE_MAIN_ID = b.NOTICE_MAIN_ID and m.NOTICE_MAIN_ID = :mainId and b.STATUS in (:status)  ";
		List<BillingNoticeDetail> details = entityManager.createNativeQuery(sqlString, BillingNoticeDetail.class)
		.setParameter("status", status).setParameter("mainId", mainId)
		.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(BillingNoticeDetail detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
	    }

		return details;
	}

}
