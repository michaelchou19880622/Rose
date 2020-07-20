package com.bcs.web.ui.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.db.service.ContentLinkService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.report.service.ContentLinkReportService;
import com.bcs.core.report.service.PageVisitReportService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.DBResultUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.LinkClickReportModel;
import com.bcs.web.ui.model.LinkClickReportSearchModel;
import com.bcs.web.ui.model.PageVisitReportModel;
import com.bcs.web.ui.service.ExportExcelUIService;

@Controller
@RequestMapping("/bcs")
public class BCSLinkPageController extends BCSBaseController {
	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private ContentLinkReportService contentLinkReportService;
	@Autowired
	private PageVisitReportService pageVisitReportService;
	@Autowired
	private UserTraceLogService userTraceLogService;
	@Autowired
	private ContentFlagService contentFlagService;
	@Autowired
	private ExportExcelUIService exportExcelUIService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSLinkPageController.class);
	
	/**
	 * 取得連結列表
	 */
	@ControllerLog(description="取得連結列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getLinkUrlList")
	@ResponseBody
	public ResponseEntity<?> getLinkUrlList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getLinkUrlList");

		try{ 
			List<ContentLink> result = new ArrayList<ContentLink>();
			
//			// Add Coupon List Page
//			ContentLink couponListPage = new ContentLink();
//			couponListPage.setLinkUrl(UriHelper.getCouponListPagePattern());
//			couponListPage.setLinkTitle("我的優惠列表");
//			result.add(couponListPage);
//
//			// Add Reward Card List Page
//			ContentLink rewardCardListPage = new ContentLink();
//			rewardCardListPage.setLinkUrl(UriHelper.getRewardCardListPagePattern());
//			rewardCardListPage.setLinkTitle("我的集點卡列表");
//			result.add(rewardCardListPage);
//			
//			// Add Read Terms Of Business Page
//			ContentLink readTermsOfBusinessPage = new ContentLink();
//			readTermsOfBusinessPage.setLinkUrl(UriHelper.getReadTermsOfBusinessPagePattern());
//			readTermsOfBusinessPage.setLinkTitle("升級LINE帳號");
//			result.add(readTermsOfBusinessPage);
//			
//			// Add Game List
//			List<GameModel> games = contentGameService.getAllContentGame();
//			for(GameModel game : games){
//
//				ContentLink gamePage = new ContentLink();
//				gamePage.setLinkUrl(UriHelper.getScratchPattern(game.getGameId()));
//				gamePage.setLinkTitle("遊戲：" + game.getGameName());
//				result.add(gamePage);
//			}
			
//			// Add MGM Pages
//            List<ShareCampaign> shareCampaigns = shareCampaignService.findByStatus(ShareCampaign.STATUS_ACTIVE);
//            for(ShareCampaign o : shareCampaigns) {  
//                ContentLink shareCampaignLinkPage = new ContentLink();
//                shareCampaignLinkPage.setLinkUrl(UriHelper.getMgmPagePattern(o.getCampaignId()));
//                shareCampaignLinkPage.setLinkTitle("分享好友活動: " + o.getCampaignName());
//                result.add(shareCampaignLinkPage);
//            }

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private Map<String, Map<String, Map<String, Long>>> cacheLinkReport = new HashMap<String, Map<String, Map<String, Long>>>();
	/**
	 * 取得連結列表
	 */
	@ControllerLog(description="取得連結列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getLinkUrlReportList")
	@ResponseBody
	public ResponseEntity<?> getLinkUrlReportList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getLinkUrlReportList");

		Calendar yesterdayCalendar = Calendar.getInstance();
		yesterdayCalendar.add(Calendar.DATE, -1);
		
		Calendar nowCalendar = Calendar.getInstance();
		
		Calendar nextCalendar = Calendar.getInstance();
		nextCalendar.add(Calendar.DATE, 1);
		
		try{ 
			String queryFlag = request.getParameter("queryFlag");
			String pageStr = request.getParameter("page");
			
			Map<String, LinkClickReportModel> linkResult = new LinkedHashMap<String, LinkClickReportModel>();

			List<Object[]> result = null; // LINK_URL, LINK_TITLE, LINK_ID, MODIFY_TIME
			if(StringUtils.isNotBlank(pageStr) && StringUtils.isBlank(queryFlag)){
				
				result = new ArrayList<Object[]>();

				int size = 20;
				int page = Integer.parseInt(pageStr);
				
				Sort.Order order = new Sort.Order(Direction.DESC, "modifyTime");
				Sort sort = new Sort(order);
				
				Pageable pageable = new PageRequest(page, size, sort);
				Page<ContentLink> linkList = contentLinkService.findAll(pageable);
				for(ContentLink link : linkList.getContent()){
					if(StringUtils.isBlank(link.getLinkUrl())){
						continue;
					}
					Object[] obj = new Object[4];
					obj[0] = link.getLinkUrl();
					obj[1] = link.getLinkTitle();
					obj[2] = link.getLinkId();
					obj[3] = link.getModifyTime();
					result.add(obj);
				}
			}
			else{
				if(StringUtils.isNotBlank(queryFlag)){
					result = contentLinkService.findAllLinkUrlByLikeFlag("%" + queryFlag + "%");

					List<Object[]> links = contentLinkService.findAllLinkUrlByLikeTitle("%" + queryFlag + "%");
					if(result != null){
						if(links != null && links.size() > 0){
							result.addAll(links);
						}
					}
					else{
						result = links;
					}
				}
				else{
					result = contentLinkService.getAllContentLinkUrl();
				}
			}
			
			for(Object[] link : result){
				String linkUrl = castToString(link[0]);
				String linkTitle = castToString(link[1]);
				String linkId = castToString(link[2]);
				String linkTime = castToString(link[3]);
				LinkClickReportModel model = linkResult.get(linkUrl);
				
				if(model == null){
					model = new LinkClickReportModel();
					model.setLinkUrl(linkUrl);
					model.setLinkId(linkId);
					model.setLinkTitle(linkTitle);
					
					model.setLinkTime(linkTime);
					
					linkResult.put(linkUrl, model);
				}
				else{
					if(StringUtils.isBlank(model.getLinkTitle())){
						model.setLinkTitle(linkTitle);
					}
				}
			}
			
			// Get ContentFlag, setLinkClickCount
			for(LinkClickReportModel model : linkResult.values()){
				List<String> flags = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(model.getLinkId(), ContentFlag.CONTENT_TYPE_LINK);
				model.addFlags(flags);
				Thread.sleep(10);
				// setLinkClickCount
				this.setLinkClickCount(model, nowCalendar, yesterdayCalendar, nextCalendar);
			}

			return new ResponseEntity<>(linkResult, HttpStatus.OK);
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
	 * 取得連結列表
	 */
	@ControllerLog(description="取得連結列表")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/getLinkUrlReportList", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getLinkUrlReportList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody LinkClickReportSearchModel linkClickReportSearchModel) throws IOException {
		String queryFlag = new String(linkClickReportSearchModel.getQueryFlag().getBytes("utf-8"),"utf-8");
		Integer page = linkClickReportSearchModel.getPage();
		logger.info("getLinkUrlReportList, queryFlag=" + queryFlag + " page=" + page);
		Calendar yesterdayCalendar = Calendar.getInstance();
		yesterdayCalendar.add(Calendar.DATE, -1);
		Calendar nowCalendar = Calendar.getInstance();
		Calendar nextCalendar = Calendar.getInstance();
		nextCalendar.add(Calendar.DATE, 1);
		
		try{ 
			Map<String, LinkClickReportModel> linkResult = new LinkedHashMap<String, LinkClickReportModel>();
			List<Object[]> result = null; // LINK_URL, LINK_TITLE, LINK_ID, MODIFY_TIME
			if(page != null && page != 0 && StringUtils.isBlank(queryFlag)){
				result = new ArrayList<Object[]>();
				int size = 20;
				Sort.Order order = new Sort.Order(Direction.DESC, "modifyTime");
				Sort sort = new Sort(order);
				Pageable pageable = new PageRequest(page, size, sort);
				Page<ContentLink> linkList = contentLinkService.findAll(pageable);
				for(ContentLink link : linkList.getContent()){
					if(StringUtils.isBlank(link.getLinkUrl())){
						continue;
					}
					Object[] obj = new Object[4];
					obj[0] = link.getLinkUrl();
					obj[1] = link.getLinkTitle();
					obj[2] = link.getLinkId();
					obj[3] = link.getModifyTime();
					result.add(obj);
				}
			}
			else{
				if(StringUtils.isNotBlank(queryFlag)){
					result = contentLinkService.findAllLinkUrlByLikeFlag("%" + queryFlag + "%");
					List<Object[]> links = contentLinkService.findAllLinkUrlByLikeTitle("%" + queryFlag + "%");
					if(result != null){
						if(links != null && links.size() > 0){
							result.addAll(links);
						}
					}
					else{
						result = links;
					}
				}
				else{
					result = contentLinkService.getAllContentLinkUrl();
				}
			}
			
			for(Object[] link : result){
				String linkUrl = castToString(link[0]);
				String linkTitle = castToString(link[1]);
				String linkId = castToString(link[2]);
				String linkTime = castToString(link[3]);
				LinkClickReportModel model = linkResult.get(linkUrl);
				
				if(model == null){
					model = new LinkClickReportModel();
					model.setLinkUrl(linkUrl);
					model.setLinkId(linkId);
					model.setLinkTitle(linkTitle);
					model.setLinkTime(linkTime);
					linkResult.put(linkUrl, model);
				}
				else{
					if(StringUtils.isBlank(model.getLinkTitle())){
						model.setLinkTitle(linkTitle);
					}
				}
			}
			
			// Get ContentFlag, setLinkClickCount
			for(LinkClickReportModel model : linkResult.values()){
				List<String> flags = contentFlagService.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(model.getLinkId(), ContentFlag.CONTENT_TYPE_LINK);
				model.addFlags(flags);
				Thread.sleep(10);
				// setLinkClickCount
				this.setLinkClickCount(model, nowCalendar, yesterdayCalendar, nextCalendar);
			}

			return new ResponseEntity<>(linkResult, HttpStatus.OK);
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
	
	private String castToString(Object obj){
		if(obj != null){
			return obj.toString();
		}
		return "";
	}
	
	private void setLinkClickCount(LinkClickReportModel model, Calendar nowCalendar, Calendar yesterdayCalendar, Calendar nextCalendar) throws Exception{
		
		String systemStartDate = CoreConfigReader.getString(CONFIG_STR.SYSTEM_START_DATE);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String nowDateKey = model.getLinkUrl() + sdf.format(nowCalendar.getTime());
		logger.info("nowDateKey:" + nowDateKey);
		
		// Get Link Click Count
		Map<String, Map<String, Long>> mapResult = cacheLinkReport.get(nowDateKey);
		
		if(mapResult == null){
			mapResult = contentLinkReportService.getLinkUrlReport(systemStartDate, sdf.format(yesterdayCalendar.getTime()), model.getLinkUrl());
			cacheLinkReport.put(nowDateKey, mapResult);
		}

		// Get From Cache
		AtomicLong totalCount = new AtomicLong(0);
		AtomicLong userCount = new AtomicLong(0);
		if(mapResult != null){
			for(Map<String, Long> dataMap : mapResult.values()){
				totalCount.addAndGet(dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_COUNT.toString()));
				userCount.addAndGet(dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_LINK_DISTINCT_COUNT.toString()));
			}
		}
		
		// Get Click Count Today
		List<Object[]> list = contentLinkService.countClickCountByLinkUrlAndTime(model.getLinkUrl(), sdf.format(nowCalendar.getTime()), sdf.format(nextCalendar.getTime()));

		if(list != null){
			for(Object[] objArray : list){
				totalCount.addAndGet(DBResultUtil.caseCountResult(objArray[0], false).longValue());
				userCount.addAndGet(DBResultUtil.caseCountResult(objArray[1], false).longValue());
			}
		}
		
		model.setTotalCount(totalCount.longValue());
		model.setUserCount(userCount.longValue());
	}
	
	private Map<String, Map<String, Map<String, Long>>> cachePageVisitReport = new HashMap<String, Map<String, Map<String, Long>>>();
	/**
	 * 取得頁面訪問列表
	 */
	@ControllerLog(description="取得頁面訪問列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getPageVisitReportList")
	@ResponseBody
	public ResponseEntity<?> getPageVisitReportList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getPageVisitReportList");

		Calendar yesterdayCalendar = Calendar.getInstance();
		yesterdayCalendar.add(Calendar.DATE, -1);
		
		Calendar nowCalendar = Calendar.getInstance();
		
		Calendar nextCalendar = Calendar.getInstance();
		nextCalendar.add(Calendar.DATE, 1);
		
		try{ 
			Map<String, PageVisitReportModel> pageResult = new LinkedHashMap<String, PageVisitReportModel>();
			
			for(MobilePageEnum page : MobilePageEnum.values()){
				String pageUrl = page.getName();
				String pageTitle = page.getTitle();
				
				PageVisitReportModel model = new PageVisitReportModel();
				model.setPageUrl(pageUrl);
				model.setPageTitle(pageTitle);
				
				pageResult.put(pageUrl, model);
				
				Thread.sleep(10);

				// setLinkClickCount
				this.setPageVisitCount(model, nowCalendar, yesterdayCalendar, nextCalendar);
			}

			return new ResponseEntity<>(pageResult, HttpStatus.OK);
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
	
	private void setPageVisitCount(PageVisitReportModel model, Calendar nowCalendar, Calendar yesterdayCalendar, Calendar nextCalendar) throws Exception{

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		String systemStartDate = CoreConfigReader.getString(CONFIG_STR.SYSTEM_START_DATE);
		
		String nowDateKey = model.getPageUrl() + sdf.format(nowCalendar.getTime());
		logger.info("nowDateKey:" + nowDateKey);
		
		// Get Link Click Count
		Map<String, Map<String, Long>> mapResult = cacheLinkReport.get(nowDateKey);
		
		if(mapResult == null){
			mapResult = pageVisitReportService.getPageVisitReport(systemStartDate, sdf.format(yesterdayCalendar.getTime()), model.getPageUrl());
			cachePageVisitReport.put(nowDateKey, mapResult);
		}

		// Get From Cache
		AtomicLong totalCount = new AtomicLong(0);
		AtomicLong userCount = new AtomicLong(0);
		if(mapResult != null){
			for(Map<String, Long> dataMap : mapResult.values()){
				totalCount.addAndGet(dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_COUNT.toString()));
				userCount.addAndGet(dataMap.get(RECORD_REPORT_TYPE.DATA_TYPE_PAGE_DISTINCT_COUNT.toString()));
			}
		}
		
		// Get Click Count Today
		List<Object[]> list = userTraceLogService.countByReferenceIdAndTime(LOG_TARGET_ACTION_TYPE.TARGET_MobilePage.toString(), 
																								LOG_TARGET_ACTION_TYPE.ACTION_VisitPage.toString(), 
																								model.getPageUrl(), 
																								sdf.format(nowCalendar.getTime()), 
																								sdf.format(nextCalendar.getTime()));

		if(list != null){
			for(Object[] objArray : list){
				totalCount.addAndGet(DBResultUtil.caseCountResult(objArray[0], false).longValue());
				userCount.addAndGet(DBResultUtil.caseCountResult(objArray[1], false).longValue());
			}
		}
		
		model.setTotalCount(totalCount.longValue());
		model.setUserCount(userCount.longValue());
	}
	
	@ControllerLog(description="countLinkUrlList")
	@RequestMapping(method = RequestMethod.GET, value = "/admin/countLinkUrlList")
	@ResponseBody
	public ResponseEntity<?> countLinkUrlList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws Exception {
		logger.info("countLinkUrlList");
		
		try {
			String linkUrl = request.getParameter("linkUrl");
			String startDate = request.getParameter("startDate");
			String endDate = request.getParameter("endDate");
			
			if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
				Map<String, Map<String, Long>> result = contentLinkReportService.getLinkUrlReport(startDate, endDate, linkUrl);
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

	@ControllerLog(description="countLinkIdList")
	@RequestMapping(method = RequestMethod.GET, value = "/admin/countLinkIdList")
	@ResponseBody
	public ResponseEntity<?> countLinkIdList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws Exception {
		logger.info("countLinkIdList");
		
		try {
			String linkId = request.getParameter("linkId");
			String startDate = request.getParameter("startDate");
			String endDate = request.getParameter("endDate");
			
			if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
				Map<String, Map<String, Long>> result = contentLinkReportService.getLinkIdReport(startDate, endDate, linkId);
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

	@ControllerLog(description="countPageVisitList")
	@RequestMapping(method = RequestMethod.GET, value = "/admin/countPageVisitList")
	@ResponseBody
	public ResponseEntity<?> countPageVisitList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws Exception {
		logger.info("countPageVisitList");
		
		try {
			String pageUrl = request.getParameter("pageUrl");
			String startDate = request.getParameter("startDate");
			String endDate = request.getParameter("endDate");
			
			MobilePageEnum.valueOf(pageUrl);
			
			if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
				Map<String, Map<String, Long>> result = pageVisitReportService.getPageVisitReport(startDate, endDate, pageUrl);
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
	
	@ControllerLog(description="exportMidForLinkClickReport")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportMidForLinkClickReport")
	@ResponseBody
	public void exportMidForLinkClickReport(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws Exception {
		logger.info("exportMidForLinkClickReport");
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");

		String linkUrl = request.getParameter("linkUrl");
		
		 if(StringUtils.isNotBlank(linkUrl)){
				logger.info("linkUrl:" + linkUrl);
				
				List<ContentLink> list = contentLinkService.findByLinkUrl(linkUrl);
				
				if(list == null || list.size() == 0){
					throw new Exception("linkUrl Error");
				}
				
				if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date timeStart = sdf.parse(startDate);
					
					Date timeEnd = sdf.parse(endDate);
					Calendar calendarEnd = Calendar.getInstance();
					calendarEnd.setTime(timeEnd);
					calendarEnd.add(Calendar.DATE, 1);
				
					String title = "";
					for(ContentLink link : list){
						if(StringUtils.isBlank(title)){
							title = link.getLinkTitle();
						}
					}
				
					List<String> clickLinkMids = contentLinkService.findClickMidByLinkUrlAndTime(linkUrl, sdf.format(timeStart), sdf.format(calendarEnd.getTime()));
				
					if(clickLinkMids != null){
						
						List<String> titles = new ArrayList<String>();
						titles.add("點擊人UID");
						List<List<String>> data = new ArrayList<List<String>>();
						data.add(clickLinkMids);
		
						String time = sdf.format(timeStart) + "~" + sdf.format(calendarEnd.getTime()) ;
						exportExcelUIService.exportMidResultToExcel(request, response, "ClickUrlMid", "點擊連結:" + title , time, titles, data);
						return;
					}
				}
		 }
		 throw new Exception("資料產生錯誤");
	}
}
