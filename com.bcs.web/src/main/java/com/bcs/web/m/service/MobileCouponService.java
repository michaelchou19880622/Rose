package com.bcs.web.m.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.entity.WinnerList;
import com.bcs.core.db.service.ActionUserRewardCardPointDetailService;
import com.bcs.core.db.service.ContentCouponCodeService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.GroupGenerateService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.WinnerListService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.m.service.MobilePageService;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.ui.service.ActionUserCouponUIService;
import com.bcs.web.ui.service.ActionUserRewardCardUIService;

@Service
public class MobileCouponService {
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ActionUserCouponUIService actionUserCouponUIService;
	@Autowired
	private WinnerListService winnerListService;
	@Autowired
	private MobilePageService mobilePageService;
	@Autowired
	private GroupGenerateService groupGenerateService;
	@Autowired
	private SendGroupService sendGroupService;
	@Autowired
	private ContentCouponCodeService contentCouponCodeService;
	@Autowired
	private ActionUserRewardCardUIService actionUserRewardCardUIService;
	@Autowired
	private ActionUserRewardCardPointDetailService actionUserRewardCardPointDetailService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(MobileCouponService.class);
	
	public String couponContentPage(String referenceId,
			Model model,
			HttpServletRequest request, 
			HttpServletResponse response,
			MobilePageEnum successPage,
			boolean unlimitedRedirect, 
			boolean hasFillOutTheForm) throws Exception{
		
		String sessionMID = (String) request.getSession().getAttribute("MID");
		
		//驗證MID couponId
	    if (StringUtils.isBlank(sessionMID) || StringUtils.isBlank(referenceId)) {
	    	return this.indexPage(request, response, model);
	    }
		
	    //驗證是否存在
		String couponId = referenceId;
		ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
		if(contentCoupon == null){
			return this.indexPage(request, response, model);
		}
		
		
		
		// 驗證 Status
		if(ContentCoupon.COUPON_STATUS_DELETE.equals(contentCoupon.getStatus())){
			model.addAttribute("noCoupon", "優惠券錯誤:找不到優惠券");
			
			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserCouponIndexPage.getName(), "userCouponIndexPage");
			return MobilePageEnum.UserCouponIndexPage.toString();
		}
		else if(ContentCoupon.COUPON_STATUS_DISABLE.equals(contentCoupon.getStatus())){
			model.addAttribute("noCoupon", "優惠券錯誤:找不到優惠券");
			
			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserCouponIndexPage.getName(), "userCouponIndexPage");
			return MobilePageEnum.UserCouponIndexPage.toString();
		}
		
		// 若尚未領用，則驗證領用期間、領用次數限制
		synchronized (ActionUserCouponUIService.GET_COUPON_FLAG) {
			//如果尚未領取
			if (!actionUserCouponUIService.isGetCoupon(sessionMID, couponId)) {
				// 驗證領用期間、領用次數限制
				String errorMessage = actionUserCouponUIService.checkContentCoupon(contentCoupon);
				
				if (StringUtils.isNotBlank(errorMessage)) {
					return this.msgPage(model, sessionMID, contentCoupon, errorMessage);
				}
				
				// Validate SendGroup
				boolean checkSendGroup = checkMidBySendGroup(contentCoupon.getCouponFlag(), sessionMID);
				if(checkSendGroup){
					// Success
				}
				else{
					errorMessage = "couponGroupError";
					return this.msgPage(model, sessionMID, contentCoupon, errorMessage);
				}
				
				/* 優惠券設定PRIVATE*/
				if(ContentCoupon.COUPON_FLAG_PRIVATE.equals(contentCoupon.getCouponFlag())){
					errorMessage = "couponPrivateError";
					return this.msgPage(model, sessionMID, contentCoupon, errorMessage);
				}
				//領取優惠券
				actionUserCouponUIService.createFromUIForGet(sessionMID, couponId, contentCoupon.getCouponStartUsingTime(), contentCoupon.getCouponEndUsingTime());				
			}
			
			//是否需要填寫資料
			ActionUserCoupon actionUserCoupon = actionUserCouponUIService.findByMidAndCouponIdAndActionType(sessionMID, couponId, ActionUserCoupon.ACTION_TYPE_GET);
			
			WinnerList winnerList = null;
			
			if(actionUserCoupon != null && actionUserCoupon.getWinnerListId()!=null)
				winnerList = winnerListService.findOne(actionUserCoupon.getWinnerListId());

			if((winnerList == null) && contentCoupon.getIsFillIn().equals(ContentCoupon.IS_COUPON_FILLIN_TRUE)){		
					ContentCoupon coupon = contentCouponService.findOne(couponId);		
					model.addAttribute("contentCoupon", coupon);
					model.addAttribute("imageSource", "../bcs/getResource/IMAGE/" + coupon.getCouponImageId());
					model.addAttribute("couponStartUsingTime", coupon.getCouponStartUsingTime());
					model.addAttribute("couponEndUsingTime", coupon.getCouponEndUsingTime());
					
					// 導頁至資料填寫頁面
					return MobilePageEnum.UserCouponFillOutInfoPage.toString();
			}
		}

		// Validate SendGroup
		boolean checkSendGroup = checkMidBySendGroup(contentCoupon.getCouponFlag(), sessionMID);
		if(checkSendGroup){
			// Success
		}
		else{
			String errorMessage = "couponGroupError";
			return msgPage(model, sessionMID, contentCoupon, errorMessage);
		}

		Date couponStartUsingTime = null;
		Date couponEndUsingTime = null;
		// 優惠劵是否在使用期間
		boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_COUPON_USE_TIME);
		boolean inUsingTime = false;
		// Get Setting From ActionUserCoupon
		if(isUseTime){
			ActionUserCoupon getRecord = actionUserCouponUIService.findByMidAndCouponIdAndActionType(sessionMID, couponId, ActionUserCoupon.ACTION_TYPE_GET);
			if(getRecord != null){
				inUsingTime = actionUserCouponUIService.isInUsingTime(getRecord);
				couponStartUsingTime = getRecord.getCouponStartUsingTime();
				couponEndUsingTime = getRecord.getCouponEndUsingTime();
			}
			else{
				inUsingTime = actionUserCouponUIService.isInUsingTime(contentCoupon);
				couponStartUsingTime = contentCoupon.getCouponStartUsingTime();
				couponEndUsingTime = contentCoupon.getCouponEndUsingTime();
			}
		}
		else{
			inUsingTime = actionUserCouponUIService.isInUsingTime(contentCoupon);
			couponStartUsingTime = contentCoupon.getCouponStartUsingTime();
			couponEndUsingTime = contentCoupon.getCouponEndUsingTime();
		}
		
		if(!inUsingTime){
			String errorMessage = "useTimeError";
			return this.msgPage(model, sessionMID, contentCoupon, errorMessage);
		}
		
		// 優惠劵是否超過使用限制而不准使用
		boolean disabledByUsingLimit = actionUserCouponUIService.isDisabledByUsingLimit(sessionMID, contentCoupon);
		
		if(disabledByUsingLimit){
			String errorMessage = "couponUse";
			return this.msgPage(model, sessionMID, contentCoupon, errorMessage);
		}
		
		/* 優惠劵是否有效期間內不限次數 ,註解：會有兩個畫面，此畫面不使用*/
		boolean unlimited = ContentCoupon.COUPON_USING_LIMIT_UNLIMITED.equalsIgnoreCase(contentCoupon.getCouponUsingLimit());
