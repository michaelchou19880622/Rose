package com.bcs.web.m.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.api.service.LineProfileService;
import com.bcs.core.api.service.LineWebLoginApiService;
import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ActionUserRewardCard;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.entity.ContentRewardCardPoint;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ActionUserRewardCardPointDetailService;
import com.bcs.core.db.service.ActionUserRewardCardService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentRewardCardPointService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.m.service.MobilePageService;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.m.service.MobileCouponService;
import com.bcs.web.m.service.MobileRewardCardService;
import com.bcs.web.ui.model.ContentRewardCardModel;
import com.bcs.web.ui.service.ActionUserCouponUIService;
import com.bcs.web.ui.service.ActionUserRewardCardUIService;
import com.bcs.web.ui.service.ContentRewardCardUIService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Controller
@RequestMapping("/m")
public class MobileRewardCardViewController {

	@Autowired
	private MobilePageService mobilePageService;
	@Autowired
	private MobileRewardCardService mobileRewardCardService;
	@Autowired
	private ActionUserCouponUIService actionUserCouponUIService;
	@Autowired
	private ActionUserRewardCardService actionUserRewardCardService;
	@Autowired
	private ActionUserRewardCardUIService actionUserRewardCardUIService;
	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private MobileUserController mobileUserController;
	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ContentRewardCardPointService contentRewardCardPointService;
	@Autowired
	private ActionUserRewardCardPointDetailService actionUserRewardCardPointDetailService;
	@Autowired
	private LineWebLoginApiService lineWebLoginApiService;
	@Autowired
	private LineProfileService lineProfileService;
	@Autowired
	private ContentRewardCardUIService contentRewardCardUIService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	
	protected LoadingCache<String, ContentRewardCardModel> dataCache;

	/** Logger */
	private static Logger logger = Logger.getLogger(MobileRewardCardViewController.class);

	public MobileRewardCardViewController() {

		dataCache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(10, TimeUnit.MINUTES)
				.build(new CacheLoader<String, ContentRewardCardModel>() {
					@Override
					public ContentRewardCardModel load(String key) throws Exception {
						return new ContentRewardCardModel();
					}
				});
	}

	/**
	 * 單張集點卡頁面
	 * 
	 * @param referenceId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/userRewardCardContentPage")
	public String userRewardCardContentPage(@RequestParam(value = "referenceId", required = false) String referenceId,
			Model model, HttpServletRequest request, HttpServletResponse response) {
		logger.info("userRewardCardContentPage referenceId : " + referenceId);

		try {
			return this.rewardCardContentPage(referenceId, model, request, response,MobilePageEnum.UserRewardCardContentPage, false);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileUserController.indexPage(request, response, model);
		}
	}

	/**
	 * 預覽集點卡頁面
	 * 
	 * @param referenceId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/userRewardCardPreviewContentPage")
	public String userRewardCardPreviewContentPage(
			@RequestParam(value = "referenceId", required = false) String referenceId, Model model,
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("userRewardCardPreviewContentPage referenceId : " + referenceId);

		try {
			try {
				ContentRewardCardModel contentRewardCardModel = dataCache.get(referenceId);

				if (StringUtils.isNotBlank(contentRewardCardModel.getContentRewardCard().getStatus())) {
					return this.rewardCardContentPreviewPage(referenceId, model, request, response,
							MobilePageEnum.UserRewardCardContentPage, true);
				}
			} catch (ExecutionException e) {
			}

			return this.rewardCardContentPreviewPage(referenceId, model, request, response,
					MobilePageEnum.UserRewardCardContentPage, false);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileUserController.indexPage(request, response, model);
		}
	}

//	/**
//	 * 取得集點卡
//	 * 
//	 * @param referenceId
//	 * @param model
//	 * @param request
//	 * @param response
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.GET, value = "/userRewardCardSelectStorePage")
//	public String userCouponSelectStorePage(@RequestParam(value = "referenceId", required = false) String referenceId,
//			Model model, HttpServletRequest request, HttpServletResponse response) {
//		logger.info("userCouponSelectStorePage referenceId : " + referenceId);
//		try {
//			return this.rewardCardContentPage(referenceId, model, request, response,
//					MobilePageEnum.UserCouponSelectStorePage, true);
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			return mobileUserController.indexPage(request, response, model);
//		}
//	}

	private String rewardCardContentPreviewPage(String referenceId, Model model, HttpServletRequest request,
			HttpServletResponse response, MobilePageEnum successPage, boolean fromCache) {

		ContentRewardCardModel contentRewardCardModel = null;
		ContentRewardCard contentRewardCard = new ContentRewardCard();

		/* 撈集點卡資料。如果 formCache 為 true，便從 cache 找，否則從 Database 撈 */
		if (fromCache) {
			try {
				contentRewardCardModel = dataCache.get(referenceId);
				contentRewardCard = contentRewardCardModel.getContentRewardCard();
			} catch (ExecutionException e) {
			}
		} else {
			String rewardCardId = referenceId;
			contentRewardCard = contentRewardCardService.findOne(rewardCardId);
		}

