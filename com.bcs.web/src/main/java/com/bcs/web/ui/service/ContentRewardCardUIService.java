package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.entity.ContentRewardCardPoint;
import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.db.service.ContentRewardCardPointService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.web.ui.model.ContentRewardCardModel;

@Service
public class ContentRewardCardUIService {

	private static Logger logger = Logger.getLogger(ContentRewardCardUIService.class);
	
	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ContentRewardCardPointService contentRewardCardPointService;
	@Autowired
	private ContentResourceService contentResourceService;
	@Autowired
	private ContentCouponService contentCouponService;
	
	@SuppressWarnings("unused")
	public enum COUPON_STATUS {
		
		//在前端設定顯示真正狀態中文
        STATUS_GOTTEN("已領取此優惠券","STATUS_GOTTEN"), 
        STATUS_GOTTEN_SAME_POINT("已領取同點數之優惠券，無法領取","STATUS_GOTTEN_SAME_POINT"), 
        STATUS_CANNOT_GET("點數不足，無法領取","STATUS_CANNOT_GET"), 
        STATUS_CAN_GET("可以領取","STATUS_CAN_GET"),
        STATUS_USED("已使用此優惠券","STATUS_USED"),
		STATUS_CANNOT_GET_AMOUNT_ZERO("數量不足，無法領取","STATUS_CANNOT_GET_AMOUNT_ZERO");
		private String name;
        private String statusCode;

        // 构造方法
        private COUPON_STATUS(String name,String statusCode) {
            this.name = name;
            this.statusCode = statusCode;
        }
		public String getStatusCode() {
			return statusCode;
		}
    }
	
