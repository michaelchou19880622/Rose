package com.bcs.core.db.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jcodec.common.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.entity.ActionUserRewardCardPointDetail;
import com.bcs.core.db.repository.ActionUserCouponRepository;
import com.bcs.core.db.repository.ActionUserRewardCardPointDetailRepository;
import com.bcs.core.db.repository.ActionUserRewardCardRepository;
import com.bcs.core.model.RewardCardModel;
import com.bcs.core.model.RewardCardModel;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;

@Service
public class ActionUserRewardCardPointDetailService {
	public static final Integer pageSize = 1000;
	
	@Autowired
	private ActionUserCouponRepository actionUserCouponRepository;
	@Autowired
	private ActionUserRewardCardPointDetailRepository actionUserRewardCardPointDetailRepository;
	
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
	
	public boolean existsByUserRewardCardIdAndRewardCardPointId(Long userRewardCardId, String rewardCardPointId) {
		return actionUserRewardCardPointDetailRepository.existsByUserRewardCardIdAndRewardCardPointId(userRewardCardId, rewardCardPointId);
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
	 * 查詢指定使用者之指定集點卡的總集點數 
	 * 
	 * @param mid
	 * @param actionUserRewardCardId
	 * @param actionType
	 * @return havePoint
	 */
	public int sumActionUserRewardCardGetPoint(Long rewardCardId) {
		try{
			return actionUserRewardCardPointDetailRepository.sumActionUserRewardCardGetPoint(rewardCardId);
		}catch(Exception e){
			return 0;
		}
	}
	
	public void save(ActionUserRewardCardPointDetail actionUserRewardCardPointDetail) {
		actionUserRewardCardPointDetailRepository.save(actionUserRewardCardPointDetail);
	}
	
	public List<ActionUserRewardCardPointDetail> findByUserRewardCardIdAndReferenceId(Long userRewardCardId, String referenceId){
		return actionUserRewardCardPointDetailRepository.findByUserRewardCardIdAndReferenceId(userRewardCardId, referenceId);
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
	
	public  List<RewardCardModel> getRecordListByRewardCardId(String rewardCardId,Integer pageIndex){
		String queryString = "SELECT "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.ID, "+
			"BCS_ACTION_USER_REWARD_CARD.MID, "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_GET_AMOUNT, "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_GET_TIME, "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_TYPE "+
			"FROM BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL "+
			"LEFT JOIN BCS_ACTION_USER_REWARD_CARD ON BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.USER_REWARD_CARD_ID = BCS_ACTION_USER_REWARD_CARD.ID "+
			"WHERE REFERENCE_ID = ?1";
		Query query = entityManager.createNativeQuery(queryString).setParameter(1, rewardCardId);
    	query.setFirstResult(((pageIndex-1)*pageSize));
    	query.setMaxResults(pageSize);
		List<Object[]> list = query.getResultList();
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<RewardCardModel> rewardCardModels = new ArrayList<RewardCardModel>() ;
		for(Object[] o : list){
			RewardCardModel rewardCardModel = new RewardCardModel();
			rewardCardModel.setCardId(o[0]!=null? o[0].toString():"");
			rewardCardModel.setMID(o[1]!=null? o[1].toString():"");
			rewardCardModel.setPointGetAmount(o[2]!= null? Integer.parseInt(o[2].toString()):0);
			rewardCardModel.setPointGetTime(o[3]!=null? o[3].toString():"");
			rewardCardModel.setPointType(o[4]!=null? o[4].toString():"");
			rewardCardModels.add(rewardCardModel);
		}
		return rewardCardModels;
	}
	
	public  List<RewardCardModel> getRecordListByRewardCardId(String rewardCardId,String startDate, String endDate,Optional<Integer> pageIndex) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		String queryString = "SELECT "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.ID, "+
			"BCS_ACTION_USER_REWARD_CARD.MID, "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_GET_AMOUNT, "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_GET_TIME, "+
			"BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_TYPE "+
			"FROM BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL "+
			"LEFT JOIN BCS_ACTION_USER_REWARD_CARD ON BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.USER_REWARD_CARD_ID = BCS_ACTION_USER_REWARD_CARD.ID "+
			"WHERE REFERENCE_ID = ?1 AND (BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_GET_TIME BETWEEN ?2 AND ?3) ";
		
		Date startDateObj = sdf.parse(startDate);
		Date endDateObj = sdf.parse(endDate);
		
		Calendar c = Calendar.getInstance();
		c.setTime(endDateObj);
		c.add(Calendar.DATE, 1); //增加一天，因為轉換的date其分秒是0，因此查詢時，今天新增的發送報告有設定時與分時，可能會撈不到
		c.add(Calendar.SECOND, -1); //減一秒，因為可能今天新增的發送報告時間是隔天且無設定時與分，會與增加一天的時間重疊，導致可能撈到隔天的資料
		endDateObj = c.getTime();
		
		Query query = entityManager.createNativeQuery(queryString).setParameter(1, rewardCardId).setParameter(2, startDateObj).setParameter(3, endDateObj);
    	if(pageIndex.isPresent()){
    		query.setFirstResult(((pageIndex.get()-1)*pageSize));
        	query.setMaxResults(pageSize);
    	}
		List<Object[]> list = query.getResultList();
		
		Map<String, Object> map = new HashMap<String, Object>();
		List<RewardCardModel> rewardCardModels = new ArrayList<RewardCardModel>() ;
		for(Object[] o : list){
			RewardCardModel rewardCardModel = new RewardCardModel();
			rewardCardModel.setCardId(o[0]!=null?o[0].toString():"");
			rewardCardModel.setMID(o[1]!=null?o[1].toString():"");
			rewardCardModel.setPointGetAmount(o[2]!=null?Integer.parseInt(o[2].toString()):0);
			rewardCardModel.setPointGetTime(o[3]!=null?o[3].toString():"");
			rewardCardModel.setPointType(o[4]!=null?o[4].toString():"");
			rewardCardModels.add(rewardCardModel);
		}
		return rewardCardModels;
	}
	
	public ActionUserRewardCardPointDetail createForUse(String mid, ActionUserRewardCard actionUserRewardCard, int getAmount) {
		return this.createForUse(mid, actionUserRewardCard, getAmount, null);
	}
	
	public ActionUserRewardCardPointDetail createForUse(String mid, ActionUserRewardCard actionUserRewardCard, int getAmount, String referenceId) {
		return this.createForUse(mid, actionUserRewardCard, getAmount, referenceId, null, ActionUserRewardCardPointDetail.POINT_TYPE_SYSTEM);
	}
	
	public ActionUserRewardCardPointDetail createForUse(String mid, ActionUserRewardCard actionUserRewardCard, int getAmount, String referenceId, String rewardCardPointId, String pointType) {
			
		ActionUserRewardCardPointDetail actionUserRewardCardPointDetail = new ActionUserRewardCardPointDetail();
		actionUserRewardCardPointDetail.setUserRewardCardId(actionUserRewardCard.getId());
		actionUserRewardCardPointDetail.setPointGetAmount(getAmount);
		actionUserRewardCardPointDetail.setPointGetTime(new Date());
		actionUserRewardCardPointDetail.setReferenceId(referenceId);
		actionUserRewardCardPointDetail.setRewardCardPointId(rewardCardPointId);
		actionUserRewardCardPointDetail.setPointType(pointType);
		//新增集點卡取得點數紀錄
		this.save(actionUserRewardCardPointDetail);
		UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_REWARD_CARD, LOG_TARGET_ACTION_TYPE.ACTION_CouponUse, mid, actionUserRewardCardPointDetail, actionUserRewardCardPointDetail.getUserRewardCardId().toString());	
			
		return actionUserRewardCardPointDetail;
	}
	