		/* 找不到就去 Webcomm official website */
		if (contentRewardCard == null) {
			return mobileUserController.indexPage(request, response, model);
		}

		model.addAttribute("havePoint", 0);
		model.addAttribute("contentRewardCard", contentRewardCard);
		model.addAttribute("rewardCardStartUsingTime", contentRewardCard.getRewardCardStartUsingTime());
		model.addAttribute("rewardCardEndUsingTime", contentRewardCard.getRewardCardEndUsingTime());
		model.addAttribute("contentCoupons", contentRewardCardModel.getContentCouponList());

		if (!contentRewardCard.getRewardCardBackGround().matches("stamp_con stamp_con_bg_color_[0-9]+")) {
			model.addAttribute("rewardCardBackgroundImage",
					"background-image: url(\"../bcs/getResource/IMAGE/" + contentRewardCard.getRewardCardBackGround()
							+ "\"); background-repeat: no-repeat; background-size: 100% 100%;");
		}

		return successPage.toString();
	}

	/**
	 * 設定預覽集點卡
	 * 
	 * @param contentCoupon
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/createPreviewContentRewardCard", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createPreviewContentRewardCard(@RequestBody ContentRewardCardModel contentRewardCardModel,
			@CurrentUser CustomUser customUser, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		logger.info("createPreviewContentRewardCard");

		try {
			ContentRewardCard contentRewardCard = contentRewardCardModel.getContentRewardCard();
			List<ContentCoupon> contentCouponList = contentRewardCardModel.getContentCouponList();

			ContentRewardCardModel previewContentRewardCardModel = new ContentRewardCardModel();
			ContentRewardCard previewContentRewardCard = new ContentRewardCard();
			List<ContentCoupon> previewContentCouponList = new ArrayList<>();

			for (ContentCoupon contentCoupon : contentCouponList) {
				Long requirePoint = contentCoupon.getRequirePoint();

				contentCoupon = contentCouponService.findOne(contentCoupon.getCouponId());
				ContentCoupon previewContentCoupon = new ContentCoupon();

				previewContentCoupon.setCouponTitle(contentCoupon.getCouponTitle());
				previewContentCoupon.setRequirePoint(requirePoint);
				previewContentCoupon.setStatus(ContentRewardCardUIService.COUPON_STATUS.STATUS_CANNOT_GET.toString());

				// 設定 Coupon Image Id to Resource URL
				String imageListId = contentCoupon.getCouponListImageId();
				previewContentCoupon.setCouponListImageId(actionUserCouponUIService.settingCouponImage(imageListId));

				// 設定 Coupon Image Id to Resource URL
				String imageId = contentCoupon.getCouponImageId();
				previewContentCoupon.setCouponImageId(actionUserCouponUIService.settingCouponImage(imageId));

				previewContentCouponList.add(previewContentCoupon);

			}

			contentRewardCardUIService.checkContentRewardCard(contentRewardCard);

			contentRewardCard.setRewardCardId(null);

			previewContentRewardCard.setRewardCardMainTitle(contentRewardCard.getRewardCardMainTitle());
			previewContentRewardCard.setRewardCardSubTitle(contentRewardCard.getRewardCardSubTitle());
			previewContentRewardCard.setRewardCardBackGround(contentRewardCard.getRewardCardBackGround());
			previewContentRewardCard.setRewardCardListImageId(contentRewardCard.getRewardCardListImageId());
			previewContentRewardCard.setRequirePoint(contentRewardCard.getRequirePoint());
			previewContentRewardCard.setRewardCardStartUsingTime(contentRewardCard.getRewardCardStartUsingTime());
			previewContentRewardCard.setRewardCardEndUsingTime(contentRewardCard.getRewardCardEndUsingTime());
			previewContentRewardCard.setBonusPoint(contentRewardCard.getBonusPoint());
			previewContentRewardCard.setLimitGetTime(contentRewardCard.getLimitGetTime());
			previewContentRewardCard.setRewardCardStartGetTime(contentRewardCard.getRewardCardStartGetTime());
			previewContentRewardCard.setRewardCardEndGetTime(contentRewardCard.getRewardCardEndGetTime());
			previewContentRewardCard.setRewardCardGetNumber(0);
			previewContentRewardCard.setRewardCardUseDescription(contentRewardCard.getRewardCardUseDescription());
			previewContentRewardCard.setRewardCardDescription(contentRewardCard.getRewardCardDescription());
			previewContentRewardCard.setStatus(ContentRewardCard.REWARD_CARD_STATUS_DISABLE);
			previewContentRewardCard.setModifyTime(new Date());
			previewContentRewardCard.setModifyUser(customUser.getAccount());

			previewContentRewardCardModel.setContentRewardCard(previewContentRewardCard);
			previewContentRewardCardModel.setContentCouponList(previewContentCouponList);

			String cacheKey = UUID.randomUUID().toString().toLowerCase();

			logger.info("◎ 集點卡物件：" + previewContentRewardCardModel);

			dataCache.put(cacheKey, previewContentRewardCardModel);
			return new ResponseEntity<>(cacheKey, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if (e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	/**
	 * Generate RewardCard Page
	 * 
	 * @param referenceId
	 * @param model
	 * @param request
	 * @param response
	 * @param successPage
	 * @param unlimitedRedirect
	 * @return
	 * @throws BcsNoticeException
	 */
	private String rewardCardContentPage(String referenceId, Model model, HttpServletRequest request,
			HttpServletResponse response, MobilePageEnum successPage, boolean canGetRewardCard)
			throws BcsNoticeException {
		HttpSession session = request.getSession();
		String sessionMID = (String) session.getAttribute("MID");
		String rewardCardId = referenceId;
		
		String errorMessage="";
	
		ActionUserRewardCard actionUserRewardCardForGet = actionUserRewardCardUIService.findByMidAndRewardCardIdAndActionType(sessionMID, rewardCardId);
		
		// 驗證 mid
		if (StringUtils.isBlank(sessionMID)) {
			return mobileUserController.indexPage(request, response, model);
		}

		
		ContentRewardCard contentRewardCard = contentRewardCardService.findOne(rewardCardId);
		if (contentRewardCard == null) {
			return mobileUserController.indexPage(request, response, model);
		}

		// 驗證 Status
		if (ContentRewardCard.REWARD_CARD_STATUS_DELETE.equals(contentRewardCard.getStatus())) {
			model.addAttribute("noCard", "集點卡錯誤:找不到集點卡");
			return this.userRewardCardIndexPage(request, response, model);
		} else if (ContentRewardCard.REWARD_CARD_STATUS_DISABLE.equals(contentRewardCard.getStatus())) {
			model.addAttribute("noCard", "集點卡錯誤:找不到集點卡");
			return this.userRewardCardIndexPage(request, response, model);
		}
		
		//使用區間
		Date rewardCardStartUsingTime = contentRewardCard.getRewardCardStartUsingTime();
		Date rewardCardEndUsingTime = contentRewardCard.getRewardCardEndUsingTime();
		
		//領用區間
		Date rewardCardStartGetTime = contentRewardCard.getRewardCardStartGetTime();
		Date rewardCardEndGetTime = contentRewardCard.getRewardCardEndGetTime();
		
		//現在時間
		Date now = new Date();
		
		// 集點卡是否在領用期間
		Boolean isInGetTime =(now.compareTo(rewardCardStartGetTime) >= 0 && now.compareTo(rewardCardEndGetTime) < 0);
		if(!isInGetTime){
			errorMessage = "getTimeError";
			return this.msgPage(model, sessionMID, contentRewardCard, errorMessage);
		}
		
		// 集點卡是否在使用期間
		boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_REWARDCARD_USE_TIME);
		boolean inUsingTime = false;
		// Get Setting From ActionUserRewardCard
		if (isUseTime) {
			
			if (actionUserRewardCardForGet.getId() != null) 
				inUsingTime = actionUserRewardCardUIService.isInUsingTime(actionUserRewardCardForGet);
			else 
				inUsingTime = actionUserRewardCardUIService.isInUsingTime(contentRewardCard);
		} else {
			inUsingTime = actionUserRewardCardUIService.isInUsingTime(contentRewardCard);
		}
		
		if (!inUsingTime) {
			errorMessage = "useTimeError";
			return this.msgPage(model, sessionMID, contentRewardCard, errorMessage);
		}
		
		// 若尚未領用，則驗證領用期間、領用次數限制
		synchronized (ActionUserRewardCardUIService.GET_REWARD_CARD_FLAG) {
			if (!actionUserRewardCardUIService.isGetRewardCard(sessionMID, rewardCardId)) {
				if((contentRewardCard.getRewardCardFlag()!=null && 
						contentRewardCard.getRewardCardFlag().equals(ContentRewardCard.REWARD_CARD_FLAG_PRIVATE))){
					if(!canGetRewardCard)	
						errorMessage = "rewardCardError";
				}

				if (StringUtils.isNotBlank(errorMessage)) {
					logger.info("errorMessage:" + errorMessage);
					return this.msgPage(model, sessionMID, contentRewardCard, errorMessage);
				}

				// 新增領用(GET)集點卡記錄
				actionUserRewardCardForGet = actionUserRewardCardUIService.createFromUIForGet(sessionMID, rewardCardId,contentRewardCard.getRewardCardStartUsingTime(), contentRewardCard.getRewardCardEndUsingTime());
				// 根據取卡回饋點數，領取集點卡時直接發送點數
				if(contentRewardCard.getBonusPoint()>0L)
					mobileRewardCardService.createActionUserRewardCardForUse(sessionMID, rewardCardId,contentRewardCard.getBonusPoint().intValue());
			}
		}

		// 計算已經獲得的點數
