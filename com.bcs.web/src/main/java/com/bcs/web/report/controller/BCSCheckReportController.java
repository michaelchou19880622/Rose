package com.bcs.web.report.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.api.service.LineTokenApiService;
import com.bcs.core.bot.api.service.LineAccessApiService;
import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.record.service.CatchRecordReceive;
import com.bcs.core.db.entity.ActionUserCoupon;
import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.service.ActionUserCouponService;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.RecordReportService;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.db.service.SystemLogService;
import com.bcs.core.db.service.UserTraceLogService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.enums.RECORD_REPORT_TYPE;
import com.bcs.core.record.service.CatchHandleMsgReceiveTimeout;
import com.bcs.core.record.service.CatchRecordBinded;
import com.bcs.core.record.service.CatchRecordOpAddReceive;
import com.bcs.core.record.service.CatchRecordOpBlockedReceive;
import com.bcs.core.utils.SystemCheckUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.controller.BCSMsgSendController;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Controller
@RequestMapping("/bcs")
public class BCSCheckReportController extends BCSBaseController {

	@Autowired
	private CatchRecordBinded catchRecordBinded;
	@Autowired
	private CatchRecordReceive catchRecordReceive;
	@Autowired
	private CatchRecordOpAddReceive catchRecordOpAddReceive;
	@Autowired
	private CatchRecordOpBlockedReceive catchRecordOpBlockedReceive;
	@Autowired
	private CatchHandleMsgReceiveTimeout catchHandleMsgReceiveTimeout;
	@Autowired
	private SystemLogService systemLogService;
	@Autowired
	private UserTraceLogService userTraceLogService;
	@Autowired
	private LineUserService lineUserService;
	@Autowired
	private MsgBotReceiveService msgBotReceiveService;
	@Autowired
	private RecordReportService recordReportService;
	@Autowired
	private SystemConfigService systemConfigService;
	@Autowired
	private ActionUserCouponService actionUserCouponService;
	@Autowired
	private LineTokenApiService lineTokenApiService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSMsgSendController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/catchRecordReceiveList")
	@ResponseBody
	public ResponseEntity<?> catchRecordReceiveList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("catchRecordReceiveList");

		String zoomStr = request.getParameter("zoom");
		int zoom = 1;
		if(StringUtils.isNotBlank(zoomStr)){
			zoom = Integer.parseInt(zoomStr);
		}
		
		String calendarStr = request.getParameter("calendar");
		int calendarField = parseCalendar(calendarStr);
		
