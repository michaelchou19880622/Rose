package com.bcs.web.ui.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.connection.BcsHikariConfig;
import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.entity.WinnerList;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ContentCouponCodeService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.WinnerListService;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.log.util.UserTraceLogUtil;
import com.bcs.core.resource.UriHelper;
import com.bcs.web.ui.model.UserInfoModel;

@Service
public class ActionUserCouponUIService {
	
	public static final String GET_COUPON_FLAG = "GET_COUPON_FLAG";
	
	private static Logger logger = Logger.getLogger(ActionUserCouponUIService.class);

	@Autowired
	private ActionUserCouponService actionUserCouponService;
	
	@Autowired
	private ContentCouponService contentCouponService;
	
	@Autowired
	private ContentCouponCodeService contentCouponCodeService;
	
	@Autowired
	private WinnerListService winnerListService;
	/**
	 * 新增領用(GET)優惠劵記錄
	 * 
	 * @param mid
	 * @param couponId
	 * @throws Exception 
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void createFromUIForGet(String mid, String couponId, Date sdate, Date edate) throws Exception {
	   actionUserCouponService.createActionUserCoupon(mid, couponId, sdate, edate);
	}	
	
	/**
	 * 新增使用(USE)優惠劵記錄
	 * 
	 * @param contentCoupon
	 * @param adminUserAccount
	 * @return
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void createFromUIForUse(String mid, ActionUserCoupon actionUserCoupon,ActionUserCoupon actionUserCouponForGet) {
		logger.info("createFromUIForUse mid : " + mid + ", actionUserCoupon : " + actionUserCoupon);

		actionUserCoupon.setMid(mid);
		actionUserCoupon.setActionType(ActionUserCoupon.ACTION_TYPE_USE);
		actionUserCoupon.setActionTime(new Date());
		
		actionUserCoupon.setCouponCodeId(actionUserCouponForGet.getCouponCodeId());
		actionUserCoupon.setWinnerListId(actionUserCouponForGet.getWinnerListId());

		actionUserCouponService.save(actionUserCoupon);
		
		UserTraceLogUtil.saveLogTrace(LOG_TARGET_ACTION_TYPE.TARGET_COUPON, LOG_TARGET_ACTION_TYPE.ACTION_CouponUse, mid, actionUserCoupon, actionUserCoupon.getCouponId().toString());
		
		// 優惠劵的使用次數 + 1
		contentCouponService.increaseCouponUsingNumberByCouponId(actionUserCoupon.getCouponId());
	}
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void saveUserInfo(String MID,UserInfoModel userInfoModel){
		WinnerList winner = new WinnerList();
		
		String winnerId = checkDuplicateUUID("1");
		
		logger.info("MID:"+MID);
		ActionUserCoupon actionUserCoupon = this.findByMidAndCouponIdAndActionType(MID, userInfoModel.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET);
		
		/* 將使用者的優惠券領取資訊塞入 winner 物件中 */
		winner.setUID(MID);
		winner.setWinnerListId(winnerId);
		winner.setUserName(userInfoModel.getName());
		winner.setUserIdCardNumber(userInfoModel.getId_card_number());
		winner.setUserPhoneNumber(userInfoModel.getPhone());
		winner.setUserAddress(userInfoModel.getAddress());
		winner.setCouponId(userInfoModel.getCouponId());
		winner.setModifyTime(new Date());
		winnerListService.saveWinner(winner);
		
		actionUserCoupon.setWinnerListId(winnerId);
		actionUserCouponService.save(actionUserCoupon);
		
