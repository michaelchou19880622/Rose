package com.bcs.web.m.service;

import java.io.IOException;
// import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.entity.ActionUserRewardCardPointDetail;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.service.ActionUserRewardCardPointDetailService;
import com.bcs.core.db.service.ActionUserRewardCardService;
import com.bcs.core.db.service.ContentCouponCodeService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.web.ui.service.ActionUserCouponUIService;
import com.bcs.web.ui.service.ActionUserRewardCardUIService;
import com.bcs.web.ui.service.ContentRewardCardUIService.COUPON_STATUS;

@Service
public class MobileRewardCardService {

	private static Logger logger = Logger.getLogger(MobileRewardCardService.class);
	
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ActionUserCouponUIService actionUserCouponUIService;
	@Autowired
	private ActionUserRewardCardUIService actionUserRewardCardUIService;
	@Autowired
	private ActionUserRewardCardPointDetailService actionUserRewardCardPointDetailService;
	@Autowired
	private ContentCouponCodeService contentCouponCodeService;
	/**
	 * 新增使用(USE)集點卡 取得點數記錄
	 * 
	 * @param actionUserRewardCard
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws BcsNoticeException 
	 */
	@Transactional(rollbackFor=Exception.class)
	public String createActionUserRewardCardForUse( String sessionMID, String rewardCardId,	int getPointAmount) throws BcsNoticeException {
		logger.info("MobileRewardCardService createActionUserRewardCardForUse");
		
		Date now = new Date();
		
		// 驗證 mid 已綁訂
		if(StringUtils.isBlank(sessionMID)) {
			throw new BcsNoticeException("使用者錯誤");
		}
			
		// 驗證使用前已領用
		if (!actionUserRewardCardUIService.isGetRewardCard(sessionMID, rewardCardId)) {
			throw new BcsNoticeException("集點卡未領取");
		}
			
		ContentRewardCard contentRewardCard = contentRewardCardService.findOne(rewardCardId);
		logger.info("contentRewardCard:" + contentRewardCard);
		// 驗證 Status
		if(ContentRewardCard.REWARD_CARD_STATUS_DELETE.equals(contentRewardCard.getStatus())){
			throw new BcsNoticeException("集點卡錯誤:找不到集點卡");
		}
		else if(ContentRewardCard.REWARD_CARD_STATUS_DISABLE.equals(contentRewardCard.getStatus())){
			throw new BcsNoticeException("集點卡錯誤:找不到集點卡");
		}
			
		// 集點卡是否在使用期間
		boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_REWARDCARD_USE_TIME);
		boolean inUsingTime = false;
		// 找到該使用者正在使用的集點卡
		ActionUserRewardCard getRecord = actionUserRewardCardUIService.findByMidAndRewardCardIdAndActionType(sessionMID, contentRewardCard.getRewardCardId());
		if(isUseTime){
			if(getRecord != null){
				inUsingTime = actionUserRewardCardUIService.isInUsingTime(getRecord);
			}
			else{
				inUsingTime = actionUserRewardCardUIService.isInUsingTime(contentRewardCard);
			}
		}
		else{
			inUsingTime = actionUserRewardCardUIService.isInUsingTime(contentRewardCard);			}
			
		String successStr = "";
		
		if (!inUsingTime) {
			//throw new BcsNoticeException("集點卡不在使用期間");
		    successStr = "集點卡不在使用期間";
		}
			
		//防止不當使用設定
		if(now.compareTo(getRecord.getNextGetPointTime()) < 0){
			return "此集點卡，在" + contentRewardCard.getLimitGetTime() + "小時內無法領取";
		}
		
        Long limitGetTime = contentRewardCard.getLimitGetTime();
        boolean isLimitNumber = false;
        if(limitGetTime < 24L && limitGetTime != 0L) {
            isLimitNumber = actionUserRewardCardPointDetailService.existsByUserRewardCardIdAndLimitGetNumberAndLimitGetTime(rewardCardId, new Date(), ActionUserRewardCardPointDetail.POINT_TYPE_AUTOMATIC);     
        }
        else if(limitGetTime == 24L){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTime(now);
            calendarEnd.add(Calendar.DATE, 1);
            
            Long UserRewardCardPointDetaiLength = actionUserRewardCardPointDetailService.countByUserRewardCardIdAndReferenceIdAndPointType(getRecord.getId(), String.valueOf(rewardCardId), 
                    ActionUserRewardCardPointDetail.POINT_TYPE_AUTOMATIC, sdf.format(now), sdf.format(calendarEnd.getTime()));
            
            if(UserRewardCardPointDetaiLength >= contentRewardCard.getLimitGetNumber()){
                isLimitNumber = true;
            }  
        }
        
