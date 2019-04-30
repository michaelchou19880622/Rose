package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMitake;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailUnica;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainEvery8d;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMing;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainMitake;
//import com.bcs.core.taishin.circle.billingNotice.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;

@Repository
public class PnpRepositoryCustomImpl implements PnpRepositoryCustom {

	@PersistenceContext
	private EntityManager entityManager;
	
	/**
	 * 找出第一筆待PNP detail
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpDetailMitake findFirstDetailByStatusForUpdateMitake(String stage,String status) {
		String sqlString = "select  d.* from BCS_PNP_DETAIL_MITAKE d "
				+ "where d.STATUS = :status "
//				+ "and d.TEMP_ID in (:tempIds) "
                + "and d.PROC_STAGE in (:stage) "
                + "Order by d.CREAT_TIME ";
		List<PnpDetailMitake> Details = entityManager.createNativeQuery(sqlString, PnpDetailMitake.class)
				.setParameter("status", status)
				.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
				.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (Details != null && !Details.isEmpty()) {
			for(PnpDetailMitake detailItem : Details) {
				entityManager.refresh(detailItem, LockModeType.PESSIMISTIC_WRITE);
				return detailItem;
			}
		}
		return null;
	}
	
	/**
	 * 找出第一筆待PNP detail
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpDetailEvery8d findFirstDetailByStatusForUpdateEvery8d(String stage,String status) {
		String sqlString = "select  d.* from BCS_PNP_DETAIL_EVERY8D d  "
				+ "where d.STATUS = :status "
//				+ "and d.TEMP_ID in (:tempIds) "
				+ "and d.PROC_STAGE in (:stage) "
				+ "Order by d.CREAT_TIME ";
		List<PnpDetailEvery8d> Details = entityManager.createNativeQuery(sqlString, PnpDetailEvery8d.class)
		.setParameter("status", status)
		.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
		.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (Details != null && !Details.isEmpty()) {
			for(PnpDetailEvery8d detailItem : Details) {
				entityManager.refresh(detailItem, LockModeType.PESSIMISTIC_WRITE);
				return detailItem;
			}
	    }
		return null;
	}
	
	/**
	 * 找出第一筆待PNP detail
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpDetailUnica findFirstDetailByStatusForUpdateUnica(String stage,String status) {
		String sqlString = "select  d.* from BCS_PNP_DETAIL_UNICA d  "
				+ "where d.STATUS = :status "
//				+ "and d.TEMP_ID in (:tempIds) "
+ "and d.PROC_STAGE in (:stage) "
+ "Order by d.CREAT_TIME ";
		List<PnpDetailUnica> Details = entityManager.createNativeQuery(sqlString, PnpDetailUnica.class)
				.setParameter("status", status)
				.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
				.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (Details != null && !Details.isEmpty()) {
			for(PnpDetailUnica detailItem : Details) {
				entityManager.refresh(detailItem, LockModeType.PESSIMISTIC_WRITE);
				return detailItem;
			}
		}
		return null;
	}
	
	/**
	 * 找出第一筆待PNP detail
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpDetailMing findFirstDetailByStatusForUpdateMing(String stage,String status) {
		String sqlString = "select  d.* from BCS_PNP_DETAIL_MING d  "
				+ "where d.STATUS = :status "
//				+ "and d.TEMP_ID in (:tempIds) "
                + "and d.PROC_STAGE in (:stage) "
                + "Order by d.CREAT_TIME ";
		List<PnpDetailMing> Details = entityManager.createNativeQuery(sqlString, PnpDetailMing.class)
				.setParameter("status", status)
				.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
				.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (Details != null && !Details.isEmpty()) {
			for(PnpDetailMing detailItem : Details) {
				entityManager.refresh(detailItem, LockModeType.PESSIMISTIC_WRITE);
				return detailItem;
			}
		}
		return null;
	}
	
	/**
	 * 找出第一個WAIT PnpMainMitake 準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainMitake findFirstMainByStatusForUpdateMitake(String stage,String status) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_MITAKE m  "
				+ "where m.STATUS = :status "
//				+ "and m.TEMP_ID in (:tempIds) "
                + "and m.PROC_STAGE in (:stage) "
                + "Order by m.CREAT_TIME ";
		List<PnpMainMitake> mains = entityManager.createNativeQuery(sqlString, PnpMainMitake.class)
				.setParameter("status", status)
				.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
				.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainMitake mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
		}
		
		return null;
	}
	
	/**
	 * 找出第一個WAIT PnpMainEvery8d 準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainEvery8d findFirstMainByStatusForUpdateEvery8d(String stage,String status) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_EVERY8D m  "
				+ "where m.STATUS = :status "
//				+ "and m.TEMP_ID in (:tempIds) "
				+ "and m.PROC_STAGE in (:stage) "
				+ "Order by m.CREAT_TIME ";
		List<PnpMainEvery8d> mains = entityManager.createNativeQuery(sqlString, PnpMainEvery8d.class)
		.setParameter("status", status)
		.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
		.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainEvery8d mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
	    }

		return null;
	}
	
	/**
	 * 找出第一個WAIT PnpMainUnica 準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainUnica findFirstMainByStatusForUpdateUnica(String stage,String status) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_UNICA m  "
				+ "where m.STATUS = :status "
//				+ "and m.TEMP_ID in (:tempIds) "
+ "and m.PROC_STAGE in (:stage) "
+ "Order by m.CREAT_TIME ";
		List<PnpMainUnica> mains = entityManager.createNativeQuery(sqlString, PnpMainUnica.class)
				.setParameter("status", status)
				.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
				.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainUnica mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
		}
		
		return null;
	}
	
	/**
	 * 找出第一個WAIT PnpMainMing 準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainMing findFirstMainByStatusForUpdateMing(String stage,String status) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_MING m  "
				+ "where m.STATUS = :status "
//				+ "and m.TEMP_ID in (:tempIds) "
				+ "and m.PROC_STAGE in (:stage) "
				+ "Order by m.CREAT_TIME ";
		List<PnpMainMing> mains = entityManager.createNativeQuery(sqlString, PnpMainMing.class)
				.setParameter("status", status)
				.setParameter("stage", stage)
//		.setParameter("tempIds", tempIds)
				.setMaxResults(1).getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainMing mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
		}
		
		return null;
	}
	
	/**
	 * 找出PnpMainMitake
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainMitake findMainByMainIdMitake(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_MITAKE m  "
				+ "where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainMitake> mains = entityManager.createNativeQuery(sqlString, PnpMainMitake.class)
				.setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainMitake mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
		}
		
		return null;
	}
	
	/**
	 * 找出PnpMainEvery8d
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainEvery8d findMainByMainIdEvery8d(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_EVERY8D m  "
				+ "where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainEvery8d> mains = entityManager.createNativeQuery(sqlString, PnpMainEvery8d.class)
		.setParameter("mainId", mainId)
		.getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainEvery8d mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
	    }

		return null;
	}
	
	/**
	 * 找出PnpMainUnica
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainUnica findMainByMainIdUnica(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_UNICA m  "
				+ "where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainUnica> mains = entityManager.createNativeQuery(sqlString, PnpMainUnica.class)
				.setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainUnica mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
		}
		
		return null;
	}
	
	/**
	 * 找出PnpMainMing
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public PnpMainMing findMainByMainIdMing(Long mainId) {
		
		String sqlString = "select  m.* from BCS_PNP_MAIN_MING m  "
				+ "where m.PNP_MAIN_ID = :mainId ";
		List<PnpMainMing> mains = entityManager.createNativeQuery(sqlString, PnpMainMing.class)
				.setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (mains != null && !mains.isEmpty()) {
			for(PnpMainMing mainItem : mains) {
				entityManager.refresh(mainItem, LockModeType.PESSIMISTIC_WRITE);
				return mainItem;
			}
		}
		
		return null;
	}
	
	
	/**
	 * 找出 PnpMainMitake 的detail準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailByStatusForUpdateMitake(List<String> status, Long mainId) {
		
		String sqlString = "select  b.* from BCS_PNP_DETAIL_MITAKE b, BCS_PNP_MAIN_MITAKE m  "
				+ "where  m.PNP_MAIN_ID = b.PNP_MAIN_ID and m.PNP_MAIN_ID = :mainId and b.STATUS in (:status)  ";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailMitake.class)
				.setParameter("status", status).setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
		}
		
		return details;
	}
	
	/**
	 * 找出 PnpMainEvery8d 的detail準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailByStatusForUpdateEvery8d(List<String> status, Long mainId) {
		
		String sqlString = "select  b.* from BCS_PNP_DETAIL_EVERY8D b, BCS_PNP_MAIN_EVERY8D m  "
				+ "where  m.PNP_MAIN_ID = b.PNP_MAIN_ID and m.PNP_MAIN_ID = :mainId and b.STATUS in (:status)  ";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailEvery8d.class)
		.setParameter("status", status).setParameter("mainId", mainId)
		.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
	    }

		return details;
	}
	
	/**
	 * 找出 PnpMainUnica 的detail準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailByStatusForUpdateUnica(List<String> status, Long mainId) {
		
		String sqlString = "select  b.* from BCS_PNP_DETAIL_UNICA b, BCS_PNP_MAIN_UNICA m  "
				+ "where  m.PNP_MAIN_ID = b.PNP_MAIN_ID and m.PNP_MAIN_ID = :mainId and b.STATUS in (:status)  ";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailUnica.class)
				.setParameter("status", status).setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
		}
		
		return details;
	}
	
	/**
	 * 找出 PnpMainMing 的detail準備更新用
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailByStatusForUpdateMing(List<String> status, Long mainId) {
		
		String sqlString = "select  b.* from BCS_PNP_DETAIL_MING b, BCS_PNP_MAIN_MING m  "
				+ "where  m.PNP_MAIN_ID = b.PNP_MAIN_ID and m.PNP_MAIN_ID = :mainId and b.STATUS in (:status)  ";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailMing.class)
				.setParameter("status", status).setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
		}
		
		return details;
	}

	/**
	 * 找出要PNP的Details
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailsWaitForPNPMitake(String stage , String status , Long mainId) {
		String sqlString = "select b.* from BCS_PNP_DETAIL_MITAKE b  "
				+ "where b.PROC_STAGE in (:stage)  "
				+ "and b.STATUS in (:status)"
				+ "and b.PNP_MAIN_ID in(:mainId)";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailMitake.class)
				.setParameter("stage", stage)
				.setParameter("status", status)
				.setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
		}
		return details;
	}
	
	/**
	 * 找出要PNP的Details
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailsWaitForPNPEvery8d(String stage , String status , Long mainId) {
		String sqlString = "select b.* from BCS_PNP_DETAIL_EVERY8D b  "
				+ "where b.PROC_STAGE in (:stage)  "
				+ "and b.STATUS in (:status)"
				+ "and b.PNP_MAIN_ID in(:mainId)";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailEvery8d.class)
		.setParameter("stage", stage)
		.setParameter("status", status)
		.setParameter("mainId", mainId)
		.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
	    }
		return details;
	}
	
	/**
	 * 找出要PNP的Details
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailsWaitForPNPUnica(String stage , String status , Long mainId) {
		String sqlString = "select b.* from BCS_PNP_DETAIL_UNICA b  "
				+ "where b.PROC_STAGE in (:stage)  "
				+ "and b.STATUS in (:status)"
				+ "and b.PNP_MAIN_ID in(:mainId)";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailUnica.class)
				.setParameter("stage", stage)
				.setParameter("status", status)
				.setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
		}
		return details;
	}
	
	/**
	 * 找出要PNP的Details
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(rollbackFor=Exception.class, timeout = 3000)
	public List<? super PnpDetail> findDetailsWaitForPNPMing(String stage , String status , Long mainId) {
		String sqlString = "select b.* from BCS_PNP_DETAIL_Ming b  "
				+ "where b.PROC_STAGE in (:stage)  "
				+ "and b.STATUS in (:status)"
				+ "and b.PNP_MAIN_ID in(:mainId)";
		List<? super PnpDetail> details = entityManager.createNativeQuery(sqlString, PnpDetailMing.class)
				.setParameter("stage", stage)
				.setParameter("status", status)
				.setParameter("mainId", mainId)
				.getResultList();
		
		// lock and refresh before update
		if (details != null && !details.isEmpty()) {
			for(Object detail : details) {
				entityManager.refresh(detail, LockModeType.PESSIMISTIC_WRITE);
			}
		}
		return details;
	}

}
