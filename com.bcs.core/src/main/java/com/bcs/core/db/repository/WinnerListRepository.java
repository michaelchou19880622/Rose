package com.bcs.core.db.repository;

import org.springframework.data.jpa.repository.Query;

// import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.WinnerList;
import com.bcs.core.db.persistence.EntityRepository;

public interface WinnerListRepository extends EntityRepository<WinnerList, String>{

	/* @Query(value = "SELECT TOP 1 * FROM BCS_WINNER_LIST WHERE GAME_ID = ?1 AND UID = ?2", nativeQuery = true)
	public WinnerList findByGameIdAndUID(String GAME_ID, String UID);
	
	@Query(value = "SELECT PRIZE_LIST_ID FROM BCS_WINNER_LIST WHERE GAME_ID = ?1 AND UID = ?2", nativeQuery = true)
	public Integer findPrizeListIdByGameId(String gameId, String UID); */

	public WinnerList findByActionUserCouponIdAndUid(Long actionUserCouponId,String UID);
	
	@Query(value = "SELECT COUNT(*) FROM BCS_WINNER_LIST WHERE COUPON_ID = ?1", nativeQuery = true)
	public Integer countTotalWinnerByCouponId(String couponId);
	
	String countTotalWinnerByGameIdQueryString = 
    		"SELECT COUNT(*) "
    		+ "FROM "
    			+ "BCS_ACTION_USER_COUPON "
    			+ "LEFT JOIN BCS_CONTENT_COUPON ON BCS_ACTION_USER_COUPON.COUPON_ID = BCS_CONTENT_COUPON.COUPON_ID "
    			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_ACTION_USER_COUPON.WINNER_LIST_ID = BCS_WINNER_LIST.WINNER_LIST_ID "
    		+ "WHERE "
    			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE_ID = ?1 "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE = 'SCRATCH_CARD' "
    			+ "AND BCS_CONTENT_COUPON.STATUS = 'ACTIVE' ";
	@Query(value = countTotalWinnerByGameIdQueryString, nativeQuery = true)
	public Integer countTotalWinnerByGameId(String gameId);
	
	String countTotalWinnerByGameIdAndCouponIdQueryString = 
    		"SELECT COUNT(*) "
    		+ "FROM "
    			+ "BCS_ACTION_USER_COUPON "
    			+ "LEFT JOIN BCS_CONTENT_COUPON ON BCS_ACTION_USER_COUPON.COUPON_ID = BCS_CONTENT_COUPON.COUPON_ID "
    			+ "LEFT JOIN BCS_WINNER_LIST ON BCS_ACTION_USER_COUPON.WINNER_LIST_ID = BCS_WINNER_LIST.WINNER_LIST_ID "
    		+ "WHERE "
    			+ "BCS_ACTION_USER_COUPON.ACTION_TYPE = 'GET' "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE_ID = ?1 "
    			+ "AND BCS_ACTION_USER_COUPON.COUPON_ID = ?2 "
    			+ "AND BCS_CONTENT_COUPON.EVENT_REFERENCE = 'SCRATCH_CARD' "
    			+ "AND BCS_CONTENT_COUPON.STATUS = 'ACTIVE' ";
	@Query(value = countTotalWinnerByGameIdAndCouponIdQueryString, nativeQuery = true)
	public Integer countTotalWinnerByGameIdAndCouponId(String gameId, String couponId);
}