		Map<String, AtomicLong> result = catchRecordReceive.getAll(calendarField, zoom);
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	private int parseCalendar(String calendarStr){
		logger.info("parseCalendar:" + calendarStr);

		int calendarField = Calendar.MINUTE;
		if(StringUtils.isNotBlank(calendarStr)){
			if(Calendar.HOUR_OF_DAY ==  Integer.parseInt(calendarStr)){
				calendarField = Calendar.HOUR_OF_DAY;
			}
			else if(Calendar.DATE ==  Integer.parseInt(calendarStr)){
				calendarField = Calendar.DATE;
			}
		}
		
		return calendarField;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/catchRecordBindedList")
	@ResponseBody
	public ResponseEntity<?> catchRecordBindedList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("catchRecordBindedList");

		String zoomStr = request.getParameter("zoom");
		int zoom = 1;
		if(StringUtils.isNotBlank(zoomStr)){
			zoom = Integer.parseInt(zoomStr);
		}
		
		String calendarStr = request.getParameter("calendar");
		int calendarField = parseCalendar(calendarStr);
		
		Map<String, AtomicLong> result = catchRecordBinded.getAll(calendarField, zoom);
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/catchHandleMsgReceiveTimeoutList")
	@ResponseBody
	public ResponseEntity<?> catchHandleMsgReceiveTimeoutList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("catchHandleMsgReceiveTimeoutList");

		String zoomStr = request.getParameter("zoom");
		int zoom = 1;
		if(StringUtils.isNotBlank(zoomStr)){
			zoom = Integer.parseInt(zoomStr);
		}
		
		String calendarStr = request.getParameter("calendar");
		int calendarField = parseCalendar(calendarStr);
		
		Map<String, AtomicLong> result = catchHandleMsgReceiveTimeout.getAll(calendarField, zoom);
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/catchRecordOpAddReceiveList")
	@ResponseBody
	public ResponseEntity<?> catchRecordOpAddReceiveList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("catchRecordOpAddReceiveList");

		String zoomStr = request.getParameter("zoom");
		int zoom = 1;
		if(StringUtils.isNotBlank(zoomStr)){
			zoom = Integer.parseInt(zoomStr);
		}
		
		String calendarStr = request.getParameter("calendar");
		int calendarField = parseCalendar(calendarStr);
		
		Map<String, AtomicLong> result = catchRecordOpAddReceive.getAll(calendarField, zoom);
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/catchRecordOpBlockedReceiveList")
	@ResponseBody
	public ResponseEntity<?> catchRecordOpBlockedReceiveList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("catchRecordOpBlockedReceiveList");

		String zoomStr = request.getParameter("zoom");
		int zoom = 1;
		if(StringUtils.isNotBlank(zoomStr)){
			zoom = Integer.parseInt(zoomStr);
		}
		
		String calendarStr = request.getParameter("calendar");
		int calendarField = parseCalendar(calendarStr);
		
		Map<String, AtomicLong> result = catchRecordOpBlockedReceive.getAll(calendarField, zoom);
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/systemCheck")
	@ResponseBody
	public ResponseEntity<?> systemCheck(HttpServletRequest request,HttpServletResponse response) {
		logger.info("systemCheck");
		
		ObjectNode result = SystemCheckUtil.Info();
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/admin/systemReportPage")
	public String systemReportPage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("systemReportPage");
		
		return BcsPageEnum.SystemReportPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/countBinded2Block")
	@ResponseBody
	public ResponseEntity<?> countBinded2Block(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("countBinded2Block");

		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date time = sdf.parse(endDate);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(time);
			calendar.add(Calendar.DATE, 1);

			endDate = sdf.format(calendar.getTime());
			logger.info("startDate:" + startDate);
			logger.info("endDate:" + endDate);
			
			Long result = userTraceLogService.countBinded2Block(startDate, endDate);
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		
		Long result = userTraceLogService.countBinded2Block();
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/admin/countReceiveList")
	@ResponseBody
	public ResponseEntity<?> countReceiveList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("countReceiveList");

		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date timeStart = sdf.parse(startDate);
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(timeStart);
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);

			endDate = sdf.format(calendarEnd.getTime());
			logger.info("startDate:" + startDate);
			logger.info("endDate:" + endDate);

			String referenceId = RECORD_REPORT_TYPE.REFERENCE_ID_COUNT_RECEIVE.toString();
			// Query REFERENCE_ID_COUNT_RECEIVE
			Map<String, Map<String, Long>> countReceiveList = recordReportService
					.findRecordReportListByContentType(referenceId,
							referenceId, startDate, endDate);
			
			Map<String, Long> result = new LinkedHashMap<String, Long>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);

					String startTimeStr = sdf.format(calendarStart.getTime());
					Map<String, Long> mapReceive = countReceiveList.get(startTimeStr);
					
					Long count = null;

					if(mapReceive != null){
						count = mapReceive.get(referenceId);
					}
					if (count == null) {
						count = msgBotReceiveService.countReceiveByType(sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()), MsgBotReceive.EVENT_TYPE_MESSAGE);
						count += msgBotReceiveService.countReceiveByType(sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()), MsgBotReceive.EVENT_TYPE_POSTBACK);
						