	public boolean existsByUserRewardCardIdAndLimitGetNumberAndLimitGetTime(String userRewardCardId, Date now, String pointType) {
	    return actionUserRewardCardPointDetailRepository.existsByUserRewardCardIdAndLimitGetNumberAndLimitGetTime(userRewardCardId, now, pointType);
	}
	
	public Long countByUserRewardCardIdAndReferenceIdAndPointType(Long userRewardCardId, String referenceId, String pointType, String startDate, String endDate){
	    return actionUserRewardCardPointDetailRepository.countByUserRewardCardIdAndReferenceIdAndPointType(userRewardCardId, referenceId, pointType, startDate, endDate);
	}
	
	public  Integer getActionUserRewardCardTotalPointByCopounIdAndMID(String couponId,String MID) throws ParseException{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String queryString = "SELECT SUM(BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.POINT_GET_AMOUNT) "+
            "FROM BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL, "+
            "(SELECT BCS_ACTION_USER_REWARD_CARD.ID AS actionUserRewardCardId "+
            "FROM BCS_CONTENT_COUPON "+
            "LEFT JOIN BCS_ACTION_USER_REWARD_CARD ON BCS_CONTENT_COUPON.EVENT_REFERENCE_ID = BCS_ACTION_USER_REWARD_CARD.REWARD_CARD_ID "+
            "WHERE BCS_CONTENT_COUPON.COUPON_ID = ?1 AND BCS_ACTION_USER_REWARD_CARD.MID = ?2) AS JOINED_DATA "+
            "WHERE BCS_ACTION_USER_REWARD_CARD_POINT_DETAIL.USER_REWARD_CARD_ID = JOINED_DATA.actionUserRewardCardId ";
        
        Query query = entityManager.createNativeQuery(queryString).setParameter(1, couponId).setParameter(2, MID);
        List<Integer> list = query.getResultList(); 
        if(list.get(0)==null)
            return 0;
        else
            return list.get(0);
    }
	
	public Integer getRewardCardPointMaxPageByRewardCardId(String rewardCardId){
		Integer pointNum =  actionUserRewardCardPointDetailRepository.countRewardCardPointNumByRewardCardId(rewardCardId);
    	Integer page = pointNum / pageSize;
		if(pointNum % 1000 != 0){
			page++;
		}
		return page;
	}
}
