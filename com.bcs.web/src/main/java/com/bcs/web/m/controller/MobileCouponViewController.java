package com.bcs.web.m.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.m.service.MobilePageService;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.m.service.MobileCouponService;
import com.bcs.web.ui.model.ContentCouponModel;
import com.bcs.web.ui.model.UserInfoModel;
import com.bcs.web.ui.service.ActionUserCouponUIService;
import com.bcs.web.ui.service.ContentCouponUIService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


@Controller
@RequestMapping("/m")
public class MobileCouponViewController {

	@Autowired
	private MobilePageService mobilePageService;
	@Autowired
	private ActionUserCouponUIService actionUserCouponUIService;
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ContentCouponUIService contentCouponUIService;
	@Autowired
	private MobileCouponService mobileCouponService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	
	protected LoadingCache<String, ContentCoupon> dataCache;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(MobileCouponViewController.class);
	
	public MobileCouponViewController(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.build(new CacheLoader<String, ContentCoupon>() {
					@Override
					public ContentCoupon load(String key) throws Exception {
						return new ContentCoupon();
					}
				});
	}

	@RequestMapping(method = RequestMethod.GET, value = "/userCouponIndexPage")
	public String userCouponIndexPage(HttpServletRequest request, HttpServletResponse response,
			Model model) {
		logger.info("userCouponIndexPage");

		try {
			String sessionMID = (String) request.getSession().getAttribute("MID");
			
//			// 驗證 mid 已綁訂
			if (StringUtils.isBlank(sessionMID)) {
				return mobileCouponService.indexPage(request, response, model);
			}
	
			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserCouponIndexPage.getName(), "userCouponIndexPage");
			return MobilePageEnum.UserCouponIndexPage.toString();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileCouponService.indexPage(request, response, model);
		}
	}
	
	/**
	 * 優惠劵頁面
	 * 
	 * @param referenceId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/userCouponContentPage")
	public String userCouponContentPage(
			@RequestParam(value = "referenceId", required = false) String referenceId,
			Model model,
			HttpServletRequest request, 
			HttpServletResponse response) {
		logger.info("userCouponContentPage referenceId : " + referenceId);

		try {
			return mobileCouponService.couponContentPage(referenceId, model, request, response, MobilePageEnum.UserCouponContentPage, false, false);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileCouponService.indexPage(request, response, model);
		}
	}
	
	/**
	 * 優惠劵頁面
	 * 
	 * @param referenceId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/userCouponPreviewContentPage")
	public String userCouponPreviewContentPage(
			@RequestParam(value = "referenceId", required = false) String referenceId,
			Model model,
			HttpServletRequest request, 
			HttpServletResponse response) {
		logger.info("userCouponPreviewContentPage referenceId : " + referenceId);

		try {
			try {
				ContentCoupon contentCoupon = dataCache.get(referenceId);
				
				if(StringUtils.isNotBlank(contentCoupon.getStatus())){
					return this.couponContentPreviewPage(referenceId, model, request, response, MobilePageEnum.UserCouponContentPage, true);
				}
			} catch (ExecutionException e) {}
			
			return this.couponContentPreviewPage(referenceId, model, request, response, MobilePageEnum.UserCouponContentPage, false);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileCouponService.indexPage(request, response, model);
		}
	}
	
	/**
	 * 優惠劵頁面
	 * 
	 * @param referenceId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/userCouponSelectStorePage")
	public String userCouponSelectStorePage(
			@RequestParam(value = "referenceId", required = false) String referenceId,
			Model model,
			HttpServletRequest request, 
			HttpServletResponse response) {
		logger.info("userCouponSelectStorePage referenceId : " + referenceId);
		try{
			return mobileCouponService.couponContentPage(referenceId, model, request, response, MobilePageEnum.UserCouponSelectStorePage, true, false);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileCouponService.indexPage(request, response, model);
		}
	}

	private String couponContentPreviewPage(String referenceId,
			Model model,
			HttpServletRequest request, 
			HttpServletResponse response,
			MobilePageEnum successPage,
			boolean fromCache){

		ContentCoupon contentCoupon = null;
		if(fromCache){
			try {
				contentCoupon = dataCache.get(referenceId);
			} catch (ExecutionException e) {}
		}
		else{
			String couponId = referenceId;
			contentCoupon = contentCouponService.findOne(couponId);
		}
		if(contentCoupon == null){
			return mobileCouponService.indexPage(request, response, model);
		}
		
		// 優惠劵是否在使用期間
		boolean inUsingTime = actionUserCouponUIService.isInUsingTime(contentCoupon);
		
		// 優惠劵是否超過使用限制而不准使用
		boolean disabledByUsingLimit = false;
		
		// 優惠劵是否有效期間內不限次數
		boolean unlimited = ContentCoupon.COUPON_USING_LIMIT_UNLIMITED.equalsIgnoreCase(contentCoupon.getCouponUsingLimit());
		
		// 優惠劵序號
		String couponSerialNumber = contentCoupon.getCouponSerialNumber();

		// 設定 Coupon Image Id to Resource URL
		String imageListId = actionUserCouponUIService.settingCouponImage(contentCoupon.getCouponListImageId());
		
		// 設定 Coupon Image Id to Resource URL
		String imageId = actionUserCouponUIService.settingCouponImage(contentCoupon.getCouponImageId());
		
		// ChangeCouponDescription
		actionUserCouponUIService.changeCouponDescription(contentCoupon);
		
		model.addAttribute("contentCoupon", contentCoupon);
		model.addAttribute("couponStartUsingTime", contentCoupon.getCouponStartUsingTime());
		model.addAttribute("couponEndUsingTime", contentCoupon.getCouponEndUsingTime());
		model.addAttribute("inUsingTime", inUsingTime);
		model.addAttribute("disabledByUsingLimit", disabledByUsingLimit);
		model.addAttribute("unlimited", unlimited);
		model.addAttribute("couponSerialNumber", couponSerialNumber);
		model.addAttribute("imageListId", imageListId);
		model.addAttribute("imageId", imageId);
		model.addAttribute("preview", true);

		return successPage.toString();
	}
	
	/**
	 * 設定預覽優惠劵
	 * 
	 * @param contentCoupon
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/createPreviewContentCoupon", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createPreviewContentCoupon(
			@RequestBody ContentCouponModel contentCouponModel, 
			@CurrentUser CustomUser customUser, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("createPreviewContentCoupon");
		
		try{
			contentCouponUIService.checkContentCoupon(contentCouponModel);    // 檢查 ContentCoupon 的內容格式是否完整
			
			contentCouponModel.getContentCoupon().setCouponId(null);
			ContentCoupon contentCoupon = contentCouponUIService.saveFromUI(contentCouponModel,null, customUser.getAccount(), false);
			String result = UUID.randomUUID().toString().toLowerCase();
			
			dataCache.put(result, contentCoupon);
			return new ResponseEntity<>(result, HttpStatus.OK);
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
	 * 新增領取(GET)優惠劵記錄
	 * 
	 * @param actionUserCoupon
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/createActionUserCouponForGetApi", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createActionUserCouponForGetApi(
			@RequestBody ActionUserCoupon actionUserCoupon, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("createActionUserCouponForGetApi:" + actionUserCoupon);

		return mobileCouponService.actionUserCouponForGetApi(actionUserCoupon, request, response, false);
	}

	/**
	 * Get My Coupon List From Api
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/getMyCouponList")
	@ResponseBody
	public ResponseEntity<?> getMyCouponList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("getMyCouponList");

		try{
			String sessionMID = (String) request.getSession().getAttribute("MID");

			if (StringUtils.isBlank(sessionMID)) {
				return new ResponseEntity<>("MID Error", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			Map<String, ContentCoupon> contentCoupons = new LinkedHashMap<String, ContentCoupon>();
			
			// Get Coupon List From DB
			mobileCouponService.getCouponListFromDB(sessionMID, contentCoupons);
			
			return new ResponseEntity<>(contentCoupons.values(), HttpStatus.OK);
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
	 * 優惠劵頁面
	 * 
	 * @param userInfoModel
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/sendUserInfo", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> sendUserInfo(
			@RequestBody UserInfoModel userInfoModel, 
			Model model,
			HttpServletRequest request, 
			HttpServletResponse response) {
		
		logger.info("★ User infomation" + userInfoModel);
		
		try{
			String sessionMID = (String) request.getSession().getAttribute("MID");
			
			if(StringUtils.isBlank(sessionMID)){
				return new ResponseEntity<>("請重新進入網頁", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			actionUserCouponUIService.saveUserInfo(sessionMID, userInfoModel);
			return new ResponseEntity<>("Save success!", HttpStatus.OK);
		} catch (Exception e) {
			logger.info("sendUserInfo Error");
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
	 * 優惠劵頁面
	 * 
	 * @param userInfoModel
	 * @param referenceId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/getCoupon")
	public String getCoupon(
			@RequestParam(value = "counponId", required = true) String counponId,
			Model model,
			HttpServletRequest request, 
			HttpServletResponse response) {
		
		logger.info("★ This user infomation is belong to the coupon which id is: " + counponId);
		
		try{
			return mobileCouponService.couponContentPage(counponId, model, request, response, MobilePageEnum.UserCouponContentPage, true, true);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileCouponService.indexPage(request, response, model);
		}
	}
	
	/**
	 * 新增使用(USE)優惠劵記錄
	 * 
	 * @param actionUserCoupon
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/createActionUserCouponForUse", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createActionUserCouponForUse(
			@RequestBody ActionUserCoupon actionUserCoupon, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("createActionUserCouponForUse");
		
		try{
			String sessionMID = (String) request.getSession().getAttribute("MID");
			ActionUserCoupon getRecord=new ActionUserCoupon();
			
			// 驗證 mid 已綁訂
			if(StringUtils.isBlank(sessionMID)) {
				throw new BcsNoticeException("使用者錯誤");
			}
			
			String couponId = actionUserCoupon.getCouponId();
			
			// 驗證使用前已領用
			if (!actionUserCouponUIService.isGetCoupon(sessionMID, couponId)) {
				throw new BcsNoticeException("優惠券未領取");
			}
			
			ContentCoupon contentCoupon = contentCouponService.findOne(couponId);

			// 驗證 Status
			if(ContentCoupon.COUPON_STATUS_DELETE.equals(contentCoupon.getStatus())){
				throw new BcsNoticeException("優惠券錯誤:找不到優惠券");
			}
			else if(ContentCoupon.COUPON_STATUS_DISABLE.equals(contentCoupon.getStatus())){
				throw new BcsNoticeException("優惠券錯誤:找不到優惠券");
			}

			// Validate SendGroup
			boolean checkSendGroup = mobileCouponService.checkMidBySendGroup(contentCoupon.getCouponFlag(), sessionMID);
			if(checkSendGroup){
				// Success
			}
			else{
				throw new BcsNoticeException("優惠券錯誤:限制使用");
			}
			
			// 優惠劵是否在使用期間
			boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_COUPON_USE_TIME);
			boolean inUsingTime = false;
			// Get Setting From ActionUserCoupon
			if(isUseTime){
				getRecord = actionUserCouponUIService.findByMidAndCouponIdAndActionType(sessionMID, contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET);
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
			
			if (!inUsingTime) {
				throw new BcsNoticeException("優惠券不在使用期間");
			}
			
			// 優惠劵是否超過使用限制而不准使用
			boolean disabledByUsingLimit = actionUserCouponUIService.isDisabledByUsingLimit(sessionMID, contentCoupon);
			
			if (disabledByUsingLimit) {
				throw new BcsNoticeException("優惠券使用錯誤");
			}
			
			boolean unlimited = ContentCoupon.COUPON_USING_LIMIT_UNLIMITED.equalsIgnoreCase(contentCoupon.getCouponUsingLimit());
			if (unlimited) {
				throw new BcsNoticeException("優惠券不限次數使用");
			}

			boolean useSuccess = false;
			String errorStr = "";
			String successStr = "";
			
			actionUserCouponUIService.createFromUIForUse(sessionMID, actionUserCoupon,getRecord);	
			useSuccess = true;
			
			if(!useSuccess){
				throw new Exception(errorStr);
			}
			return new ResponseEntity<>(successStr, HttpStatus.OK);
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
}