						recordReportService
								.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
										startTimeStr,
										referenceId,
										referenceId,
										referenceId, 
										count);
					}
					
					result.put(sdf.format(calendarStart.getTime()), count);

					calendarStart.add(Calendar.DATE, 1);
				}
				else{
				
					break;
				}
			}
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		
		String result = "Error";
		
		return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/admin/countReceiveOpList")
	@ResponseBody
	public ResponseEntity<?> countReceiveOpList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("countReceiveOpList");

		String opTypeStr = request.getParameter("opType");
		String opTypeData = RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_ADD_COUNT.toString();
		try{
			if(StringUtils.isNotBlank(opTypeStr)){
				if(MsgBotReceive.EVENT_TYPE_UNFOLLOW.equals(opTypeStr)){
					opTypeData = RECORD_REPORT_TYPE.DATA_TYPE_RECEIVE_OP_BLOCK_COUNT.toString();
				}
			}
			else{
				opTypeStr = MsgBotReceive.EVENT_TYPE_FOLLOW;
			}
		}
		catch(Exception e){}
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate) ){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date timeStart = sdf.parse(startDate);
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(timeStart);
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);

			endDate = sdf.format(calendarEnd.getTime());
			logger.info("startDate:" + startDate);
			logger.info("endDate:" + endDate);
			
			String referenceId = RECORD_REPORT_TYPE.REFERENCE_ID_USER_GROUP.toString();
			
			// Query CONTENT_TYPE_RECEIVE_OP
			Map<String, Long> receiveOpList = recordReportService
					.findRecordReportListByRecordTime(referenceId,
							RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(), opTypeData, startDate, endDate);
			
			Map<String, Long> result = new LinkedHashMap<String, Long>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);
					
					String startTimeStr = sdf.format(calendarStart.getTime());
					String breakTimeStr = sdf.format(calendarBreak.getTime());
					
					Long count = null;
					if(receiveOpList != null){
						count = receiveOpList.get(startTimeStr);
					}
					if(count == null){
						count = msgBotReceiveService.countReceiveByType(startTimeStr, breakTimeStr, opTypeStr);
						recordReportService
							.saveByReferenceIdAndContentTypeAndDataTypeAndRecordTime(
									startTimeStr,
									referenceId,
									RECORD_REPORT_TYPE.CONTENT_TYPE_RECEIVE_OP.toString(),
									opTypeData,
									count);
					}
					
					result.put(startTimeStr, count);

					calendarStart.add(Calendar.DATE, 1);
				}
				else{
				
					break;
				}
			}
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		
		String result = "Error";
		
		return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/countSystemLogList")
	@ResponseBody
	public ResponseEntity<?> countSystemLogList(HttpServletRequest request,HttpServletResponse response)  throws Exception {
		logger.info("countSystemLogList");

		String target = request.getParameter("target");
		String action = request.getParameter("action");
		String level = request.getParameter("level");
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate) ){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date timeStart = sdf.parse(startDate);
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(timeStart);
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);

			endDate = sdf.format(calendarEnd.getTime());
			logger.info("startDate:" + startDate);
			logger.info("endDate:" + endDate);
			
			Map<String, Long> result = new LinkedHashMap<String, Long>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);
					
					Long count = 0L;
					
					if(StringUtils.isNotBlank(level)){
						count = systemLogService.countByLevel(level, sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()));
					}
					else if(StringUtils.isNotBlank(target) && StringUtils.isNotBlank(action)){
						count = systemLogService.countByTargetAndAction(target, action, sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()));
					}
					else{
						count = systemLogService.countAll(sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()));
					}
					
					result.put(sdf.format(calendarStart.getTime()), count);

					calendarStart.add(Calendar.DATE, 1);
				}
				else{
				
					break;
				}
			}
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		
		String result = "Error";
		
		return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/systemLogList")
	@ResponseBody
	public ResponseEntity<?> systemLogList(HttpServletRequest request,HttpServletResponse response) {
		logger.info("systemLogList");

		String sizeStr = request.getParameter("size");
		int size = 200;
		if(StringUtils.isNotBlank(sizeStr)){
			size = Integer.parseInt(sizeStr);
		}
		
		Sort.Order order = new Sort.Order(Direction.DESC, "logId");
		Sort sort = new Sort(order);
		
		Pageable pageable = new PageRequest(0, size, sort);
		
		Page<SystemLog> result = null;

		String target = request.getParameter("target");
		String action = request.getParameter("action");
		String level = request.getParameter("level");
		if(StringUtils.isNotBlank(level)){
			result = systemLogService.findByLevel(level, pageable);
		}
		else if(StringUtils.isNotBlank(target) && StringUtils.isNotBlank(action)){
			result = systemLogService.findByTargetAndAction(target, action, pageable);
		}
		else{
			result = systemLogService.findAll(pageable);	
		}		
		
		return new ResponseEntity<>(result.getContent(), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/actionUserCouponList")
	@ResponseBody
	public ResponseEntity<?> actionUserCouponList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("actionUserCouponList");

		String sizeStr = request.getParameter("size");
		int size = 200;
		if(StringUtils.isNotBlank(sizeStr)){
			size = Integer.parseInt(sizeStr);
		}
		
		Sort.Order order = new Sort.Order(Direction.DESC, "id");
		Sort sort = new Sort(order);
		
		Pageable pageable = new PageRequest(0, size, sort);
		
		List<ActionUserCoupon> result = new ArrayList<ActionUserCoupon>();

		String couponId = request.getParameter("couponId");
		
		if(StringUtils.isNotBlank(couponId)){
			result = actionUserCouponService.findByCouponId(couponId);
		}
		else{
			result = actionUserCouponService.findAll(pageable).getContent();
		}		
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/userTraceLogList")
	@ResponseBody
	public ResponseEntity<?> userTraceLogList(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("userTraceLogList");

		String sizeStr = request.getParameter("size");
		int size = 200;
		if(StringUtils.isNotBlank(sizeStr)){
			size = Integer.parseInt(sizeStr);
		}
		
		Sort.Order order = new Sort.Order(Direction.DESC, "logId");
		Sort sort = new Sort(order);
		
		Pageable pageable = new PageRequest(0, size, sort);
		
		List<UserTraceLog> result = new ArrayList<UserTraceLog>();

		String target = request.getParameter("target");
		String action = request.getParameter("action");
		String level = request.getParameter("level");
		
		String mid = request.getParameter("mid");
		String referenceId = request.getParameter("referenceId");
		
		if(StringUtils.isNotBlank(level)){
			result = userTraceLogService.findByLevel(level, pageable).getContent();
		}
		else if(StringUtils.isNotBlank(target) && StringUtils.isNotBlank(action)){
			result = userTraceLogService.findByTargetAndAction(target, action, pageable).getContent();
		}
		else if(StringUtils.isNotBlank(mid)){
			result = userTraceLogService.findByModifyUser(mid, pageable).getContent();
		}
		else if(StringUtils.isNotBlank(referenceId)){
			result = userTraceLogService.findByReferenceId(referenceId, pageable).getContent();
		}
		else{
			result = userTraceLogService.findAll(pageable).getContent();
		}		
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/countUserTraceLogList")
	@ResponseBody
	public ResponseEntity<?> countUserTraceLogList(HttpServletRequest request,HttpServletResponse response)  throws Exception {
		logger.info("countUserTraceLogList");

		String target = request.getParameter("target");
		String action = request.getParameter("action");
		String level = request.getParameter("level");
		
		String startDate = request.getParameter("startDate");
		String endDate = request.getParameter("endDate");
		
		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate) ){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date timeStart = sdf.parse(startDate);
			Calendar calendarStart = Calendar.getInstance();
			calendarStart.setTime(timeStart);
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);

			endDate = sdf.format(calendarEnd.getTime());
			logger.info("startDate:" + startDate);
			logger.info("endDate:" + endDate);
			
			Map<String, Long> result = new LinkedHashMap<String, Long>();

			Date timeBreak = sdf.parse(startDate);
			Calendar calendarBreak = Calendar.getInstance();
			calendarBreak.setTime(timeBreak);
			while(true){
				if(calendarStart.compareTo(calendarEnd)  < 0){
					calendarBreak.add(Calendar.DATE, 1);
					
					Long count = 0L;
					
					if(StringUtils.isNotBlank(level)){
						count = userTraceLogService.countByLevel(level, sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()));
					}
					else if(StringUtils.isNotBlank(target) && StringUtils.isNotBlank(action)){
						count = userTraceLogService.countByTargetAndAction(target, action, sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()));
					}
					else{
						count = userTraceLogService.countAll(sdf.format(calendarStart.getTime()), sdf.format(calendarBreak.getTime()));
					}
					
					result.put(sdf.format(calendarStart.getTime()), count);

					calendarStart.add(Calendar.DATE, 1);
				}
				else{
				
					break;
				}
			}
			
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		
		String result = "Error";
		
		return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/clearConfigurationData")
	@ResponseBody
	public ResponseEntity<?> clearConfigurationData(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("clearConfigurationData");
		
		systemConfigService.clearData();
		LineAccessApiService.clearData();
		return new ResponseEntity<>("Success", HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getConfigurationData")
	@ResponseBody
	public ResponseEntity<?> getConfigurationData(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("getConfigurationData");
		
		Map<String, String> result = systemConfigService.getSettingData();
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/checkAccessTokenTime")
	@ResponseBody
	public ResponseEntity<?> checkAccessTokenTime(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("checkAccessTokenTime");
		
		ObjectNode callVerifyResult = LineAccessApiService.callVerifyAPIAndIssueToken(CONFIG_STR.Default.toString(), false);
		logger.info("callVerifyResult:" + callVerifyResult);
		
		return new ResponseEntity<>(callVerifyResult, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/callRefreshingAPI")
	@ResponseBody
	public ResponseEntity<?> callRefreshingAPI(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("callRefreshingAPI");
		ObjectNode callRefreshingResult = LineAccessApiService.callVerifyAPIAndIssueToken(CONFIG_STR.Default.toString(), true);
		logger.info("callRefreshingResult:" + callRefreshingResult);
		
		return new ResponseEntity<>(callRefreshingResult, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/callRefreshingAPI_Force")
	@ResponseBody
	public ResponseEntity<?> callRefreshingAPI_Force(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("callRefreshingAPI_Force");
		
		String channelId = request.getParameter("channelId");
		if(StringUtils.isBlank(channelId)){
			channelId = CONFIG_STR.Default.toString();
		}
		
		boolean callRefreshingResult = LineAccessApiService.callRefreshingAPI(channelId);
		logger.info("callRefreshingResult:" + callRefreshingResult);
		
		return new ResponseEntity<>("callRefreshingResult:" + callRefreshingResult, HttpStatus.OK);
	}
}
