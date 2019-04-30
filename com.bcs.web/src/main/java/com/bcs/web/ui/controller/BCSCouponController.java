package com.bcs.web.ui.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.ContentCouponCodeService;
import com.bcs.core.db.service.ContentCouponService;
import com.bcs.core.db.service.ContentGameService;
import com.bcs.core.db.service.ContentRewardCardService;
import com.bcs.core.db.service.WinnerListService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.model.WinnedCouponModel;
import com.bcs.core.report.service.ContentCouponReportService;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.ContentCouponModel;
import com.bcs.web.ui.service.ContentCouponUIService;
import com.bcs.web.ui.service.ExportExcelUIService;


@Controller
@RequestMapping("/bcs")
public class BCSCouponController extends BCSBaseController {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSCouponController.class);

	@Autowired
	private ContentCouponService contentCouponService;
	@Autowired
	private ContentCouponUIService contentCouponUIService;
	@Autowired
	private ContentCouponReportService contentCouponReportService;
	@Autowired
	private ExportExcelUIService exportExcelUIService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	@Autowired
	ContentCouponCodeService contentCouponCodeService;
	@Autowired
	WinnerListService winnerListService;
	@Autowired
	ContentRewardCardService contentRewardCardService;
	@Autowired
	ContentGameService contentGameService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/couponCreatePage")
	public String couponCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("couponCreatePage");
		return BcsPageEnum.CouponCreatePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/couponListPage")
	public String couponListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("couponListPage");		
		return BcsPageEnum.CouponListPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/couponListDisablePage")
	public String couponListDisablePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("couponListDisablePage");		
		return BcsPageEnum.CouponListDisablePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/couponReportPage")
	public String couponReportPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("couponReportPage");		
		return BcsPageEnum.CouponReportPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/couponListApiPage")
	public String couponListApiPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("couponListApiPage");		
		return BcsPageEnum.CouponListApiPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/couponSerialNumberPage")
	public String couponSerialNumberPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("couponSerialNumberPage");		
		return BcsPageEnum.CouponSerialNumberPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/couponWinnerListPage")
	public String couponWinnerListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("couponWinnerListPage");		
		return BcsPageEnum.CouponWinnerListPage.toString();
	}
	
	/**
	 * 查詢優惠劵列表 COUPON_STATUS_ACTIVE
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	@ControllerLog(description="查詢優惠劵列表 COUPON_STATUS_ACTIVE")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentCouponList")
	@ResponseBody
	public ResponseEntity<?> getContentCouponList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getContentCouponList");
		
		try {
			List<ContentCoupon> contentCoupons = contentCouponService.findByStatus(ContentCoupon.COUPON_STATUS_ACTIVE);
			return new ResponseEntity<>(contentCoupons, HttpStatus.OK);
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
	
	@ControllerLog(description="getContentCouponListAndEventReference")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentCouponListAndEventReference")
	@ResponseBody
	public ResponseEntity<?> getContentCouponListAndEventReference(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getContentCouponListAndEventReference");
		
		try {
			List<ContentCoupon> contentCoupons = contentCouponService.findByStatus(ContentCoupon.COUPON_STATUS_ACTIVE);
			List <ContentCouponModel> contentCouponModelList = new ArrayList<>();
			for(ContentCoupon contentCoupon:contentCoupons){
				String eventReferenceTitle = null;
				if(contentCoupon.getEventReferenceId() != null){
					switch(contentCoupon.getEventReference()){
					case ContentCoupon.EVENT_REFERENCE_REWARD_CARD:
						ContentRewardCard contentRewardCard = contentRewardCardService.findOne(contentCoupon.getEventReferenceId());
						eventReferenceTitle = contentRewardCard.getRewardCardMainTitle();
						break;
					case ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD:
						ContentGame contentGame = contentGameService.findOne(contentCoupon.getEventReferenceId());
						eventReferenceTitle =contentGame.getGameName();
						break;
					}
				}
				
				ContentCouponModel contentCouponModel = new ContentCouponModel();
				contentCouponModel.setContentCoupon(contentCoupon);
				contentCouponModel.setEventReferenceTitle(eventReferenceTitle);
				contentCouponModelList.add(contentCouponModel);
			}
			return new ResponseEntity<>(contentCouponModelList, HttpStatus.OK);
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
	 * 查詢優惠劵列表 COUPON_STATUS_DISABLE
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	@ControllerLog(description="查詢優惠劵列表 COUPON_STATUS_DISABLE")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentCouponListDisable")
	@ResponseBody
	public ResponseEntity<?> getContentCouponListDisable(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getContentCouponListDisable");
		
		try {
			List<ContentCoupon> contentCoupons = contentCouponService.findByStatus(ContentCoupon.COUPON_STATUS_DISABLE);
			
			List <ContentCouponModel> contentCouponModelList = new ArrayList<>();
			for(ContentCoupon contentCoupon:contentCoupons){
				String eventReferenceTitle = null;
				if(contentCoupon.getEventReferenceId() != null){
					switch(contentCoupon.getEventReference()){
					case ContentCoupon.EVENT_REFERENCE_REWARD_CARD:
						ContentRewardCard contentRewardCard = contentRewardCardService.findOne(contentCoupon.getEventReferenceId());
						eventReferenceTitle = contentRewardCard.getRewardCardMainTitle();
						break;
					case ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD:
						ContentGame contentGame = contentGameService.findOne(contentCoupon.getEventReferenceId());
						eventReferenceTitle =contentGame.getGameName();
						break;
					}
				}
				
				ContentCouponModel contentCouponModel = new ContentCouponModel();
				contentCouponModel.setContentCoupon(contentCoupon);
				contentCouponModel.setEventReferenceTitle(eventReferenceTitle);
				contentCouponModelList.add(contentCouponModel);
			}
			return new ResponseEntity<>(contentCouponModelList, HttpStatus.OK);
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
	 * 查詢未使用過的優惠券 (不包含當下刮刮卡的)
	 * 
	 * @param request
	 * @param response
	 * @return gameId
	 * @throws Exception 
	 */
	@ControllerLog(description="查詢未使用過的優惠券 (不包含當下刮刮卡的)")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getUnusedContentCouponList")
	@ResponseBody
	public ResponseEntity<?> getUnusedContentCouponList(
			HttpServletRequest request, 
			HttpServletResponse response, 
			@CurrentUser CustomUser customUser,
			@RequestParam String gameId) {
		logger.info("getContentCouponList");
		
		try {
			if(gameId == null) {
				logger.info("◎ 建立優惠券：撈取沒有被使用過且狀態為隱藏的優惠券");
				return new ResponseEntity<>(contentCouponService.findUnusedContentCouponList(), HttpStatus.OK);
			} else {
				logger.info("◎ 修改優惠券：撈取屬於自己以及沒有被使用過且狀態為隱藏的優惠券");
				return new ResponseEntity<>(contentCouponService.findUnusedContentCouponList(gameId), HttpStatus.OK);
			}
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
	 * Setting Coupon ImageId to Url
	 * @param imageId
	 * @return
	 */
	public String settingCouponImage(String imageId){
		
		if(StringUtils.isNotBlank(imageId)){
			if(imageId.toLowerCase().startsWith("http")){
				
			}
			else{
				imageId = UriHelper.getResourceUri("IMAGE", imageId, true);
			}
		}
		
		return imageId;
	}
	
	/**
	 * 取得優惠劵
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取得優惠劵")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getContentCoupon")
	@ResponseBody
	public ResponseEntity<?> getContentCoupon(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String couponId  
			) throws IOException {
		logger.info("getContentCoupon");				
		
		try{
			if(couponId != null){
				logger.info("couponId:" + couponId);
				ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
				
				if(contentCoupon != null){
					return new ResponseEntity<>(contentCoupon, HttpStatus.OK);
				}
			}
			
			throw new Exception("CouponId Null");
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
	 * 新增或修改優惠劵
	 * 
	 * @param contentCoupon
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="新增或修改優惠劵")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/saveContentCoupon",consumes = {"multipart/form-data"})
	@ResponseBody
	public ResponseEntity<?> saveContentCoupon(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestPart("contentCoupon") @Valid ContentCouponModel contentCouponModel,
			@RequestPart(value="couponCodeListData", required=false)  MultipartFile couponCodeListData
			) throws IOException {
		logger.info("saveContentCoupon");
		
		try{
			logger.info("@@@@ contentCouponModel:"+contentCouponModel);
			
			contentCouponUIService.checkContentCoupon(contentCouponModel);
			contentCouponUIService.saveFromUI(contentCouponModel ,couponCodeListData, customUser.getAccount());
			return new ResponseEntity<>(null, HttpStatus.OK);
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
	 * 刪除優惠劵
	 * 
	 * @param inputLineAccount
	 * @param adminUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="刪除優惠劵")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteContentCoupon")
	@ResponseBody
	public ResponseEntity<?> deleteContentCoupon(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String couponId
			) throws IOException {
		logger.info("deleteContentCoupon");
		
		try{
			contentCouponUIService.deleteFromUI(couponId, customUser.getAccount());
			return new ResponseEntity<>("Delete Success", HttpStatus.OK);
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
	 * 改變訊息
	 * 
	 * @param customUser
	 * @param request
	 * @param response
	 * @return String
	 * @throws IOException
	 */
	@ControllerLog(description="改變訊息")
	@RequestMapping(method = RequestMethod.DELETE, value ="/edit/redesignContentCoupon")
	@ResponseBody
	public ResponseEntity<?> redesignContentCoupon(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String couponId
			) throws IOException {
		logger.info("redesignContentCoupon");

//		String couponId = request.getParameter("couponId");
		
		try{
			if(StringUtils.isNotBlank(couponId)){
				logger.info("couponId:" + couponId);
				contentCouponUIService.switchContentCouponStatus(couponId, customUser.getAccount());
				
				return new ResponseEntity<>("Change Success", HttpStatus.OK);
			}
			else{
				logger.error("couponId Null");
				throw new BcsNoticeException("請選擇正確的優惠券");
			}
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

	@ControllerLog(description="countCouponReport")
	@RequestMapping(method = RequestMethod.GET, value = "/admin/countCouponReport")
	@ResponseBody
	public ResponseEntity<?> countCouponReport(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String couponId,
			@RequestParam String startDate,
			@RequestParam String endDate) throws Exception {
		logger.info("countCouponReport");
		
		try {			
			ContentCoupon contentCoupon = null;
			// Validate
			if(couponId != null && StringUtils.isNotBlank(couponId.toString())){
				contentCoupon = contentCouponService.findOne(couponId);
				if(contentCoupon != null){
					// Pass
				}
				else{
					throw new BcsNoticeException("輸入錯誤");
				}
			}
			else{
				throw new BcsNoticeException("輸入錯誤");
			}
			
			if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
				Map<String, Map<String, Map<String, Long>>> result = new HashMap<String, Map<String, Map<String, Long>>>();
				
				Map<String, Map<String, Long>> resultGet = contentCouponReportService.getCouponGetReport(startDate, endDate, couponId);
				result.put("Get", resultGet);
				
				Map<String, Map<String, Long>> resultUse = contentCouponReportService.getCouponUseReport(startDate, endDate, couponId);
				result.put("Use", resultUse);
				
				Map<String, Map<String, Long>> CouponTitle = new HashMap<String, Map<String, Long>>();
				CouponTitle.put(contentCoupon.getCouponTitle(), null);
				result.put("CouponTitle", CouponTitle);
				
				return new ResponseEntity<>(result, HttpStatus.OK);
			} else {
				if (StringUtils.isBlank(startDate)) {
					logger.error("startDate null");
					throw new BcsNoticeException("缺少查詢起始日期");
				} else {
					logger.error("endDate null");
					throw new BcsNoticeException("缺少查詢結束日期");
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			
			if(e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	/**
	 * 匯出 Coupon Report EXCEL MID
	 */
	@ControllerLog(description="匯出 Coupon Report EXCEL MID")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportMidForCouponReport")
	@ResponseBody
	public void exportMidForCouponReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String couponId,
			@RequestParam String startDate,
			@RequestParam String endDate
			) throws Exception{

		ContentCoupon contentCoupon = contentCouponService.findOne(couponId);
			
		if(contentCoupon == null){
			throw new Exception("CouponId Error");
		}
		
		if(couponId != null && StringUtils.isNotBlank(couponId.toString()) && StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date timeStart = sdf.parse(startDate);
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);
			
			List<String> getMids = actionUserCouponService.findMidByCouponIdAndActionTypeAndTime(contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_GET, timeStart, calendarEnd.getTime());

			List<String> useMids = actionUserCouponService.findMidByCouponIdAndActionTypeAndTime(contentCoupon.getCouponId(), ActionUserCoupon.ACTION_TYPE_USE, timeStart, calendarEnd.getTime());

			List<String> getNotUseMids = new ArrayList<String>();
			for(String mid : getMids){
				if(!useMids.contains(mid)){
					getNotUseMids.add(mid);
				}
			}
			
			List<String> titles = new ArrayList<String>();
			titles.add("領取UID");
			titles.add("使用UID");
			titles.add("領取未使用UID");
			
			List<List<String>> data = new ArrayList<List<String>>();
			data.add(getMids);
			data.add(useMids);
			data.add(getNotUseMids);
			
			String time = startDate + "~" + endDate;
			exportExcelUIService.exportMidResultToExcel(request, response, "CouponReport", contentCoupon.getCouponTitle() + "優惠券" , time, titles, data);
		}
		else{

			throw new Exception("Time Range Error");
		}
	}
	
	
	/**
	 * 上傳電子優惠券序號的數量
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/edit/countNumberForCouponCodeListFile",consumes = {"multipart/form-data"})
	@ResponseBody
	public ResponseEntity<?> countNumberForCouponCodeListFile(
			@RequestPart(value="couponCodeListData", required=false)  MultipartFile couponCodeListData,
			@CurrentUser CustomUser customUser, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("countNumberForCouponCodeListFile");
		try{
			int couponCodeListNumber = contentCouponUIService.countNumberForCouponCodeListFile(couponCodeListData);
			return new ResponseEntity<>(couponCodeListNumber, HttpStatus.OK);
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
	 * 取得電子優惠券序號的數量
	 */
	@ControllerLog(description="取得電子優惠券序號的數量")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getCouponCodeListNumber")
	@ResponseBody
	public ResponseEntity<?> getCouponCodeListNumber(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String couponId
			) throws IOException {
		logger.info("countNumberForCouponCodeListFile");
		try{
			int couponCodeListNumber = contentCouponCodeService.findCouponCodeListNumber(couponId);
			return new ResponseEntity<>(couponCodeListNumber, HttpStatus.OK);
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
	 * 優惠券電子序號列表
	 */
	@ControllerLog(description="優惠券電子序號列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getCouponCodeList/{couponId}/{pageIndex}")
	@ResponseBody
	public ResponseEntity<?> getCouponCodeList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String couponId,
			@PathVariable Integer pageIndex) throws IOException {
		logger.info("getRecordList");
		try{
			Pageable pageable = new PageRequest((pageIndex>=1)?pageIndex-1:0,ContentCouponCodeService.pageSize);
			List<ContentCouponCode> result =contentCouponCodeService.findByCouponId(couponId,pageable);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 填寫資料紀錄
	 */
	@ControllerLog(description="填寫資料紀錄")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getWinnerListByCouponId/{couponId}/{index}")
	@ResponseBody
	public ResponseEntity<?> getWinnerList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String couponId,
			@PathVariable Integer index) throws IOException {
		logger.info("getWinnerList");
		try{
			List<WinnedCouponModel> winnedCouponModelList = winnerListService.getWinnerListAndCouponCodeByCouponId(couponId,index);
			return new ResponseEntity<>(winnedCouponModelList, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 填寫資料紀錄
	 */
	@ControllerLog(description="填寫資料紀錄")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getWinnerListByCouponId/{couponId}/{startDate}/{endDate}/{pageIndex}")
	@ResponseBody
	public ResponseEntity<?> getWinnerList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String couponId,
			@PathVariable String startDate,
			@PathVariable String endDate,
			@PathVariable Optional<Integer> pageIndex) throws IOException {
		logger.info("getWinnerList");
		try{
			List<WinnedCouponModel> winnedCouponModelList = winnerListService.getWinnerListAndCouponCodeByCouponId(couponId,startDate,endDate,pageIndex);
			return new ResponseEntity<>(winnedCouponModelList, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ControllerLog(description="取得填寫資料最大頁面數")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getWinnerMaxPage/{couponId}")
	@ResponseBody
	public ResponseEntity<?> getWinnerMaxPage(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String couponId) throws IOException {
		logger.info("getWinnerMaxPage");
		try{
			Integer MaxPage = winnerListService.getWinnerPageByCouponId(couponId);
			return new ResponseEntity<>(MaxPage, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ControllerLog(description="取得填寫資料最大頁面數")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getCouponSerialMaxPage/{couponId}")
	@ResponseBody
	public ResponseEntity<?> getCouponSerialMaxPage(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String couponId) throws IOException {
		logger.info("getCouponSerialMaxPage");
		try{
			Integer MaxPage = contentCouponCodeService.getCouponCodeMaxPageByCouponId(couponId);
			return new ResponseEntity<>(MaxPage, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
