package com.bcs.core.db.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.repository.ActionUserCouponRepository;
import com.bcs.core.db.repository.ActionUserRewardCardRepository;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;

@Service
public class ActionUserRewardCardService {

	@Autowired
	private ActionUserCouponRepository actionUserCouponRepository;
	@Autowired
	private ActionUserRewardCardRepository actionUserRewardCardRepository;
	@Autowired
	private ContentRewardCardService contentRewardCardService;

	/**
	 * findByMid
	 * @param mid
	 * @return
	 */
	public List<ActionUserRewardCard> findByMid(String mid){
		return actionUserRewardCardRepository.findByMid(mid);
	}
	
	public List<ActionUserCoupon> findByCouponId(String couponId){
		return actionUserCouponRepository.findByCouponId(couponId);
	}

	/**
	 * findOne
	 * @param id
	 * @return
	 */
	public ActionUserRewardCard findOne(Long id){
		return actionUserRewardCardRepository.findOne(id);
	}
	/**
	 * @param mid
	 * @param rewardCardId
	 * @param actionType
	 * @return ActionUserRewardCard
	 */
	public ActionUserRewardCard findByMidAndRewardCardIdAndActionType(String mid, String rewardCardId, String actionType) {
		return actionUserRewardCardRepository.findByMidAndRewardCardIdAndActionType(mid, rewardCardId, actionType);
	}
	
	/**
	 * 查詢是否存在指定的 mid、rewardCardId、actionType 的集點卡使用記錄
	 * 
	 * @param mid
	 * @param rewardCardId
	 * @param actionType
	 * @return
	 */
	public boolean existsByMidAndRewardCardIdAndActionType(String mid, String rewardCardId, String actionType) {
		return actionUserRewardCardRepository.existsByMidAndRewardCardIdAndActionType(mid, rewardCardId, actionType);
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
	
	public void save(ActionUserRewardCard actionUserRewardCard) {
		actionUserRewardCardRepository.save(actionUserRewardCard);
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
	
	public ActionUserRewardCard createForGet(String mid, String rewardCardId, Date sdate, Date edate) {
		ActionUserRewardCard actionUserRewardCard = new ActionUserRewardCard();
		actionUserRewardCard.setMid(mid);
		actionUserRewardCard.setRewardCardId(rewardCardId);
		actionUserRewardCard.setActionType(ActionUserRewardCard.ACTION_TYPE_GET);
		actionUserRewardCard.setActionTime(new Date());
		actionUserRewardCard.setRewardCardStartUsingTime(sdate);
		actionUserRewardCard.setRewardCardEndUsingTime(edate);
		actionUserRewardCard.setNextGetPointTime(new Date());
		
		this.save(actionUserRewardCard);

		UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_REWARD_CARD, LOG_TARGET_ACTION_TYPE.ACTION_RewardCardGet, mid, actionUserRewardCard, rewardCardId.toString());
		
		// 集點卡的領用次數 + 1
		contentRewardCardService.increaseRewardCardGetNumberByRewardCardId(actionUserRewardCard.getRewardCardId());
		
		return actionUserRewardCard;
	}
}