//		ActionUserRewardCard actionUserRewardCard = actionUserRewardCardUIService.findByMidAndRewardCardIdAndActionType(sessionMID, rewardCardId);
		int havePoint = actionUserRewardCardPointDetailService.sumActionUserRewardCardGetPoint(actionUserRewardCardForGet.getId());

		// ChangeRewardCardDescription
		actionUserRewardCardUIService.changeRewardCardDescription(contentRewardCard);

		model.addAttribute("contentRewardCard", contentRewardCard);
		model.addAttribute("actionUserRewardCardId", actionUserRewardCardForGet.getId());
		model.addAttribute("rewardCardStartUsingTime", rewardCardStartUsingTime);
		model.addAttribute("rewardCardEndUsingTime", rewardCardEndUsingTime);
		model.addAttribute("inUsingTime", inUsingTime);
		model.addAttribute("havePoint", havePoint);
		model.addAttribute("preview", false);
		
		Map<String, ContentCoupon> contentCoupons = mobileRewardCardService.getCouponListFromDB(sessionMID, rewardCardId, havePoint);
		model.addAttribute("contentCoupons",contentCoupons.values());
		if(!contentRewardCard.getRewardCardBackGround().matches("stamp_con stamp_con_bg_color_[0-9]+")) {
			model.addAttribute("rewardCardBackgroundImage", "background-image: url(\"../bcs/getResource/IMAGE/" + contentRewardCard.getRewardCardBackGround() + "\"); background-repeat: no-repeat; background-size: 100% 100%;");
		}

		logger.info("rewardCardContentPage model:" + model);

		mobilePageService.visitPageLog(sessionMID, successPage.getName(), referenceId);
		return successPage.toString();
	}

	/**
	 * return UserRewardCardMsgPage
	 * 
	 * @param model
	 * @param sessionMID
	 * @param contentRewardCard
	 * @param errorMessage
	 * @return
	 */
	private String msgPage(Model model, String sessionMID, ContentRewardCard contentRewardCard, String errorMessage) {
		return this.msgPage(model, sessionMID, contentRewardCard, errorMessage, false, false);
	}

	private String msgPage(Model model, String sessionMID, ContentRewardCard contentRewardCard, String errorMessage,
			boolean isShowMessage, boolean showCouponBtn) {

		// Change RewardCard Description
		actionUserRewardCardUIService.changeRewardCardDescription(contentRewardCard);

		Date rewardCardStartUsingTime = null;
		Date rewardCardEndUsingTime = null;

		// 集點卡是否在使用期間
		boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_REWARDCARD_USE_TIME);

		// Get Setting From ActionUserRewardCard
		if (isUseTime) {
			ActionUserRewardCard getRecord = actionUserRewardCardUIService
					.findByMidAndRewardCardIdAndActionType(sessionMID, contentRewardCard.getRewardCardId());
			if (getRecord != null) {
				rewardCardStartUsingTime = getRecord.getRewardCardStartUsingTime();
				rewardCardEndUsingTime = getRecord.getRewardCardEndUsingTime();
			} else {
				rewardCardStartUsingTime = contentRewardCard.getRewardCardStartUsingTime();
				rewardCardEndUsingTime = contentRewardCard.getRewardCardEndUsingTime();
			}
		} else {
			rewardCardStartUsingTime = contentRewardCard.getRewardCardStartUsingTime();
			rewardCardEndUsingTime = contentRewardCard.getRewardCardEndUsingTime();
		}

		model.addAttribute("contentRewardCard", contentRewardCard);
		model.addAttribute("rewardCardStartUsingTime", rewardCardStartUsingTime);
		model.addAttribute("rewardCardEndUsingTime", rewardCardEndUsingTime);
		model.addAttribute("showMessage", errorMessage);
		if (isShowMessage) {
			model.addAttribute("errorMessage", "showMessage");
			model.addAttribute("showMessage", errorMessage);
		} else {
			model.addAttribute("errorMessage", errorMessage);
			model.addAttribute("showMessage", "");
		}
		model.addAttribute("showCouponBtn", showCouponBtn);

		mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserRewardCardMsgPage.getName(), errorMessage);
		return MobilePageEnum.UserRewardCardMsgPage.toString();
	}

	/**
	 * 新增領取(GET)集點卡記錄
	 * 
	 * @param actionUserRewardCard
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/createActionUserRewardCardForGetApi", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createActionUserRewardCardForGetApi(@RequestBody ActionUserRewardCard actionUserRewardCard,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("createActionUserRewardCardForGetApi:" + actionUserRewardCard);

		return this.actionUserRewardCardForGetApi(actionUserRewardCard, request, response, false);
	}

	public ResponseEntity<?> actionUserRewardCardForGetApi(ActionUserRewardCard actionUserRewardCard, HttpServletRequest request, HttpServletResponse response, boolean onlyCheck) throws IOException {
		logger.info("actionUserRewardCardForGetApi:" + actionUserRewardCard);

		try {
			String sessionMID = (String) request.getSession().getAttribute("MID");

			// 驗證 mid 已綁訂
			if (StringUtils.isBlank(sessionMID)) {
				throw new Exception("User Error");
			}

			String rewardCardId = actionUserRewardCard.getRewardCardId();

			if (rewardCardId != null) {
				ContentRewardCard contentRewardCard = contentRewardCardService.findOne(rewardCardId);
				if (contentRewardCard != null) {
					// BCS 領取不在此驗證 參考 > couponRewardCardPage
					// 驗證 Status
					if (ContentRewardCard.REWARD_CARD_STATUS_DELETE.equals(contentRewardCard.getStatus())) {
						throw new BcsNoticeException("集點卡錯誤:找不到集點卡");
					} else if (ContentRewardCard.REWARD_CARD_STATUS_DISABLE.equals(contentRewardCard.getStatus())) {
						throw new BcsNoticeException("集點卡錯誤:找不到集點卡");
					}
				} else {
					throw new Exception("rewardCardId Null");
				}
			} else {
				throw new Exception("rewardCardId Null");
			}

			return new ResponseEntity<>(rewardCardId, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if (e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	/**
	 * Get My RewardCard List From Api
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/getMyRewardCardList")
	@ResponseBody
	public ResponseEntity<?> getMyRewardCardList(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.info("getMyRewardCardList");

		try {
			String sessionMID = (String) request.getSession().getAttribute("MID");

			// 驗證 mid 已綁訂
			if (StringUtils.isBlank(sessionMID)) {
				return new ResponseEntity<>("MID Error", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			Map<String, ContentRewardCard> contentReardCards = new LinkedHashMap<String, ContentRewardCard>();

			// Get RewardCard List From DB
			this.getRewardCardListFromDB(sessionMID, contentReardCards);

			return new ResponseEntity<>(contentReardCards.values(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if (e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/userRewardCardIndexPage")
	public String userRewardCardIndexPage(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("userRewardCardIndexPage");

		try {
			String sessionMID = (String) request.getSession().getAttribute("MID");
			// 驗證 mid 已綁訂
			if (StringUtils.isBlank(sessionMID)) {
				return mobileUserController.indexPage(request, response, model);
			}
			mobilePageService.visitPageLog(sessionMID, MobilePageEnum.UserRewardCardIndexPage.getName(),
					"userRewardCardIndexPage");
			return MobilePageEnum.UserRewardCardIndexPage.toString();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileUserController.indexPage(request, response, model);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/RewardCard/getPoint/{rewardCardPointId}")
	public String getPoint(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String rewardCardPointId, Model model) throws IOException {
		logger.info("getPoint");

		try {
			String lineoauthLink = "";

			String ChannelID = CoreConfigReader.getString(CONFIG_STR.Default.toString(),
					CONFIG_STR.ChannelID.toString(), true);

			lineoauthLink = CoreConfigReader.getString(CONFIG_STR.LINE_OAUTH_URL_V2_1);
			lineoauthLink = lineoauthLink.replace("{ChannelID}", ChannelID);
			lineoauthLink = lineoauthLink.replace("{RedirectUrl}", URLEncoder.encode(UriHelper.getRewardCardValidateUri(), "UTF-8"));

			lineoauthLink = lineoauthLink.replace("{TracingId}", rewardCardPointId);

			model.addAttribute("lineoauthLink", lineoauthLink);

			return MobilePageEnum.UserTracingStartPage.toString();
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return mobileUserController.indexPage(request, response, model);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/RewardCard/validate")
	public void validateTracing(HttpServletRequest request, HttpServletResponse response, Model model)
			throws Exception {
		logger.info("RewardCard validateTracing");

		HttpSession session = request.getSession();

		try {
			String code = request.getParameter("code");
			logger.info("validateTracing code:" + code);

			String state = request.getParameter("state");
			logger.info("validateTracing state:" + state);

			String error = request.getParameter("error");
			logger.info("validateTracing error:" + error);

			String errorCode = request.getParameter("errorCode");
			logger.info("validateTracing errorCode:" + errorCode);

			String errorMessage = request.getParameter("errorMessage");
			logger.info("validateTracing errorMessage:" + errorMessage);

			String mid = (String) session.getAttribute("MID");
			logger.info("mid:" + mid);

			if (StringUtils.isBlank(state)) {
				throw new Exception("rewardCardPointId Error:" + state);
			}

			ContentRewardCardPoint contentRewardCardPoint = contentRewardCardPointService.findOne(state);

			if (contentRewardCardPoint == null) {
				throw new Exception("rewardCardPointId Error:" + state);
			}

			if (StringUtils.isNotBlank(error) || StringUtils.isNotBlank(errorCode)) {
				Map<String, String> result = new HashMap<String, String>();
				result.put("error", error);
				result.put("code", code);
				result.put("state", state);
				result.put("errorCode", errorCode);
				result.put("errorMessage", errorMessage);

				String linkUrl = UriHelper.getRewardCardGetPointUri(state);
				response.sendRedirect(linkUrl);
				return;
			}

			this.callRetrievingAPI(code, mid, request, response, model, contentRewardCardPoint, state);
			return;

		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			String linkUrl = UriHelper.bcsMPage;
			response.sendRedirect(linkUrl);
			return;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/RewardCard/removeRewardCardResultAttr")
	public void removeRewardCardResultAttr(HttpServletRequest request, HttpServletResponse response) throws Exception {
		logger.info("RewardCard removeRewardCardResultAttr");

		HttpSession session = request.getSession();

		try {
			if (session.getAttribute("rewardCardResult") != null) {
				logger.info("removed");
				session.removeAttribute("rewardCardResult");
			}

		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return;
		}
	}
	
	/**
	 * 新增領取(GET)優惠劵記錄
	 * 
	 * @param actionUserCoupon
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/RewardCard/createActionUserCouponForGetApi", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createActionUserCouponForGetApi(
			@RequestBody ActionUserCoupon actionUserCoupon, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		try {
			String sessionMID = (String) request.getSession().getAttribute("MID");
			logger.info("createActionUserCouponForGetApi:" + actionUserCoupon);
			Boolean canGetCoupon = true;
			String couponId = actionUserCoupon.getCouponId();
			
			ActionUserCoupon actionUserCouponForGet = actionUserCouponService.findByMidAndCouponIdAndActionType(sessionMID, couponId, ActionUserCoupon.ACTION_TYPE_GET);
			
			if(actionUserCouponForGet != null ){
				return new ResponseEntity<>(couponId, HttpStatus.OK);
			}
			
			ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
			ActionUserRewardCard actionUserRewardCard = actionUserRewardCardUIService.findByMidAndRewardCardIdAndActionType(sessionMID, contentCoupon.getEventReferenceId());
			int havePoint = actionUserRewardCardPointDetailService.sumActionUserRewardCardGetPoint(actionUserRewardCard.getId());
			if(contentCoupon.getRequirePoint() <= havePoint){//判斷是否可以領取此張優惠券
				String couponGroupId =contentCoupon.getCouponGroupId();
				Boolean isSameCouponGroupIdIsGet = actionUserCouponUIService.findSameGroupIdActionUserCouponIsGet(sessionMID,couponGroupId);
				//是否有領過同點數優惠券
				if(isSameCouponGroupIdIsGet)
					canGetCoupon=  false;
			}else{
				canGetCoupon=  false;
			}
			
			if(canGetCoupon){
				Date startUsingDate = contentCoupon.getCouponStartUsingTime();
				Date endUsingDate = contentCoupon.getCouponEndUsingTime();
				actionUserCouponService.createActionUserCoupon(sessionMID, couponId, startUsingDate, endUsingDate);
				return new ResponseEntity<>(couponId, HttpStatus.OK);
			}else{
				throw new BcsNoticeException("不能領取優惠券");
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>("優惠券已被領取完畢", HttpStatus.NOT_IMPLEMENTED); 
			}else{
				return new ResponseEntity<>("不能領取優惠券", HttpStatus.NOT_IMPLEMENTED); 
			}
		}
		
		
	}

	/**
	 * Get RewardCard List From DB
	 * 
	 * @param sessionMID
	 * @param contentRewardCards
	 */
	private void getRewardCardListFromDB(String sessionMID, Map<String, ContentRewardCard> contentRewardCards) {

		// Get BCS Reward Card REWARDCARD_STATUS_ACTIVE
		List<ContentRewardCard> bcsRewardCards = contentRewardCardService
				.findByStatus(ContentRewardCard.REWARD_CARD_STATUS_ACTIVE);
		if (bcsRewardCards != null && bcsRewardCards.size() > 0) {

			for (ContentRewardCard contentRewardCard : bcsRewardCards) {
				boolean okRewardCard = true;
				boolean isGotRewardCard = actionUserRewardCardUIService.isGetRewardCard(sessionMID, contentRewardCard.getRewardCardId());

				// 不能領取的集點卡
				if (!isGotRewardCard && contentRewardCard.getRewardCardFlag().equals(ContentRewardCard.REWARD_CARD_FLAG_PRIVATE)) {
					 okRewardCard = false;
					 continue;
				}

				// 驗證領用
				boolean inGetTime = false;
				// 驗證領用期間、領用次數限制
				Boolean isInGetTime =actionUserRewardCardUIService.isInGetTime(contentRewardCard);
				String errorMessage =(isInGetTime==true)?"":"getTimeError";

				if (StringUtils.isNotBlank(errorMessage)) {
					// 不能領用集點卡
				} else {
					inGetTime = true;
				}

				// 預時消失
				boolean isUseTime = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_REWARDCARD_USE_TIME);
				boolean inUsingTime = false;
				
								
				// Get Setting From ActionUserRewardCard
				if (isGotRewardCard == true) {
					if (isUseTime) {
						ActionUserRewardCard getRecord = actionUserRewardCardUIService.findByMidAndRewardCardIdAndActionType(sessionMID, contentRewardCard.getRewardCardId());
						if (getRecord != null) {
							inUsingTime = actionUserRewardCardUIService.isInUsingTime(getRecord);
						} else {
							inUsingTime = actionUserRewardCardUIService.isInUsingTime(contentRewardCard);
						}
					} else {
						inUsingTime = actionUserRewardCardUIService.isInUsingTime(contentRewardCard);
					}
				} else {
					inUsingTime = actionUserRewardCardUIService.isInUsingTime(contentRewardCard);
				}
				
				if (!inUsingTime && !inGetTime) {
					okRewardCard = false;
					continue;
				}

				if (okRewardCard) {
					// 設定 RewardCard Image Id to Resource URL
					String imageListId = contentRewardCard.getRewardCardListImageId();
					contentRewardCard.setRewardCardListImageId(
							actionUserRewardCardUIService.settingRewardCardImage(imageListId));

					// User Status Show
					String rewardCardUserStatus = rewardCardUserStatus(contentRewardCard, sessionMID);
					contentRewardCard.setStatus(rewardCardUserStatus);

					// 集點情況
					String rewardCardUserPointStatus = rewardCardUserPointStatus(contentRewardCard, sessionMID);
					contentRewardCard.setRewardCardSubTitle(rewardCardUserPointStatus);

					if (contentRewardCards.get(contentRewardCard.getRewardCardId()) == null) {
						contentRewardCards.put(contentRewardCard.getRewardCardId(), contentRewardCard);
					}
				}
			}
		}
	}

	/**
	 * @param contentRewardCard
	 * @param sessionMID
	 * @return
	 */
	private String rewardCardUserStatus(ContentRewardCard contentRewardCard, String sessionMID) {

		// 是否已領取
		boolean isGetRewardCard = actionUserRewardCardUIService.isGetRewardCard(sessionMID,
				contentRewardCard.getRewardCardId());
		if (isGetRewardCard) {
			return "-此集點卡已領取";
		}

		// 是否已領取
		boolean isUseRewardCard = actionUserRewardCardUIService.isGetRewardCard(sessionMID,
				contentRewardCard.getRewardCardId());
		if (isUseRewardCard) {
			return "-此集點卡已集點成功";
		}

		return "";
	}

	/**
	 * @param contentRewardCard
	 * @param sessionMID
	 * @return
	 */
	private String rewardCardUserPointStatus(ContentRewardCard contentRewardCard, String sessionMID) {

		String rewardCardId = contentRewardCard.getRewardCardId();

		// 集點情況
		try {
			int havePoint = mobileRewardCardService.getHavePoint(sessionMID, rewardCardId);
			if (havePoint < contentRewardCard.getRequirePoint()) {
				return "集點情況：" + havePoint + " / " + contentRewardCard.getRequirePoint();
			} else {
				return "集點情況：已完成所有集點";
			}
		} catch (Exception e) {
			return "集點情況：未領取";
		}
	}

	private void callRetrievingAPI(String code, String sessionMID, HttpServletRequest request,
			HttpServletResponse response, Model model, ContentRewardCardPoint contentRewardCardPoint, String state)
			throws Exception {

		String rewardCardId = String.valueOf(contentRewardCardPoint.getRewardCardId());

		String ChannelID = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelID.toString(), true);
		String ChannelSecret = CoreConfigReader.getString(CONFIG_STR.Default.toString(), CONFIG_STR.ChannelSecret.toString(), true);
		
		ObjectNode result = lineWebLoginApiService.callRetrievingAPI(ChannelID, ChannelSecret, code,
				UriHelper.getRewardCardValidateUri());

		if (result != null && result.get("access_token") != null) {
			String access_token = result.get("access_token").asText();
			if (StringUtils.isNotBlank(access_token)) {

				ObjectNode getProfile = lineProfileService.callGetProfileAPI(access_token);

				if (getProfile != null && getProfile.get("userId") != null
						&& StringUtils.isNotBlank(getProfile.get("userId").asText())) {
					sessionMID = getProfile.get("userId").asText();
					logger.info("callRetrievingAPI sessionMID:" + sessionMID);
					HttpSession session = request.getSession();

					session.setAttribute("MID", sessionMID);

					if (ContentRewardCardPoint.STATUS_ACTIVE.equals(contentRewardCardPoint.getStatus())) {

						ActionUserRewardCard actionUserRewardCard = actionUserRewardCardService.findByMidAndRewardCardIdAndActionType(sessionMID,contentRewardCardPoint.getRewardCardId(), ActionUserRewardCard.ACTION_TYPE_GET);

						// 如尚未領卡，則跑領卡流程
						if (actionUserRewardCard == null) {
							this.rewardCardContentPage(rewardCardId, model, request, response,MobilePageEnum.UserRewardCardIndexPage, true);
						}

						String rewardCardResult = contentRewardCardPointService.getPoint(sessionMID, state);
						logger.info("rewardCardResult:" + rewardCardResult);
						session.setAttribute("rewardCardResult", rewardCardResult);
					} else {
						session.setAttribute("rewardCardResult", "失敗:此點數QRCode已失效");
					}

					String linkUrl = UriHelper.getUserRewardCardPageUri(String.valueOf(contentRewardCardPoint.getRewardCardId()));

					response.sendRedirect(linkUrl);
					return;
				}
			}
		}
	}

}
