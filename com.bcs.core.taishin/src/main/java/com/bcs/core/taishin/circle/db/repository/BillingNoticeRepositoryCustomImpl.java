package com.bcs.core.taishin.circle.db.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;

@Repository
public class BillingNoticeRepositoryCustomImpl implements BillingNoticeRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * 找出第一個retry detail 準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public BillingNoticeDetail findFirstDetailByStatusForUpdate(String status, List<String> tempIds) {
		
		
		String sqlString = "select  b.* from BCS_BILLING_NOTICE_DETAIL b, BCS_BILLING_NOTICE_MAIN m  "
				+ "where  m.NOTICE_MAIN_ID = b.NOTICE_MAIN_ID  and b.STATUS = :status and m.TEMP_ID in (:tempIds) Order by b.CREAT_TIME ";
		List<BillingNoticeDetail> details = entityManager.createNativeQuery(sqlString, BillingNoticeDetail.class)
		.setParameter("status", status)
		.setParameter("tempIds", tempIds).setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(BillingNoticeDetail detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
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