	/**
	 * 新增或修改優惠劵
	 * 
	 * @param contentCouponModel
	 * @param adminUserAccount
	 * @return ContentCoupon
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class)
	public ContentRewardCard saveFromUI(ContentRewardCardModel contentRewardCardModel, String adminUserAccount) throws Exception {
		return this.saveFromUI(contentRewardCardModel, adminUserAccount, true);
	}

	/**
	 * 新增或修改優惠劵
	 * 
	 * @param contentCouponModel
	 * @param adminUserAccount
	 * @param doSave
	 * @return ContentCoupon
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class)
	public ContentRewardCard saveFromUI(ContentRewardCardModel contentRewardCardModel, String adminUserAccount, boolean doSave) throws Exception {
		ContentRewardCard contentRewardCard = contentRewardCardModel.getContentRewardCard();
		
		String action = (contentRewardCard.getRewardCardId() == null ? "Create" : "Edit");
		logger.info("action:" + action);
		
		// 若是修改
		if (action.equals("Edit") && doSave) {
			contentRewardCard = mergeOldData(contentRewardCard);
		} else if (action.equals("Create")) {
			contentRewardCard.setRewardCardId(checkDuplicateUUID("1"));
		}
		
		String status = contentRewardCard.getStatus();
		if(StringUtils.isBlank(status)){
			contentRewardCard.setStatus(ContentRewardCard.REWARD_CARD_STATUS_DISABLE);
		}
		
		// Set Modify Admin User
		contentRewardCard.setModifyUser(adminUserAccount);
		Date now = new Date();
		contentRewardCard.setModifyTime(now);
		
		if(doSave){
			contentRewardCardService.save(contentRewardCard);			
			
			List <ContentCoupon> contentCouponList = contentRewardCardModel.getContentCouponList();
			logger.info("contentCouponList"+contentCouponList);
			Map<Long,List<ContentCoupon>> contentCouponMap  = new HashMap<>();
			
			
			for(ContentCoupon contentCoupon : contentCouponList){//同點數同MAP
				Long point = contentCoupon.getRequirePoint();
				contentCoupon=contentCouponService.findOne(contentCoupon.getCouponId());
				List<ContentCoupon> contentCoupons = contentCouponMap.get(point);
				if(contentCoupons==null)
					contentCoupons = new ArrayList<>();
				contentCoupons.add(contentCoupon);
				contentCouponMap.put(point, contentCoupons);
			}
			
			for(Entry<Long, List<ContentCoupon>> contentCoupons : contentCouponMap.entrySet()){ //同點數同GROUPID,塞入DB
				
				String couponGroupId = checkDuplicateUUID("1");
				Long requirePoint = contentCoupons.getKey();
				for(ContentCoupon contentCoupon: contentCoupons.getValue()){
					contentCoupon.setEventReference(ContentCoupon.EVENT_REFERENCE_REWARD_CARD);
					contentCoupon.setEventReferenceId(contentRewardCard.getRewardCardId());
					contentCoupon.setCouponGroupId(couponGroupId);
					contentCoupon.setRequirePoint(requirePoint);
					contentCouponService.save(contentCoupon);
				}
			}
			
			contentRewardCard = contentRewardCardService.findOne(contentRewardCard.getRewardCardId());
			
			//根據ContentRewardCard.requirePoint數量，新增等筆ContentRewardCardPoint
			List<ContentRewardCardPoint> oldRewardCardPoint =  contentRewardCardPointService.findByRewardCardIdAndStatus(contentRewardCard.getRewardCardId(), ContentRewardCardPoint.STATUS_ACTIVE);
			
			if(oldRewardCardPoint == null || oldRewardCardPoint.size() == 0) {
    			for(long i = 0; i < contentRewardCard.getRequirePoint(); i++) {
    			    String rewardCardPointId = contentRewardCardPointService.generateRewardCardPointId();
    			    
    			    
    			    //產生QRCode
    			    ContentResource contentResource = contentResourceService.createQRImg(adminUserAccount, rewardCardPointId);
    			    
    			    ContentRewardCardPoint contentRewardCardPoint = new ContentRewardCardPoint();
    			    contentRewardCardPoint.setRewardCardPointId(rewardCardPointId);
    			    contentRewardCardPoint.setModifyUser(adminUserAccount);
    			    contentRewardCardPoint.setModifyTime(now);
    			    contentRewardCardPoint.setQrcodeImageId(contentResource.getResourceId());
    			    contentRewardCardPoint.setRewardCardId(contentRewardCard.getRewardCardId());
    			    contentRewardCardPoint.setStatus(ContentRewardCardPoint.STATUS_ACTIVE);
    			    contentRewardCardPointService.save(contentRewardCardPoint);
    			}
			}
			
			createSystemLog(action, contentRewardCard, contentRewardCard.getModifyUser(), contentRewardCard.getModifyTime(), contentRewardCard.getRewardCardId().toString());
		}
		return contentRewardCard;
	}
	
	public void checkContentRewardCard(ContentRewardCard contentRewardCard) {
		Validate.notBlank(contentRewardCard.getRewardCardMainTitle(), "Main Title Null");
		Validate.notBlank(contentRewardCard.getRewardCardSubTitle(), "Sub Title Null");
	}
	
	/**
	 * 刪除集點卡
	 * 
	 * @param rewardCardId
	 * @param adminUserAccount
	 */
	@Transactional(rollbackFor=Exception.class)
	public void deleteFromUI(String rewardCardId, String adminUserAccount) {
		logger.info("deleteFromUI:" + rewardCardId);
		String rewardCardTitle = contentRewardCardService.findRewardCardTitleByRewardCardId(rewardCardId);
		ContentRewardCard rewardCard = contentRewardCardService.findOne(rewardCardId);
		
		rewardCard.setStatus(ContentRewardCard.REWARD_CARD_STATUS_DELETE);
		rewardCard.setModifyTime(new Date());
		rewardCard.setModifyUser(adminUserAccount);
		contentRewardCardService.save(rewardCard);
		
		//一併刪除contentRewardCardPoint
		contentRewardCardPointService.updateStatusByRewardCardIdAndStatus(rewardCardId, ContentRewardCardPoint.STATUS_ACTIVE, ContentRewardCardPoint.STATUS_DELETE);
		
		//刪除關聯的優惠券
		List <ContentCoupon> contentCoupons =contentCouponService.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_REWARD_CARD, rewardCardId);
		for(ContentCoupon contentCoupon : contentCoupons){
			contentCoupon.setEventReference(null);
			contentCoupon.setEventReferenceId(null);
			contentCouponService.save(contentCoupon);
		}
		
