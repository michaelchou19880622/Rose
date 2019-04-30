package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.persistence.EntityRepository;

public interface ActionUserCouponRepository extends
		EntityRepository<ActionUserCoupon, Long> {

	@Transactional(readOnly = true, timeout = 30)
	List<ActionUserCoupon> findByMid(String mid);

	@Transactional(readOnly = true, timeout = 30)
	List<ActionUserCoupon> findByCouponId(String couponId);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT TOP 1 * FROM BCS_ACTION_USER_COUPON where MID = ?1 AND COUPON_ID = ?2 AND ACTION_TYPE = ?3 ORDER BY ID DESC ", nativeQuery = true)
//	@Query(value = "SELECT * FROM BCS_ACTION_USER_COUPON where MID = ?1 AND COUPON_ID = ?2 AND ACTION_TYPE = ?3 ORDER BY ID DESC LIMIT 1", nativeQuery = true) // MYSQL Difference
	ActionUserCoupon findByMidAndCouponIdAndActionType(String mid, String couponId, String actionType);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select "
			+ "case when count(x) > 0 then true else false end "
		+ "from ActionUserCoupon x "
		+ "WHERE x.mid = ?1 "
			+ "and x.couponId = ?2 "
			+ "and x.actionType = ?3 ")
	boolean existsByMidAndCouponIdAndActionType(String mid, String couponId, String actionType);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select "
			+ "case when count(x) > 0 then true else false end "
		+ "from ActionUserCoupon x "
		+ "WHERE x.mid = ?1 "
			+ "and x.couponId = ?2 "
			+ "and x.actionType = ?3 "
			+ "and YEAR(x.actionTime) = YEAR(CURRENT_TIMESTAMP) "
			+ "and MONTH(x.actionTime) = MONTH(CURRENT_TIMESTAMP) "
			+ "and DAY(x.actionTime) = DAY(CURRENT_TIMESTAMP) ")
	boolean existsByMidAndCouponIdAndActionTypeAndActionTimeInToday(String mid, String couponId, String actionType);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select max(x.couponSIndex) from ActionUserCoupon x where x.couponId = ?1")
	Long findMaxCouponSIndexByCouponId(String couponId);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.couponSIndex from ActionUserCoupon x "
			+ "where x.mid = ?1 and x.couponId = ?2 and x.actionType = ?3")
	Long findCouponSIndexByMidAndCouponIdAndActionType(String mid, String couponId, String actionType);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select distinct mid from ActionUserCoupon x "
			+ "where x.couponId = ?1 and x.actionType = ?2 and x.actionTime >= ?3 and x.actionTime < ?4")
	List<String> findMidByCouponIdAndActionTypeAndTime(String couponId, String actionType, Date start, Date end);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select count(mid), count(distinct mid) from ActionUserCoupon x "
			+ "where x.couponId = ?1 and x.actionType = ?2 and x.actionTime >= ?3 and x.actionTime < ?4")
	List<Object[]> countMidByCouponIdAndActionTypeAndTime(String couponId, String actionType, Date start, Date end);
	
	@Query(value="SELECT COUNT(COUPON_CODE_ID) as c from BCS_ACTION_USER_COUPON WHERE COUPON_ID =?1 GROUP BY COUPON_CODE_ID  ORDER BY c DESC",nativeQuery=true)
	List<Integer> getIsCouponDuplicateResult(String couponId);
}