        if(isLimitNumber) {
            return "此集點卡在" + contentRewardCard.getLimitGetTime() + "小時內, 只能集" + contentRewardCard.getLimitGetNumber() + "點";
        }
		
		//新增取點紀錄
		actionUserRewardCardUIService.createFromUIForUse(sessionMID, getRecord, getPointAmount);
		//修改下次可取點時間
		actionUserRewardCardUIService.modifyGetPointTime(sessionMID, getRecord, contentRewardCard);
			
		/*// 計算已經獲得的點數
		int havePoint = this.getHavePoint(sessionMID, rewardCardId);
			
		if(havePoint >= contentRewardCard.getRequirePoint()){
			//收集完畢，將集點卡改為已使用狀態
			//actionUserRewardCardUIService.modifyRewardCardActionType(sessionMID, rewardCardId, getRecord);
			
			ContentCoupon contentCoupon = contentCouponService.findOne(contentRewardCard.getCouponId());
			// 新增領用(GET)優惠劵記錄
			actionUserCouponUIService.createFromUIForGet(sessionMID, contentCoupon.getCouponId(), contentCoupon.getCouponStartUsingTime(), contentCoupon.getCouponEndUsingTime());
		}*/
		
		return successStr;
	}
	
	public int getHavePoint(String sessionMID, String rewardCardId){
		ActionUserRewardCard actionUserRewardCard = actionUserRewardCardUIService.findByMidAndRewardCardIdAndActionType(sessionMID, rewardCardId);
		return actionUserRewardCardPointDetailService.sumActionUserRewardCardGetPoint(actionUserRewardCard.getId());
	}
	
	public Map<String, ContentCoupon> getCouponListFromDB(String sessionMID, String rewardCardId,Integer havePoint){
		Map<String, ContentCoupon> contentCoupons = new HashMap<>();
		// Get BCS Coupon COUPON_STATUS_ACTIVE
		List<ContentCoupon> bcsCoupons = contentCouponService.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_REWARD_CARD, rewardCardId);
		
		List<String> couponGroupIds = new ArrayList<>();
		if(bcsCoupons != null && bcsCoupons.size() > 0){
			for(ContentCoupon contentCoupon : bcsCoupons){
				boolean okCoupon = true;
				boolean okGetCoupon = true;
				String status = "";
				
				if(!ContentCoupon.EVENT_REFERENCE_REWARD_CARD.equals(contentCoupon.getEventReference()) || !contentCoupon.getEventReferenceId().equals(rewardCardId)){  //不屬於此 rewardCardId
						okCoupon = false;
						continue;
				}
				
				// 驗證領用
				boolean inGetTime = false;
				// 驗證領用期間、領用次數限制
				String errorMessage = actionUserCouponUIService.checkContentCoupon(contentCoupon);
				
				if (StringUtils.isNotBlank(errorMessage)) {
					// 不能領用優惠券
				}
				else{
					inGetTime = true;
				}
				
				// 預時消失
				boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_COUPON_USE_TIME);
				boolean inUsingTime = false;
				// Get Setting From ActionUserCoupon
				if (actionUserCouponUIService.isGetCoupon(sessionMID, contentCoupon.getCouponId())) {
					if(isUseTime){
						ActionUserCoupon getRecord = actionUserCouponUIService.findByMidAndCouponIdAndActionType(sessionMID, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET);
						if(getRecord != null){
							inUsingTime = actionUserCouponUIService.isInUsingTime(getRecord);
						}
						else{
							inUsingTime = actionUserCouponUIService.isInUsingTime(contentCoupon);
						}
					}
					else{
						inUsingTime = actionUserCouponUIService.isInUsingTime(contentCoupon);
					}
				}
				else{
					inUsingTime = actionUserCouponUIService.isInUsingTime(contentCoupon);
				}
				
				if(!inUsingTime && !inGetTime){
					okCoupon = false;
					continue;
				}
				
				
				
				//集點卡優惠券狀態
				if(okCoupon){
					 if(havePoint <contentCoupon.getRequirePoint()){
						 	okGetCoupon = false;
						 	status=COUPON_STATUS.STATUS_CANNOT_GET.getStatusCode();
					}

					//用couponId去尋找是否領取過
					ActionUserCoupon gottenActionUserCoupon = actionUserCouponUIService.findByMidAndCouponIdAndActionType(sessionMID, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET);
					
					if(gottenActionUserCoupon!=null){
							//若領取過取得此已領取過的coupon的couponGroupId
							logger.info("contentCoupon.getCouponGroupId():"+contentCoupon.getCouponGroupId());
							couponGroupIds.add(contentCoupon.getCouponGroupId());
							logger.info("couponGroupIds:"+couponGroupIds);
							okGetCoupon = false;
							
							//重複檢測 contentCoupons 同點數的優惠券,必須更改狀態
							for(Entry<String, ContentCoupon> entry : contentCoupons.entrySet()){
								ContentCoupon comparedContentCoupon = entry.getValue();
								if(comparedContentCoupon.getEventReference().equals(contentCoupon.getEventReference()) && 
								   comparedContentCoupon.getEventReferenceId().equals(contentCoupon.getEventReferenceId()) &&
								   comparedContentCoupon.getCouponGroupId().equals(contentCoupon.getCouponGroupId())){
									entry.getValue().setStatus(COUPON_STATUS.STATUS_GOTTEN_SAME_POINT.getStatusCode());
								}
							}
							
							ActionUserCoupon usedActionUserCoupon = actionUserCouponUIService.findByMidAndCouponIdAndActionType(sessionMID, contentCoupon.getCouponId(),ActionUserCoupon.ACTION_TYPE_USE);
							
							if(usedActionUserCoupon!=null)//有領取優惠券但還未但判斷是否填寫資料
								status= COUPON_STATUS.STATUS_USED.getStatusCode();
							else if(contentCoupon.getIsFillIn().equals(ContentCoupon.IS_COUPON_FILLIN_TRUE)){
								if(gottenActionUserCoupon.getWinnerListId()!=null)
									status= COUPON_STATUS.STATUS_GOTTEN.getStatusCode();
								else
									status= COUPON_STATUS.STATUS_CAN_GET.getStatusCode();								
							}
							else
								status= COUPON_STATUS.STATUS_GOTTEN.getStatusCode();	

					}else{
						//判斷目前優惠券是否已領取完畢，有就不能顯示
						if(contentCoupon.getCouponGetLimitNumber()!=null && contentCoupon.getCouponGetLimitNumber()<= contentCoupon.getCouponGetNumber()){
							okGetCoupon = false;
							status=COUPON_STATUS.STATUS_CANNOT_GET_AMOUNT_ZERO.getStatusCode();
						}else{
							for(String couponGroupId :couponGroupIds){
								logger.info("couponGroupId:"+couponGroupId);
								logger.info("actionUserCoupon.getCouponGroupId():"+contentCoupon.getCouponGroupId());
								if(couponGroupId.equals(contentCoupon.getCouponGroupId())){//同群組的優惠券不得再領取x
									okGetCoupon = false;
									status=COUPON_STATUS.STATUS_GOTTEN_SAME_POINT.getStatusCode();
								}
							}
						}						
					}
				}
				
				if(okCoupon){
					
					// 設定 Coupon Image Id to Resource URL
					String imageListId = contentCoupon.getCouponListImageId();
					contentCoupon.setCouponListImageId(actionUserCouponUIService.settingCouponImage(imageListId));
					
					// 設定 Coupon Image Id to Resource URL
					String imageId = contentCoupon.getCouponImageId();
					contentCoupon.setCouponImageId(actionUserCouponUIService.settingCouponImage(imageId));

					// User Status Show
					String couponUserStatus="";
					if(okGetCoupon)
						couponUserStatus = COUPON_STATUS.STATUS_CAN_GET.getStatusCode();
					else
						 couponUserStatus = status;
					logger.info("before contentCoupon.getStatus():"+contentCoupon.getStatus());
					logger.info("contentCoupon.getCouponId():"+contentCoupon.getCouponId());
					logger.info("couponUserStatus:"+couponUserStatus);
					contentCoupon.setStatus(couponUserStatus);
					logger.info("after contentCoupon.getStatus():"+contentCoupon.getStatus());
					
					if(contentCoupons.get(contentCoupon.getCouponId()) == null){
						logger.info(contentCoupon);
						contentCoupons.put(contentCoupon.getCouponId(), contentCoupon);
					}
					
				}
			}
		}
		return contentCoupons;
	}
}
