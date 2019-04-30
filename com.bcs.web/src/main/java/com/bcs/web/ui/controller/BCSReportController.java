package com.bcs.web.ui.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.service.PushMessageRecordService;
import com.bcs.core.db.entity.PushReport;
import com.bcs.core.db.service.ContentReportService;
import com.bcs.core.db.service.PushReportService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.ExportExcelUIService;


@Controller
@RequestMapping("/bcs")
public class BCSReportController extends BCSBaseController {
	@Autowired
	private ContentReportService contentReportService;
	@Autowired
	private PushReportService pushReportService;
	@Autowired
	private ExportExcelUIService exportExcelUIService;
	@Autowired
	private UserTraceLogService userTraceLogService;
	@Autowired
	private PushMessageRecordService pushMessageRecordService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSReportController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/admin/reportPage")
	public String reportPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("reportPage");
		return BcsPageEnum.ReportPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/reportCreatePage")
	public String reportCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("reportCreatePage");
		return BcsPageEnum.ReportCreatePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/reportLinkClickPage")
	public String reportLinkClickPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("reportLinkClickPage");
		return BcsPageEnum.ReportLinkClickPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/reportLinkClickDetailPage")
	public String reportLinkClickDetailPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("reportLinkClickDetailPage");
		return BcsPageEnum.ReportLinkClickDetailPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/reportPageVisitPage")
	public String reportPageVisitPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("reportPageVisitPage");
		return BcsPageEnum.ReportPageVisitPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/reportPageVisitDetailPage")
	public String reportPageVisitDetailPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("reportPageVisitDetailPage");
		return BcsPageEnum.ReportPageVisitDetailPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/reportPushApiEffectsPage")
	public String reportPushApiEffectsPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("reportPushApiEffectsPage");
		return BcsPageEnum.ReportPushApiEffectsPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/keywordAndInteractiveReportPage")
	public String keywordAndInteractiveReportPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("keywordAndInteractiveReportPage");
		return BcsPageEnum.KeywordAndInteractiveReportPage.toString();
	}
	
	
	
	/**
	 * 取得發送報告清單
	 */
	@ControllerLog(description="取得發送報告清單")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getPushReportList")
	@ResponseBody
	public ResponseEntity<?> getPushReportList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate, 
			@RequestParam String endDate) {
		logger.info("getReportListByPushTime");
		
		try{
			List<List<String>> result = contentReportService.getPushReportList(startDate, endDate);
			
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
	 * 取得特定發送報告
	 */
	@ControllerLog(description="取得特定發送報告")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getPushReport")
	@ResponseBody
	public ResponseEntity<?> getPushReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam Long reportId, 
			@RequestParam String actionType) {
		logger.info("getReport");
		
		try{
			if (actionType.equals("Edit")) {
				PushReport result = contentReportService.getPushReportForEdit(reportId);
				return new ResponseEntity<>(result, HttpStatus.OK);
			} else {
				Object[] result = contentReportService.getPushReportForDialog(reportId);
				return new ResponseEntity<>(result, HttpStatus.OK);
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
	 * 取得圖文訊息發送報告
	 */
	@ControllerLog(description="取得圖文訊息發送報告")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getPushReportForRichMsg")
	@ResponseBody
	public ResponseEntity<?> getPushReportForRichMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String richId) {
		logger.info("getRichPushReport");
		
		try{
			PushReport result = contentReportService.getPushReportForRichMsg(richId);
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
	 * 新增與更新發送報告
	 */
	@ControllerLog(description="新增與更新發送報告")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/savePushReport", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> savePushReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@RequestBody PushReport pushReport,
			@RequestParam String actionType) throws IOException {
		try {
			String adminUserAccount = customUser.getAccount(); //取得登入者的帳號
			
			contentReportService.savePushReport(pushReport, actionType, adminUserAccount);
			
			String result = "建立發送報告成功";
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
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
	 * 刪除發送報告
	 */
	@ControllerLog(description="刪除發送報告")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deletePushReport")
	@ResponseBody
	public ResponseEntity<?> deletePushReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,			
			@RequestParam Long reportId) throws IOException {
		logger.info("deletePushReport");
		
		// Check Delete Right
		boolean isAdmin = customUser.isAdmin();
		if(isAdmin) {
			try {
				contentReportService.deletePushReport(reportId);
				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
			}
			catch(Exception e) {
				logger.error(ErrorRecord.recordError(e));
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>("User No Delete Right", HttpStatus.OK);
		}
	}
	
	/**
	 * 計算平均點擊率(以週為單位)
	 */
	@ControllerLog(description="計算平均點擊率(以週為單位)")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getClickRateForWeek")
	@ResponseBody
	public ResponseEntity<?> getClickRateForWeek(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
    		@RequestParam String endDate) {
		logger.info("getClickRateForWeek");
		
		try {			
			List<String> result = this.contentReportService.getClickRateForWeek(startDate, endDate);
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
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
	 * 計算平均點擊率(以日為單位)
	 */
	@ControllerLog(description="計算平均點擊率(以日為單位)")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getClickRateForDay")
	@ResponseBody
	public ResponseEntity<?> getClickRateForDay(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
    		@RequestParam String endDate) {
		logger.info("getClickRateForDay");
		
		try {
			
			List<Map<String, Map<String, BigDecimal>>> result = this.contentReportService.getClickRateForDay(startDate, endDate);
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@ControllerLog(description="exportToExcelForClickUrl")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForClickUrl")
	@ResponseBody
	public void exportToExcelForClickUrl(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String reportId) throws Exception {
		logger.info("exportToExcelForClickUrl");
		
		if(StringUtils.isNotBlank(reportId)){
				logger.info("reportId:" + reportId);
				
				PushReport pushReport = pushReportService.findOne(Long.parseLong(reportId));
				
				String linkId = pushReport.getReportLinkId();
				Date pushTime = pushReport.getReportPushTime();

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String pushTimeStr = sdf.format(pushTime);
				String nowStr = sdf.format(new Date());
				
				List<String> clickLinkMids = userTraceLogService.findMidByReferenceId(linkId, pushTimeStr);
				
				String title = pushReport.getReportPushUrl();
				
				if(clickLinkMids != null){
					
					List<String> titles = new ArrayList<String>();
					titles.add("點擊人UID");
					List<List<String>> data = new ArrayList<List<String>>();
					data.add(clickLinkMids);
	
					String time = pushTimeStr + "~" + nowStr ;
					exportExcelUIService.exportMidResultToExcel(request, response, "ReportClickUrl", "點擊連結:" + title , time, titles, data);
					return;
				}
		 }
		 throw new Exception("資料產生錯誤");
	}
	
	@ControllerLog(description="getPushApiEffects")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getPushApiEffects")
	@ResponseBody
	public ResponseEntity<?> getPushApiEffects(
			HttpServletRequest request, 
			HttpServletResponse response, 
			@RequestParam(value = "startDate", required = true) String startDate, 
			@RequestParam(value = "endDate", required = true) String endDate) {
		logger.info("-------------------- getPushApiEffects --------------------");
		logger.info("[getPushApiEffects] startDate: " + startDate);
		logger.info("[getPushApiEffects] endDate: " + endDate);
		
		try {
			return new ResponseEntity<>(pushMessageRecordService.getPushMessageEffects(startDate, endDate), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
