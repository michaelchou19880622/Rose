package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.repository.ActionUserCouponRepository;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.model.RewardCardModel;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ActionUserCouponService {

	@Autowired
	private ActionUserCouponRepository actionUserCouponRepository;
	@Autowired
	private ContentCouponService contentCouponService; 
	@Autowired
	private ContentCouponCodeService contentCouponCodeService;
	
	private static Logger logger = Logger.getLogger(ActionUserCouponService.class);
	
	@PersistenceContext
    EntityManager entityManager;
	/**
	 * findByMid
	 * @param mid
	 * @return
	 */
	public List<ActionUserCoupon> findByMid(String mid){
		return actionUserCouponRepository.findByMid(mid);
	}
	
	public List<ActionUserCoupon> findByCouponId(String couponId){
		return actionUserCouponRepository.findByCouponId(couponId);
	}

	/**
	 * findOne
	 * @param id
	 * @return
	 */
	public ActionUserCoupon findOne(Long id){
		return actionUserCouponRepository.findOne(id);
	}
	/**
	 * @param mid
	 * @param couponId
	 * @param actionType
	 * @return ActionUserCoupon
	 */
	public ActionUserCoupon findByMidAndCouponIdAndActionType(String mid, String couponId, String actionType) {
		return actionUserCouponRepository.findByMidAndCouponIdAndActionType(mid, couponId, actionType);
	}
	
	/**
	 * 查詢是否存在指定的 mid、couponId、actionType 的優惠劵使用記錄
	 * 
	 * @param mid
	 * @param couponId
	 * @param actionType
	 * @return
	 */
	public boolean existsByMidAndCouponIdAndActionType(String mid, String couponId, String actionType) {
		return actionUserCouponRepository.existsByMidAndCouponIdAndActionType(mid, couponId, actionType);
	}
	
	/**
	 * 查詢是否存在指定的 mid、couponId、actionType 且 actionTime 是今天的優惠劵使用記錄
	 * 
	 * @param mid
	 * @param couponId
	 * @param actionType
	 * @return
	 */
	public boolean existsByMidAndCouponIdAndActionTypeAndActionTimeInToday(String mid, String couponId, String actionType) {
		return actionUserCouponRepository.existsByMidAndCouponIdAndActionTypeAndActionTimeInToday(mid, couponId, actionType);
	}
	
	/**
	 * 查詢優惠劵的最大 couponSIndex
	 * 
	 * @param couponId
	 * @return
	 */
	public Long findMaxCouponSIndexByCouponId(String couponId) {
		return actionUserCouponRepository.findMaxCouponSIndexByCouponId(couponId);
	}
	
	/**
	 * 查詢優惠劵 couponSIndex
	 * 
	 * @param mid
	 * @param couponId
	 * @param actionType
	 * @return couponSIndex
	 */
	public Long findCouponSIndexByMidAndCouponIdAndActionType(String mid, String couponId, String actionType) {
		return actionUserCouponRepository.findCouponSIndexByMidAndCouponIdAndActionType(mid, couponId, actionType);
	}
	
	public ActionUserCoupon save(ActionUserCoupon actionUserCoupon) {
		return actionUserCouponRepository.save(actionUserCoupon);
	}
	
	public void delete(Long id){
		actionUserCouponRepository.delete(id);
	}
	
	public List<ActionUserCoupon> findAll(){
		return actionUserCouponRepository.findAll();
	}
	
	public Page<ActionUserCoupon> findAll(Pageable pageable){
		return actionUserCouponRepository.findAll(pageable);
	}
	
	public List<String> findMidByCouponIdAndActionTypeAndTime(String couponId, String actionType, Date start, Date end){
		return actionUserCouponRepository.findMidByCouponIdAndActionTypeAndTime(couponId, actionType, start, end);
	}

	public List<Object[]> countMidByCouponIdAndActionTypeAndTime(String couponId, String actionType, Date start, Date end){
		return actionUserCouponRepository.countMidByCouponIdAndActionTypeAndTime(couponId, actionType, start, end);
	}
	
	public List<Map<String, String>>  getCouponUseRecordListByRewardCardId(String rewardCardId){
		String queryString = "SELECT "+
			"BCS_ACTION_USER_COUPON.MID, "+
			"BCS_CONTENT_COUPON.COUPON_TITLE, "+
			"BCS_ACTION_USER_COUPON.ACTION_TIME, "+
			"BCS_WINNER_LIST.USER_NAME, "+
			"BCS_WINNER_LIST.USER_IDCARDNUMBER, "+
			"BCS_WINNER_LIST.USER_ADDRESS, "+
			"BCS_WINNER_LIST.USER_PHONENUMBER, "+
			"BCS_CONTENT_COUPON.IS_FILL_IN "+
			"FROM BCS_ACTION_USER_COUPON "+			
			"LEFT JOIN BCS_CONTENT_COUPON ON BCS_ACTION_USER_COUPON.COUPON_ID = BCS_CONTENT_COUPON.COUPON_ID "+
			"LEFT JOIN BCS_WINNER_LIST ON BCS_WINNER_LIST.WINNER_LIST_ID = BCS_ACTION_USER_COUPON.WINNER_LIST_ID "+
			"WHERE BCS_ACTION_USER_COUPON.COUPON_ID IN (SELECT BCS_CONTENT_COUPON.COUPON_ID FROM BCS_CONTENT_COUPON WHERE (BCS_CONTENT_COUPON.EVENT_REFERENCE ='REWARD_CARD' AND BCS_CONTENT_COUPON.EVENT_REFERENCE_ID=?1)) AND BCS_ACTION_USER_COUPON.ACTION_TYPE ='GET'";
		Query query = entityManager.createNativeQuery(queryString).setParameter(1, rewardCardId);
		List<Object[]> list = query.getResultList();

		List<Map<String, String>> results = new ArrayList<>();
		for(Object[] o : list){
			Map<String, String> result = new LinkedHashMap<>();
			result.put("UID",o[0].toString());
			result.put("couponTitle",o[1].toString());
			result.put("couponActionTime",o[2].toString());
			result.put("name", o[3] !=null ? o[3].toString() : (ContentCoupon.IS_COUPON_FILLIN_TRUE == Boolean.parseBoolean(o[7].toString()))?"此優惠券尚未填寫":"此優惠券不需填寫");
			result.put("idCardNumber", o[4] != null ? o[4].toString() : "");
			result.put("address", o[5] != null ? o[5].toString() : "");
			result.put("phoneNumber", o[6] != null ? o[6].toString() : "");
			
			results.add(result);
		}
		return results;
	}
	
	/**
	 * 新增領用(GET)優惠劵記錄
	 * 
	 * @param mid
	 * @param couponId
	 */
	@Transactional(rollbackFor = Exception.class)
	public void createActionUserCoupon(String mid, String couponId, Date sdate, Date edate)  throws Exception{
		try {
			ActionUserCoupon actionUserCoupon = this.findByMidAndCouponIdAndActionType(mid, couponId,ActionUserCoupon.ACTION_TYPE_GET);
			if (actionUserCoupon == null) {
				ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
				ContentCouponCode notUsedContentCouponCode = null;
				actionUserCoupon = new ActionUserCoupon();
				actionUserCoupon.setMid(mid);
				actionUserCoupon.setCouponId(couponId);
				actionUserCoupon.setActionType(ActionUserCoupon.ACTION_TYPE_GET);
				actionUserCoupon.setActionTime(new Date());
				actionUserCoupon.setCouponStartUsingTime(sdate);
				actionUserCoupon.setCouponEndUsingTime(edate);
	
				if (contentCoupon.getIsCouponCode().equals(ContentCoupon.IS_COUPON_CODE_TRUE)) {
					notUsedContentCouponCode = contentCouponCodeService.findNotUsedCouponCodeAndLock(couponId,ContentCouponCode.COUPON_CODE_IS_NOT_USE,0);
					actionUserCoupon.setCouponCodeId(notUsedContentCouponCode.getCouponCodeId());
				}
	
				Long maxCouponSIndex = this.findMaxCouponSIndexByCouponId(couponId);
				actionUserCoupon.setCouponSIndex(maxCouponSIndex == null ? 1 : maxCouponSIndex + 1);
	
				this.save(actionUserCoupon);
	
				if (contentCoupon.getIsCouponCode().equals(ContentCoupon.IS_COUPON_CODE_TRUE)) {
					notUsedContentCouponCode.setActionUserCouponId(actionUserCoupon.getId());
					notUsedContentCouponCode.setStatus(ContentCouponCode.COUPON_CODE_IS_USED);
					contentCouponCodeService.save(notUsedContentCouponCode);
				}
	
				UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_COUPON, LOG_TARGET_ACTION_TYPE.ACTION_CouponGet,mid, actionUserCoupon, couponId.toString());
	
				// 優惠劵的領用次數 + 1
				contentCouponService.increaseCouponGetNumberByCouponId(actionUserCoupon.getCouponId());
			}
			logger.info("end transaction commit");
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			throw e;
		}
	}
	
	@Test
	public List<Integer> getIsCouponDuplicateResult(String couponId){
		return actionUserCouponRepository.getIsCouponDuplicateResult(couponId);
	}
}
