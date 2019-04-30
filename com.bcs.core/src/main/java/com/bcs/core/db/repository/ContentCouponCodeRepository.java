package com.bcs.core.db.repository;

import java.util.List;

import javax.persistence.CacheRetrieveMode;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

import org.hibernate.CacheMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentCouponCodeRepository extends
		EntityRepository<ContentCouponCode, Long> {

	@Query(value =" SELECT TOP 1 * FROM BCS_CONTENT_COUPON_CODE WHERE COUPON_ID = ?1 AND STATUS = ?2 ORDER BY NEWID()", nativeQuery = true)
	public ContentCouponCode findOneByCouponIdAndStatus(String couponId,String status);
	
	@Query(value ="SELECT Count(*) FROM BCS_CONTENT_COUPON_CODE WHERE BCS_CONTENT_COUPON_CODE.COUPON_ID = ?1", nativeQuery = true)
	public Integer findCouponCodeListNumber(String couponId);
	
	public Page<ContentCouponCode> findByCouponId(String couponId, Pageable pageable);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query(value ="select x from ContentCouponCode x where x.couponCodeId = ?1")
	public ContentCouponCode findOneAndLock(Long couponCodeId);
	
	public ContentCouponCode findByCouponCodeIdAndStatus(Long couponCodeId,String status);
	
}
