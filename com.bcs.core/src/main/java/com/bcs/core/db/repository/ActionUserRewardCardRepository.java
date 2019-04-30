package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.persistence.EntityRepository;

public interface ActionUserRewardCardRepository extends
		EntityRepository<ActionUserRewardCard, Long> {
	
	List<ActionUserRewardCard> findByMid(String mid);
	List<ActionUserRewardCard> findByRewardCardId(Long rewardCardId);

	@Query(value = "SELECT TOP 1 * FROM BCS_ACTION_USER_REWARD_CARD where MID = ?1 AND REWARD_CARD_ID = ?2 AND ACTION_TYPE = ?3 ORDER BY ID DESC ", nativeQuery = true)
//	@Query(value = "SELECT * FROM BCS_ACTION_USER_REWARD_CARD where MID = ?1 AND REWARD_CARD_ID = ?2 AND ACTION_TYPE = ?3 LIMIT 1", nativeQuery = true)
	ActionUserRewardCard findByMidAndRewardCardIdAndActionType(String mid, String rewardCardId, String actionType);

	@Query("select "
			+ "case when count(x) > 0 then true else false end "
		+ "from ActionUserRewardCard x "
		+ "WHERE x.mid = ?1 "
			+ "and x.rewardCardId = ?2 "
			+ "and x.actionType = ?3 ")
	boolean existsByMidAndRewardCardIdAndActionType(String mid, String rewardCardId, String actionType);
	
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
	
	@Query("select max(x.couponSIndex) from ActionUserCoupon x where x.couponId = ?1")
	Long findMaxCouponSIndexByCouponId(String couponId);
	
	@Query("select x.couponSIndex from ActionUserCoupon x "
			+ "where x.mid = ?1 and x.couponId = ?2 and x.actionType = ?3")
	Long findCouponSIndexByMidAndCouponIdAndActionType(String mid, String couponId, String actionType);
	
	@Query("select distinct mid from ActionUserCoupon x "
			+ "where x.couponId = ?1 and x.actionType = ?2 and x.actionTime >= ?3 and x.actionTime < ?4")
	List<String> findMidByCouponIdAndActionTypeAndTime(String couponId, String actionType, Date start, Date end);
	
	@Query("select count(mid), count(distinct mid) from ActionUserCoupon x "
			+ "where x.couponId = ?1 and x.actionType = ?2 and x.actionTime >= ?3 and x.actionTime < ?4")
	List<Object[]> countMidByCouponIdAndActionTypeAndTime(String couponId, String actionType, Date start, Date end);
}