		createSystemLog("Delete", rewardCardTitle, adminUserAccount, new Date(), rewardCardId.toString());
	}

	@Transactional(rollbackFor=Exception.class)
	public void switchContentCouponStatus(String rewardCardId, String adminUserAccount) throws BcsNoticeException{
		// 切換狀態
		ContentRewardCard rewardCard = contentRewardCardService.findOne(rewardCardId);
		
		String status = rewardCard.getStatus();
		if(ContentRewardCard.REWARD_CARD_STATUS_ACTIVE.equals(status)){
			status = ContentRewardCard.REWARD_CARD_STATUS_DISABLE;
		}
		else if(ContentRewardCard.REWARD_CARD_STATUS_DISABLE.equals(status)){
			status = ContentRewardCard.REWARD_CARD_STATUS_ACTIVE;
		}
		else{
			throw new BcsNoticeException("請選擇正確的優惠券");
		}
		
		rewardCard.setStatus(status);
		rewardCard.setModifyTime(new Date());
		rewardCard.setModifyUser(adminUserAccount);
		contentRewardCardService.save(rewardCard);

		createSystemLog("SwitchStatus", rewardCard, adminUserAccount, new Date(), rewardCardId.toString());
	}
	
	/**
	 * 新增系統日誌
	 * 
	 * @param action
	 * @param content
	 * @param modifyUser
	 * @param modifyTime
	 */
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
		SystemLogUtil.saveLogDebug("ContentRewardCard", action, modifyUser, content, referenceId);
	}
	
	/**
	 * 將前端修改的欄位合併到原 entity，避免有些不在頁面上的欄位被清空
	 * 
	 * @param newContentCoupon
	 * @return
	 * @throws Exception 
	 */
	private ContentRewardCard mergeOldData(ContentRewardCard contentRewardCard) throws Exception {
		logger.info("contentRewardCard:" + contentRewardCard);
		// 原資料
		ContentRewardCard oldContentRewardCard = contentRewardCardService.findOne(contentRewardCard.getRewardCardId());
		logger.info("beforeMergeOldContentRewardCard:" + oldContentRewardCard);
		
		//如果有改變ContentRewardCard.requirePoint數量，將舊的ContentRewardCardPoint STATUS=DELETE，重建。
		if(contentRewardCard.getRequirePoint() != oldContentRewardCard.getRequirePoint()) {
		    contentRewardCardPointService.updateStatusByRewardCardIdAndStatus(contentRewardCard.getRewardCardId(), ContentRewardCardPoint.STATUS_ACTIVE, ContentRewardCardPoint.STATUS_DELETE);
		}
		
		oldContentRewardCard.setRewardCardMainTitle(contentRewardCard.getRewardCardMainTitle());
		oldContentRewardCard.setRewardCardSubTitle(contentRewardCard.getRewardCardSubTitle());
		oldContentRewardCard.setRewardCardBackGround(contentRewardCard.getRewardCardBackGround());
		oldContentRewardCard.setRewardCardListImageId(contentRewardCard.getRewardCardListImageId());
		oldContentRewardCard.setRequirePoint(contentRewardCard.getRequirePoint());
		oldContentRewardCard.setRewardCardStartUsingTime(contentRewardCard.getRewardCardStartUsingTime());
		oldContentRewardCard.setRewardCardEndUsingTime(contentRewardCard.getRewardCardEndUsingTime());
		oldContentRewardCard.setBonusPoint(contentRewardCard.getBonusPoint());
		oldContentRewardCard.setLimitGetTime(contentRewardCard.getLimitGetTime());
		oldContentRewardCard.setRewardCardStartGetTime(contentRewardCard.getRewardCardStartGetTime());
		oldContentRewardCard.setRewardCardEndGetTime(contentRewardCard.getRewardCardEndGetTime());
		oldContentRewardCard.setRewardCardGetNumber(contentRewardCard.getRewardCardGetNumber());
		oldContentRewardCard.setRewardCardUseDescription(contentRewardCard.getRewardCardUseDescription());
		oldContentRewardCard.setRewardCardDescription(contentRewardCard.getRewardCardDescription());
		oldContentRewardCard.setLimitGetNumber(contentRewardCard.getLimitGetNumber());
		oldContentRewardCard.setRewardCardFlag(contentRewardCard.getRewardCardFlag());
		//oldContentRewardCard.setStatus(contentRewardCard.getStatus());
		logger.info("afterMergeOldContentRewardCard:" + oldContentRewardCard);
		return oldContentRewardCard;
	}
	
    /**
	 *  檢查有無重覆使用到UUID
     */
    public Boolean checkDuplicateUUID(String queryType, String uuid) {
    	if(queryType == "1"){
    		ContentCoupon contentCoupon =contentCouponService.findByCouponGroupId(uuid);
    		if (contentCoupon == null) return false;
    	}
		return true;
    }
	
    /** 
     * 回傳一個沒有重覆的uuid
     */
    public String checkDuplicateUUID(String queryType) {
        String uuid = UUID.randomUUID().toString().toLowerCase();
        Boolean duplicateUUID = checkDuplicateUUID(queryType, uuid);
        while (duplicateUUID) {
            uuid = UUID.randomUUID().toString().toLowerCase();
            duplicateUUID = checkDuplicateUUID(queryType, uuid);
        }
        
        return uuid;
    }
}
