package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentRewardCardRepository extends EntityRepository<ContentRewardCard, String> {

	@Query("select x from ContentRewardCard x where x.status = ?1 order by MODIFY_TIME desc")
	public List<ContentRewardCard> findByStatus(String status);
/*
	@Query("select x from ContentCoupon x where x.eventReference = ?1 and x.status = ?2 order by COUPON_START_USING_TIME")
	public List<ContentCoupon> findByEventReferenceAndStatus(String eventReference, String status);
	*/
	@Query("select x.rewardCardMainTitle from ContentRewardCard x where x.rewardCardId = ?1")
	String findRewardCardTitleByRewardCardId(String rewardCardId);
	/*
	@Modifying
	@Query("update ContentCoupon x set x.couponUsingNumber = x.couponUsingNumber + 1 where x.couponId = ?1")
	void increaseCouponUsingNumberByCouponId(String couponId);
	*/
	@Modifying
	@Query("update ContentRewardCard x set x.rewardCardGetNumber = x.rewardCardGetNumber + 1 where x.rewardCardId = ?1")
	void increaseRewardCardGetNumberByRewardCardId(String rewardCardId);
	
//	@Query("select x from ContentRewardCard x where x.couponId = ?1 order by x.rewardCardId desc")
//	List<ContentRewardCard> findByCouponId(String couponId);
}
