package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentCouponRepository extends EntityRepository<ContentCoupon, String> {

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x from ContentCoupon x where x.status = ?1 order by MODIFY_TIME desc")
	public List<ContentCoupon> findByStatus(String status);
	
	@Transactional(readOnly = true, timeout = 30)
	@Query("select x from ContentCoupon x where x.couponGroupId = ?1")
	public ContentCoupon findByCouponGroupId(String couponGroupId);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x from ContentCoupon x where x.eventReference = ?1 and x.status = ?2 order by COUPON_START_USING_TIME")
	public List<ContentCoupon> findByEventReferenceAndStatus(String eventReference, String status);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * FROM BCS_CONTENT_COUPON WHERE EVENT_REFERENCE = ?1 AND EVENT_REFERENCE_ID = ?2 AND STATUS = 'ACTIVE'", nativeQuery = true)
	public List<ContentCoupon> findByEventReferenceAndEventReferenceId(String eventReference, String eventReferenceId);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.couponTitle from ContentCoupon x where x.couponId = ?1")
	String findCouponTitleByCouponId(String couponId);

	@Modifying
	@Query("update ContentCoupon x set x.couponUsingNumber = x.couponUsingNumber + 1 where x.couponId = ?1")
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	void increaseCouponUsingNumberByCouponId(String couponId);

	@Modifying
	@Query("update ContentCoupon x set x.couponGetNumber = x.couponGetNumber + 1 where x.couponId = ?1")
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	void increaseCouponGetNumberByCouponId(String couponId);
	
	@Query(value = "SELECT * FROM BCS_CONTENT_COUPON WHERE EVENT_REFERENCE IS NULL AND EVENT_REFERENCE_ID IS NULL AND COUPON_FLAG = 'PRIVATE' AND STATUS = 'ACTIVE'", nativeQuery = true)
	public List<ContentCoupon> findUnusedContentCouponList();
	
	@Query(value = "SELECT * FROM BCS_CONTENT_COUPON WHERE COUPON_GROUP_ID = ?1", nativeQuery = true)
	public List<ContentCoupon> findAllByCouponGroupId(String couponGroupId);
}