		logger.info("winner:"+winner);
	}
	/**
	 * 優惠劵是否在使用期間
	 * 
	 * @param contentCoupon
	 * @return
	 */
	public boolean isInUsingTime(ContentCoupon contentCoupon) {
		Date toDay = new Date();
		return toDay.compareTo(contentCoupon.getCouponStartUsingTime()) >= 0 
				&& toDay.compareTo(contentCoupon.getCouponEndUsingTime()) < 0;
	}
	
	/**
	 * 優惠劵是否在使用期間
	 * 
	 * @param actionUserCoupon
	 * @return
	 */
	public boolean isInUsingTime(ActionUserCoupon actionUserCoupon) {
		Date toDay = new Date();
		return toDay.compareTo(actionUserCoupon.getCouponStartUsingTime()) >= 0 
				&& toDay.compareTo(actionUserCoupon.getCouponEndUsingTime()) < 0;
	}
	
	/**
	 * 優惠劵是否超過使用限制而不准使用
	 * 
	 * @param contentCoupon
	 * @return
	 */
	public boolean isDisabledByUsingLimit(String mid, ContentCoupon contentCoupon) {
		
		// 僅限一次
		if (ContentCoupon.COUPON_USING_LIMIT_ONCE.equals(contentCoupon.getCouponUsingLimit())) {
			return actionUserCouponService.existsByMidAndCouponIdAndActionType(
					mid, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_USE);
		}
		
		// 一天一次
		if (ContentCoupon.COUPON_USING_LIMIT_ONCE_A_DAY.equals(contentCoupon.getCouponUsingLimit())) {
			return actionUserCouponService.existsByMidAndCouponIdAndActionTypeAndActionTimeInToday(
					mid, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_USE);
		}
		
		// 有效期間內不限次數
		return false;
	}
	
	/**
	 * FindBy Mid And CouponId And ActionType ACTION_TYPE_GET
	 * @param mid
	 * @param couponId
	 * @return
	 */
	public ActionUserCoupon findByMidAndCouponIdAndActionType(String mid, String couponId, String type) {
		return actionUserCouponService.findByMidAndCouponIdAndActionType(mid, couponId, type);
	}
	
	/**
	 * 優惠劵是否已被使用者領用過
	 * 
	 * @param mid
	 * @param couponId
	 * @return
	 */
	public boolean isGetCoupon(String mid, String couponId) {
		return actionUserCouponService.existsByMidAndCouponIdAndActionType(
				mid, couponId, ActionUserCoupon.ACTION_TYPE_GET);
	}
	
	/**
	 * 驗證優惠劵相關限制
	 * 
	 * @param contentCoupon
	 * @return
	 */
	public String checkContentCoupon(ContentCoupon contentCoupon) {
		
		// 驗證符合領用期間
		if (!isInGetTime(contentCoupon)) {
			return "getTimeError";
		}
		
		// 驗證符合領用次數限制
		if (!isGetNumberAdd1LessOrEqualsLimit(contentCoupon)) {
			return "getLimitNumberError";
		}
		
		return "";
	}
	
	/**
	 * 優惠劵是否在領用期間
	 * 
	 * @param actionUserCoupon
	 */
	private boolean isInGetTime(ContentCoupon contentCoupon) {
		Date couponStartGetTime = contentCoupon.getCouponStartGetTime();
		Date couponEndGetTime = contentCoupon.getCouponEndGetTime();
		
		if (couponStartGetTime == null && couponEndGetTime == null) {
			return isInUsingTime(contentCoupon);
		}
		
		Date toDay = new Date();
		return toDay.compareTo(couponStartGetTime) >= 0 
				&& toDay.compareTo(couponEndGetTime) < 0;
	}
	
	/**
	 * 優惠劵是否符合領用次數限制
	 * 
	 * @param contentCoupon
	 * @return
	 */
	private boolean isGetNumberAdd1LessOrEqualsLimit(ContentCoupon contentCoupon) {
		Integer getLimit = contentCoupon.getCouponGetLimitNumber();
		int getNumber = contentCoupon.getCouponGetNumber();
		
		// 若領用次數限制為零表示不限制次數
		if (getLimit == null || getLimit == 0) {
			return true;
		}
		
		return (getNumber + 1) <= getLimit;
	}
	
	/**
	 * Setting Coupon ImageId to Url
	 * @param imageId
	 * @return
	 */
	public String settingCouponImage(String imageId){
		
		if(StringUtils.isNotBlank(imageId)){
			if(imageId.toLowerCase().startsWith("http")){
				
			}
			else{
				imageId = UriHelper.getCdnResourceUri("IMAGE", imageId, true);
				//imageId = UriHelper.getResourceUri("IMAGE", imageId, true);
			}
		}
		
		return imageId;
	}
	
	/**
	 * Change Coupon Description
	 * @param contentCoupon
	 */
	public void changeCouponDescription(ContentCoupon contentCoupon){
		contentCoupon.setCouponDescription(replaceBr(contentCoupon.getCouponDescription()));
		contentCoupon.setCouponUseDescription(replaceBr(contentCoupon.getCouponUseDescription()));
		contentCoupon.setCouponRuleDescription(replaceBr(contentCoupon.getCouponRuleDescription()));
	}
	
	/**
	 * Replace <br> to \n
	 * @param input
	 * @return
	 */
	public String replaceBr(String input){

		if(StringUtils.isNotBlank(input)){
			input = input.replaceAll("<br>", "\n");
			input = input.replaceAll("<Br>", "\n");
		}
		
		return input;
	}
	
	/**
	 * GenerateCouponSerialNumber
	 * @param contentCoupon
	 * @param sessionMID
	 * @param couponId
	 * @return
	 */
	public String generateCouponSerialNumber(ContentCoupon contentCoupon, String sessionMID, String couponId){
		String couponSerialNumber = contentCoupon.getCouponSerialNumber();
		
		if(contentCoupon.getIsCouponCode().equals(ContentCoupon.IS_COUPON_CODE_TRUE)){
			ActionUserCoupon actionUsercoupon =  actionUserCouponService.findByMidAndCouponIdAndActionType(sessionMID,couponId,ActionUserCoupon.ACTION_TYPE_GET);
			
			if(actionUsercoupon!=null){
				Long couponCodeId = actionUsercoupon.getCouponCodeId();
				ContentCouponCode contentCouponCode = contentCouponCodeService.findOne(couponCodeId);
				return contentCouponCode.getCouponCode();
			}else
				return "";
			
		}else if (StringUtils.isNotBlank(couponSerialNumber)) {
			Long couponSIndex = actionUserCouponService.findCouponSIndexByMidAndCouponIdAndActionType(sessionMID, couponId, ActionUserCoupon.ACTION_TYPE_GET);
			String strCouponSIndex = (couponSIndex == null ? "" : String.valueOf(couponSIndex));
			StringBuffer sb = new StringBuffer();
			Matcher m = Pattern.compile("\\{(\\d+)\\}").matcher(couponSerialNumber);
			
			while (m.find()) {
				int padZeroSize = Integer.parseInt(m.group(1));
		        m.appendReplacement(sb, StringUtils.leftPad(strCouponSIndex, padZeroSize, "0"));
		    }
			m.appendTail(sb);
		    couponSerialNumber = sb.toString();
		    return couponSerialNumber;
		}else
			return "";
	}
	
	public Boolean findSameGroupIdActionUserCouponIsGet (String MID, String couponGroupId){
		List<ContentCoupon> contentCoupons = contentCouponService.findAllByCouponGroupId(couponGroupId);
		if(contentCoupons !=null && contentCoupons.size()!=0){
			for(ContentCoupon contentCoupon :contentCoupons){
				ActionUserCoupon actionUserCouponGet = this.findByMidAndCouponIdAndActionType(MID, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET); 
				if(actionUserCouponGet != null){
					return true;
				}
			}
		}
		return false;
	}
	
	public String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean duplicateUUID = winnerListService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = winnerListService.checkDuplicateUUID(queryType, uuid);
		}

		return uuid;
	}
}
