package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.entity.ContentRewardCardPoint;
import com.bcs.core.db.service.ActionUserRewardCardPointDetailService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentRewardCardPointService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.model.RewardCardModel;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.QrcodeGenerator;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.ContentRewardCardModel;
import com.bcs.web.ui.service.ActionUserRewardCardUIService;
import com.bcs.web.ui.service.ContentRewardCardUIService;
// import com.mysql.fabric.xmlrpc.base.Array;

@Controller
@RequestMapping("/bcs")
public class BCSRewardCardController extends BCSBaseController {

	/** Logger */
	private static Logger logger = Logger.getLogger(BCSRewardCardController.class);

	@Autowired
	private ContentRewardCardService contentRewardCardService;
	@Autowired
	private ContentRewardCardUIService contentRewardCardUIService;
	@Autowired
	private ActionUserRewardCardPointDetailService actionUserRewardCardPointDetailService;
	@Autowired
	private ContentRewardCardPointService contentRewardCardPointService;
	@Autowired
	private ActionUserRewardCardUIService actionUserRewardCardUIService;
	@Autowired
	private ContentCouponService contentCouponService;

	@RequestMapping(method = RequestMethod.GET, value = "/edit/rewardCardCreatePage")
	public String couponCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("rewardCardCreatePage");
		return BcsPageEnum.RewardCardCreatePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/rewardCardListPage")
	public String rewardCardListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("rewardCardListPage");
		return BcsPageEnum.RewardCardListPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/rewardCardListDisablePage")
	public String rewardCardListDisablePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("rewardCardListDisablePage");
		return BcsPageEnum.RewardCardDisableListPage.toString();
	}

	/**
	 * 查詢集點卡列表 REWARDCARD_STATUS_ACTIVE
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ControllerLog(description="查詢集點卡列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentRewardCardList")
	@ResponseBody
	public ResponseEntity<?> getContentCouponList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getContentCouponList");

		try {
			List<ContentRewardCard> contentRewardCards = contentRewardCardService
					.findByStatus(ContentRewardCard.REWARD_CARD_STATUS_ACTIVE);
			return new ResponseEntity<>(contentRewardCards, HttpStatus.OK);
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
	 * 查詢集點卡列表 REWARDCARD_STATUS_ACTIVE
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ControllerLog(description="集點卡列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentRewardModelCardList")
	@ResponseBody
	public ResponseEntity<?> getContentRewardModelCardList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getContentRewardModelCardList");

		try {
			List<ContentRewardCard> contentRewardCards = contentRewardCardService
					.findByStatus(ContentRewardCard.REWARD_CARD_STATUS_ACTIVE);
			List<ContentRewardCardModel> contentRewardCardModels = new ArrayList<>();
			for (ContentRewardCard contentRewardCard : contentRewardCards) {
				List<ContentCoupon> contentCouponList = contentCouponService.findByEventReferenceAndEventReferenceId(
						ContentCoupon.EVENT_REFERENCE_REWARD_CARD, contentRewardCard.getRewardCardId());

				ContentRewardCardModel contentRewardCardModel = new ContentRewardCardModel();
				contentRewardCardModel.setContentRewardCard(contentRewardCard);
				contentRewardCardModel.setContentCouponList(contentCouponList);

				contentRewardCardModels.add(contentRewardCardModel);
			}
			logger.info("◎ 集點卡列表：" + contentRewardCardModels);
			return new ResponseEntity<>(contentRewardCardModels, HttpStatus.OK);
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
	 * 查詢優惠劵列表 COUPON_STATUS_DISABLE
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@ControllerLog(description="查詢優惠劵列表 COUPON_STATUS_DISABLE")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentRewardCardListDisable")
	@ResponseBody
	public ResponseEntity<?> getContentCouponListDisable(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getContentRewardCardListDisable");

		try {
			List<ContentRewardCard> contentRewardCards = contentRewardCardService
					.findByStatus(ContentRewardCard.REWARD_CARD_STATUS_DISABLE);
			List<ContentRewardCardModel> contentRewardCardModels = new ArrayList<>();

			for (ContentRewardCard contentRewardCard : contentRewardCards) {
				List<ContentCoupon> contentCouponList = contentCouponService.findByEventReferenceAndEventReferenceId(
						ContentCoupon.EVENT_REFERENCE_REWARD_CARD, contentRewardCard.getRewardCardId());

				ContentRewardCardModel contentRewardCardModel = new ContentRewardCardModel();
				contentRewardCardModel.setContentRewardCard(contentRewardCard);
				contentRewardCardModel.setContentCouponList(contentCouponList);

				contentRewardCardModels.add(contentRewardCardModel);
			}

			return new ResponseEntity<>(contentRewardCardModels, HttpStatus.OK);
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
	 * 取得集點卡
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取得集點卡")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentRewardCard")
	@ResponseBody
	public ResponseEntity<?> getContentRewardCard(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String rewardCardId
			) throws IOException {
		logger.info("getContentRewardCard");

		try {
			if (rewardCardId != null) {
				logger.info("rewardCardId:" + rewardCardId);
				ContentRewardCard contentRewardCard = contentRewardCardService.findOne(rewardCardId);
				ContentRewardCardModel contentRewardCardModel = new ContentRewardCardModel();
				contentRewardCardModel.setContentRewardCard(contentRewardCard);
				List<ContentCoupon> contentCouponList = contentCouponService.findByEventReferenceAndEventReferenceId(
						ContentCoupon.EVENT_REFERENCE_REWARD_CARD, contentRewardCard.getRewardCardId());
				contentRewardCardModel.setContentCouponList(contentCouponList);
				if (contentRewardCard != null) {
					return new ResponseEntity<>(contentRewardCardModel, HttpStatus.OK);
				}
			}

			throw new Exception("RewardCardId Null");
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
	 * 新增或修改集點卡
	 * 
	 * @param contentRewardCard
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="新增或修改集點卡")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/saveContentRewardCard", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> saveContentRewardCard(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody ContentRewardCardModel contentRewardCardModel
			)
			throws IOException {
		logger.info("saveContentRewardCard:" + contentRewardCardModel);
		try {
			contentRewardCardUIService.checkContentRewardCard(contentRewardCardModel.getContentRewardCard());
			contentRewardCardUIService.saveFromUI(contentRewardCardModel, customUser.getAccount());
			return new ResponseEntity<>(null, HttpStatus.OK);
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
	 * 刪除集點卡
	 * 
	 * @param inputLineAccount
	 * @param adminUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="刪除集點卡")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteContentRewardCard")
	@ResponseBody
	public ResponseEntity<?> deleteContentRewardCard(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String rewardCardId
			)
			throws IOException {
		logger.info("deleteContentCoupon");

		try {
			contentRewardCardUIService.deleteFromUI(rewardCardId, customUser.getAccount());
			return new ResponseEntity<>("Delete Success", HttpStatus.OK);
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
	 * 改變訊息狀態
	 * 
	 * @param customUser
	 * @param request
	 * @param response
	 * @return String
	 * @throws IOException
	 */
	@ControllerLog(description="改變訊息狀態")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/redesignContentRewardCard")
	@ResponseBody
	public ResponseEntity<?> redesignContentCoupon(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String rewardCardId
			)
			throws IOException {
		logger.info("redesignContentCoupon");
		try {
			if (rewardCardId != null) {
				logger.info("rewardCardId:" + rewardCardId);
				contentRewardCardUIService.switchContentCouponStatus(rewardCardId, customUser.getAccount());

				return new ResponseEntity<>("Change Success", HttpStatus.OK);
			} else {
				logger.error("rewardCardId Null");
				throw new BcsNoticeException("請選擇正確的集點卡");
			}
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
	 * 取得贈點頁面
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/edit/rewardCardPointRecordPage")
	public String WinnerListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("rewardCardPointRecordPage");
		return BcsPageEnum.RewardCardPointRecordPage.toString();
	}

	/**
	 * 取得贈點名單
	 */
	@ControllerLog(description="取得贈點名單")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getRecordList/{rewardCardId}/{pageIndex}")
	@ResponseBody
	public ResponseEntity<?> getRecordList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String rewardCardId,
			@PathVariable Integer pageIndex) throws IOException {
		logger.info("getRecordList");
		try {
			List<RewardCardModel> result = actionUserRewardCardPointDetailService.getRecordListByRewardCardId(rewardCardId,pageIndex);
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得贈點名單
	 */
	@ControllerLog(description="取得贈點名單")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getRecordList/{rewardCardId}/{startDate}/{endDate}/{pageIndex}")
	@ResponseBody
	public ResponseEntity<?> getRecordList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String rewardCardId,
			@PathVariable String startDate, 
			@PathVariable String endDate,
			@PathVariable Optional<Integer> pageIndex)
			throws IOException {
		logger.info("getRecordList");
		try {
			List<RewardCardModel> result = actionUserRewardCardPointDetailService.getRecordListByRewardCardId(rewardCardId, startDate, endDate, pageIndex);
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 手動新增使用者集點卡點數
	 * 
	 * @param contentRewardCard
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="手動新增使用者集點卡點數")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/manuallyCreateRewardCardPoint")
	@ResponseBody
	public ResponseEntity<?> manuallyCreateRewardCardPoint(		
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestPart MultipartFile filePart,
			@RequestParam String rewardCardId
			) throws IOException {
		try {
			logger.info("manuallyCreateRewardCardPoint");
			Map<String, Object> result = actionUserRewardCardUIService.manuallyCreateRewardCardPoint(rewardCardId,
					filePart, new Date());
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if (e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@ControllerLog(description="取得贈點紀錄最大頁面數")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getRewardCardRecordMaxPage")
	@ResponseBody
	public ResponseEntity<?> getWinnerMaxPage(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String rewardCardId) throws IOException {
		logger.info("getWinnerMaxPage");
		try{
			Integer MaxPage = actionUserRewardCardPointDetailService.getRewardCardPointMaxPageByRewardCardId(rewardCardId);
			return new ResponseEntity<>(MaxPage, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ControllerLog(description="rewardCardQRCodePage")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/rewardCardQRCodePage")
	public String rewardCardQRCodePage(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String rewardCardId,
			Model model) {
		logger.info("rewardCardQRCodePage rewardCardId:" + rewardCardId);

		List<ContentRewardCardPoint> list = contentRewardCardPointService.findByRewardCardIdAndStatus(rewardCardId, ContentRewardCardPoint.STATUS_ACTIVE);
		Map<String, String> map = new LinkedHashMap<>();

		for (ContentRewardCardPoint o : list) {
			map.put(UriHelper.getRewardCardGetPointUri(o.getRewardCardPointId()),
					UriHelper.getGeneratorQRCodeUri(o.getRewardCardPointId()));
		}

		model.addAttribute("QRMap", map);

		return BcsPageEnum.RewardCardQRCodePage.toString();
	}
	
	@ControllerLog(description="generatorQRCode")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/generatorQRCode/{rewardCardPointId}")
	public void generatorQRCode(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String rewardCardPointId) {
		logger.info("generatorQRCode rewardCardPointId:" + rewardCardPointId);
		response.setContentType("image/png");
		try {
			QrcodeGenerator.generateQrcode(UriHelper.getRewardCardGetPointUri(rewardCardPointId),
					response.getOutputStream());
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}
	}

	/*
	 * @RequestMapping(method = RequestMethod.GET, value =
	 * "/admin/countCouponReport")
	 * 
	 * @ResponseBody public ResponseEntity<?> countCouponReport(HttpServletRequest
	 * request,HttpServletResponse response) throws Exception {
	 * logger.info("countCouponReport");
	 * 
	 * try { String couponId = request.getParameter("couponId"); String startDate =
	 * request.getParameter("startDate"); String endDate =
	 * request.getParameter("endDate");
	 * 
	 * ContentCoupon contentCoupon = null; // Validate if(couponId != null &&
	 * StringUtils.isNotBlank(couponId.toString())){ contentCoupon =
	 * contentCouponService.findOne(Long.parseLong(couponId)); if(contentCoupon !=
	 * null){ // Pass } else{ throw new BcsNoticeException("輸入錯誤"); } } else{ throw
	 * new BcsNoticeException("輸入錯誤"); }
	 * 
	 * if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
	 * Map<String, Map<String, Map<String, Long>>> result = new HashMap<String,
	 * Map<String, Map<String, Long>>>();
	 * 
	 * Map<String, Map<String, Long>> resultGet =
	 * contentCouponReportService.getCouponGetReport(startDate, endDate,
	 * Long.parseLong(couponId)); result.put("Get", resultGet);
	 * 
	 * Map<String, Map<String, Long>> resultUse =
	 * contentCouponReportService.getCouponUseReport(startDate, endDate,
	 * Long.parseLong(couponId)); result.put("Use", resultUse);
	 * 
	 * Map<String, Map<String, Long>> CouponTitle = new HashMap<String, Map<String,
	 * Long>>(); CouponTitle.put(contentCoupon.getCouponTitle(), null);
	 * result.put("CouponTitle", CouponTitle);
	 * 
	 * return new ResponseEntity<>(result, HttpStatus.OK); } else { if
	 * (StringUtils.isBlank(startDate)) { logger.error("startDate null"); throw new
	 * BcsNoticeException("缺少查詢起始日期"); } else { logger.error("endDate null"); throw
	 * new BcsNoticeException("缺少查詢結束日期"); } } } catch (Exception e) {
	 * logger.error(ErrorRecord.recordError(e));
	 * 
	 * if(e instanceof BcsNoticeException) { return new
	 * ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED); } else{ return
	 * new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); } } }
	 */

	/**
	 * 匯出 Coupon Report EXCEL MID
	 */
	/*
	 * @RequestMapping(method = RequestMethod.GET, value =
	 * "/edit/exportMidForCouponReport")
	 * 
	 * @ResponseBody public void exportMidForCouponReport(HttpServletRequest
	 * request, HttpServletResponse response) throws Exception{ String startDate =
	 * request.getParameter("startDate"); String endDate =
	 * request.getParameter("endDate"); String couponId =
	 * request.getParameter("couponId");
	 * 
	 * ContentCoupon contentCoupon =
	 * contentCouponService.findOne(Long.parseLong(couponId));
	 * 
	 * if(contentCoupon == null){ throw new Exception("CouponId Error"); }
	 * 
	 * if(couponId != null && StringUtils.isNotBlank(couponId.toString()) &&
	 * StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
	 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); Date timeStart =
	 * sdf.parse(startDate);
	 * 
	 * Date timeEnd = sdf.parse(endDate); Calendar calendarEnd =
	 * Calendar.getInstance(); calendarEnd.setTime(timeEnd);
	 * calendarEnd.add(Calendar.DATE, 1);
	 * 
	 * List<String> getMids =
	 * actionUserCouponService.findMidByCouponIdAndActionTypeAndTime(contentCoupon.
	 * getCouponId(), ActionUserCoupon.ACTION_TYPE_GET, timeStart,
	 * calendarEnd.getTime());
	 * 
	 * List<String> useMids =
	 * actionUserCouponService.findMidByCouponIdAndActionTypeAndTime(contentCoupon.
	 * getCouponId(), ActionUserCoupon.ACTION_TYPE_USE, timeStart,
	 * calendarEnd.getTime());
	 * 
	 * List<String> getNotUseMids = new ArrayList<String>(); for(String mid :
	 * getMids){ if(!useMids.contains(mid)){ getNotUseMids.add(mid); } }
	 * 
	 * List<String> titles = new ArrayList<String>(); titles.add("領取MID");
	 * titles.add("使用MID"); titles.add("領取未使用MID");
	 * 
	 * List<List<String>> data = new ArrayList<List<String>>(); data.add(getMids);
	 * data.add(useMids); data.add(getNotUseMids);
	 * 
	 * String time = startDate + "~" + endDate;
	 * exportExcelUIService.exportMidResultToExcel(request, response,
	 * "CouponReport", contentCoupon.getCouponTitle() + "優惠券" , time, titles, data);
	 * } else{
	 * 
	 * throw new Exception("Time Range Error"); } }
	 */
}