//		if(unlimited && unlimitedRedirect){
//			String errorMessage = "unlimited";
//			return this.msgPage(model, sessionMID, contentCoupon, errorMessage);
//		}
		
		// 優惠劵序號
		String couponSerialNumber = actionUserCouponUIService.generateCouponSerialNumber(contentCoupon, sessionMID, couponId);

		// 設定 Coupon Image Id to Resource URL
		String imageListId = actionUserCouponUIService.settingCouponImage(contentCoupon.getCouponListImageId());
		
		// 設定 Coupon Image Id to Resource URL
		String imageId = actionUserCouponUIService.settingCouponImage(contentCoupon.getCouponImageId());
		
		// ChangeCouponDescription
		actionUserCouponUIService.changeCouponDescription(contentCoupon);

		model.addAttribute("contentCoupon", contentCoupon);
		model.addAttribute("couponStartUsingTime", couponStartUsingTime);
		model.addAttribute("couponEndUsingTime", couponEndUsingTime);
		model.addAttribute("inUsingTime", inUsingTime);
		model.addAttribute("disabledByUsingLimit", disabledByUsingLimit);
		model.addAttribute("unlimited", unlimited);
		model.addAttribute("couponSerialNumber", couponSerialNumber);
		model.addAttribute("imageListId", imageListId);
		model.addAttribute("imageId", imageId);
		model.addAttribute("preview", false);

		mobilePageService.visitPageLog(sessionMID, successPage.getName(), referenceId);
		return successPage.toString();
	}
	
	public String msgPage(Model model, String sessionMID, ContentCoupon contentCoupon,  String errorMessage){
		return this.msgPage(model, sessionMID, contentCoupon, errorMessage, false);
	}
	
	private String msgPage(Model model, String sessionMID, ContentCoupon contentCoupon,  String errorMessage, boolean isShowMessage){
		// 設定 Coupon Image Id to Resource URL
		String imageListId = actionUserCouponUIService.settingCouponImage(contentCoupon.getCouponListImageId());
		
		// 設定 Coupon Image Id to Resource URL
		String imageId = actionUserCouponUIService.settingCouponImage(contentCoupon.getCouponImageId());
		
		// ChangeCouponDescription
		actionUserCouponUIService.changeCouponDescription(contentCoupon);
		
		String couponSerialNumber = actionUserCouponUIService.generateCouponSerialNumber(contentCoupon, sessionMID, contentCoupon.getCouponId());

		Date couponStartUsingTime = null;
		Date couponEndUsingTime = null;
		// 優惠劵是否在使用期間
		boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_COUPON_USE_TIME);
		// Get Setting From ActionUserCoupon
		if(isUseTime){
			ActionUserCoupon getRecord = actionUserCouponUIService.findByMidAndCouponIdAndActionType(sessionMID, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET);
			if(getRecord != null){
				couponStartUsingTime = getRecord.getCouponStartUsingTime();
				couponEndUsingTime = getRecord.getCouponEndUsingTime();
			}
			else{
				couponStartUsingTime = contentCoupon.getCouponStartUsingTime();
				couponEndUsingTime = contentCoupon.getCouponEndUsingTime();
			}
		}
		else{
			couponStartUsingTime = contentCoupon.getCouponStartUsingTime();
			couponEndUsingTime = contentCoupon.getCouponEndUsingTime();
		}
		
		model.addAttribute("couponSerialNumber", couponSerialNumber);
		model.addAttribute("contentCoupon", contentCoupon);
		model.addAttribute("couponStartUsingTime", couponStartUsingTime);
		model.addAttribute("couponEndUsingTime", couponEndUsingTime);
		model.addAttribute("imageListId", imageListId);
		model.addAttribute("imageId", imageId);
		model.addAttribute("showMessage", errorMessage);
		if(isShowMessage){
			model.addAttribute("errorMessage", "showMessage");
			model.addAttribute("showMessage", errorMessage);
		}
		else{
			model.addAttribute("errorMessage", errorMessage);
			model.addAttribute("showMessage", "");
		}
		
		mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserCouponMsgPage.getName(), errorMessage);
		return MobilePageEnum.UserCouponMsgPage.toString();
	}
	
	public boolean checkMidBySendGroup(String couponFlag, String MID){

		if(StringUtils.isNotBlank(couponFlag) && couponFlag.startsWith("GROUPID")){
			try{
				String groupIdStr = couponFlag.substring("GROUPID".length());
				Long groupId = Long.parseLong(groupIdStr);

				if(groupId > 0){
					return groupGenerateService.checkMIDBySendGroupDetailGroupId(groupId, MID);
				}
				else{
					return sendGroupService.checkMidExistDefaultGroup(groupId, MID);
				}
			}
			catch(Exception e){
	    		logger.error(ErrorRecord.recordError(e));
			}
			
			return false;
		}
		
		return true;
	}
	
	public ResponseEntity<?> actionUserCouponForGetApi(
			ActionUserCoupon actionUserCoupon, 
			HttpServletRequest request, 
			HttpServletResponse response, boolean onlyCheck) throws IOException {
		logger.info("createActionUserCouponForGetApi:" + actionUserCoupon);
		
		try{
			String sessionMID = (String) request.getSession().getAttribute("MID");
			
			// 驗證 mid
			 if(StringUtils.isBlank(sessionMID)) {
					throw new Exception("User Error");
			 }
			
			String couponId = actionUserCoupon.getCouponId();
			
			if(couponId != null ){
				ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
				if(contentCoupon != null){
					// BCS 領取不在此驗證 參考 > couponContentPage
					// 驗證 Status
					if(ContentCoupon.COUPON_STATUS_DELETE.equals(contentCoupon.getStatus())){
						throw new BcsNoticeException("優惠券錯誤:找不到優惠券");
					}
					else if(ContentCoupon.COUPON_STATUS_DISABLE.equals(contentCoupon.getStatus())){
						throw new BcsNoticeException("優惠券錯誤:找不到優惠券");
					}
				}
				else{
					throw new Exception("couponId Null");
				}
			}
			else{
				throw new Exception("couponId Null");
			}
			
			return new ResponseEntity<>(couponId, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	/**
	 * Get Coupon List From DB
	 * @param sessionMID
	 * @param contentCoupons
	 * @throws ParseException 
	 */
	public void getCouponListFromDB(String sessionMID, Map<String, ContentCoupon> contentCoupons) throws ParseException{
		
		// Get BCS Coupon COUPON_STATUS_ACTIVE
		List<ContentCoupon> bcsCoupons = contentCouponService.findByStatus(ContentCoupon.COUPON_STATUS_ACTIVE);
		
		if(bcsCoupons != null && bcsCoupons.size() > 0){
			for(ContentCoupon contentCoupon : bcsCoupons){
				boolean okCoupon = true;

				// Validate SendGroup
				boolean checkSendGroup = this.checkMidBySendGroup(contentCoupon.getCouponFlag(), sessionMID);
				if(checkSendGroup){
					// Success
				}
				else{
					okCoupon = false;
					continue;
				}

				
				//已設定為private
				if(ContentCoupon.COUPON_FLAG_PRIVATE.equals(contentCoupon.getCouponFlag())){
					// 未領取優惠券不顯示
					if(!actionUserCouponUIService.isGetCoupon(sessionMID, contentCoupon.getCouponId())){
						okCoupon = false;
						continue;
					}
				}
				
				// 驗證領用
				boolean inGetTime = false;
				// 驗證領用期間、領用次數限制
				String errorMessage = actionUserCouponUIService.checkContentCoupon(contentCoupon);
				
				if (StringUtils.isNotBlank(errorMessage)) {
					// 不能領用優惠券
					okCoupon = false;
					continue;
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
				
				if(okCoupon){
					// 設定 Coupon Image Id to Resource URL
					String imageListId = contentCoupon.getCouponListImageId();
					contentCoupon.setCouponListImageId(actionUserCouponUIService.settingCouponImage(imageListId));
					
					// 設定 Coupon Image Id to Resource URL
					String imageId = contentCoupon.getCouponImageId();
					contentCoupon.setCouponImageId(actionUserCouponUIService.settingCouponImage(imageId));

					// User Status Show
					String couponUserStatus = couponUserStatus(contentCoupon, sessionMID);
					contentCoupon.setStatus(couponUserStatus);
					
					if(contentCoupons.get(contentCoupon.getCouponId()) == null){
						contentCoupons.put(contentCoupon.getCouponId(), contentCoupon);
					}
				}
			}
		}
	}
	
	/**
	 * @param contentCoupon
	 * @param sessionMID
	 * @return
	 */
	private String couponUserStatus(ContentCoupon contentCoupon, String sessionMID){

		// 優惠劵是否超過使用限制而不准使用
		boolean disabledByUsingLimit = actionUserCouponUIService.isDisabledByUsingLimit(sessionMID, contentCoupon);
		if(disabledByUsingLimit){
			return "-此優惠券已兌換完畢";
		}
		
		// 是否已領取
		boolean isGetCoupon = actionUserCouponUIService.isGetCoupon(sessionMID, contentCoupon.getCouponId());
		if (isGetCoupon) {
			return "-此優惠券已領取";
		}
		
		return "";
	}
	
	public String indexPage(HttpServletRequest request, 
			HttpServletResponse response,
			Model model){
		logger.info("indexPage");
		model.addAttribute("linkDefault", UriHelper.bcsMPage);
		return MobilePageEnum.Page404.toString();
	}
}
