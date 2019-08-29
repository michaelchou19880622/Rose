package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentTemplateMsg;
import com.bcs.core.db.entity.ContentTemplateMsgAction;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.entity.MsgSendRecord;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.MsgDetailService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.db.service.MsgSendMainService;
import com.bcs.core.db.service.MsgSendRecordService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.utils.service.LinePointReportExcelService;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailMing;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailEvery8dRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMingRepository;
import com.bcs.core.taishin.circle.PNP.db.repository.PnpDetailMitakeRepository;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.taishin.service.PNPMaintainExcelService;
import com.bcs.core.taishin.service.PnpReportExcelService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.SendMsgDetailModel;
import com.bcs.web.ui.model.SendMsgModel;
import com.bcs.web.ui.model.TemplateActionModel;
import com.bcs.web.ui.model.TemplateMsgModel;
import com.bcs.web.ui.service.ExportExcelUIService;
import com.bcs.web.ui.service.LinePointUIService;
import com.bcs.web.ui.service.LoadFileUIService;
import com.bcs.web.ui.service.PNPMaintainUIService;
import com.bcs.web.ui.service.SendMsgUIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Controller
@RequestMapping("/bcs")
public class BCSLinePointReportController extends BCSBaseController {	
	@Autowired
	private LinePointUIService linePointUIService;
	@Autowired
	private LinePointDetailService linePointDetailService;
	@Autowired
	private LinePointReportExcelService linePointReportExcelService;
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSLinePointReportController.class);
	
	
	@ControllerLog(description = "Line Point 統計報表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/linePointStatisticsReportPage")
	public String linePointStatisticsReportPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("linePointStatisticsReportPage");
		return BcsPageEnum.LinePointStatisticsReportPage.toString();
	}
	
	@ControllerLog(description = "Line Point 統計報表明細")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/linePointStatisticsReportDetailPage")
	public String linePointStatisticsReportDetailPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("linePointStatisticsReportPage");
		return BcsPageEnum.LinePointStatisticsReportDetailPage.toString();
	}
	
	// ---- Statistics Report ----    
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReport")
	@ResponseBody
	public ResponseEntity<?> getLPStatisticsReport(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
			@RequestParam(value = "startDate", required=false) String startDateStr, 
			@RequestParam(value = "endDate", required=false) String endDateStr,
			@RequestParam(value = "modifyUser", required=false) String modifyUser, 
			@RequestParam(value = "title", required=false) String title, 
			@RequestParam(value = "page", required=true) Integer page) throws IOException {
		try{
			logger.info("[getLPStatisticsReport]");
			
			// null translation
			if(StringUtils.isBlank(startDateStr) || startDateStr.equals("null")) startDateStr = "1911-01-01";
			if(StringUtils.isBlank(endDateStr) || endDateStr.equals("null")) endDateStr = "3099-01-01";
			if(StringUtils.isBlank(title) || title.equals("null")) title = "";
			if(StringUtils.isBlank(modifyUser) || modifyUser.equals("null")) modifyUser = "";
			
			// parse date data
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = null, endDate = null;
			startDate = sdf.parse(startDateStr);
			endDate = sdf.parse(endDateStr);
			endDate = DateUtils.addDays(endDate, 1);	
			logger.info("startDate:"+startDate);
			logger.info("endDate:"+endDate);
			logger.info("title:"+title);
			logger.info("modifyUser:"+modifyUser);
			
			// get result list
			List<LinePointMain> result = new ArrayList();
			List<LinePointMain> list = linePointUIService.getLinePointStatisticsReport(startDate, endDate, modifyUser, title, page);
			
			// reset service name
			for(LinePointMain main : list) {
				String serviceName = "BCS";
				if(main.getSendType().equals(LinePointMain.SEND_TYPE_API)) {
					List<LinePointDetail> details = linePointDetailService.findByLinePointMainId(main.getId());
					serviceName = details.get(0).getServiceName();
				}
				main.setSendType(serviceName);
				result.add(main);
			}

			logger.info("result:" + ObjectUtil.objectToJsonStr(result));
			return new ResponseEntity<>(result, HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));	
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReportTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getLPStatisticsReportTotalPages(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
			@RequestParam(value = "startDate", required=false) String startDateStr, 
			@RequestParam(value = "endDate", required=false) String endDateStr,
			@RequestParam(value = "modifyUser", required=false) String modifyUser, 
			@RequestParam(value = "title", required=false) String title) throws IOException {
		try{
			logger.info("[getLPStatisticsReportTotalPages]");
			
			// null translation
			if(StringUtils.isBlank(startDateStr) || startDateStr.equals("null")) startDateStr = "1911-01-01";
			if(StringUtils.isBlank(endDateStr) || endDateStr.equals("null")) endDateStr = "3099-01-01";
			if(StringUtils.isBlank(title) || title.equals("null")) title = "";
			if(StringUtils.isBlank(modifyUser) || modifyUser.equals("null")) modifyUser = "";
			
			// parse date data
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = null, endDate = null;
			startDate = sdf.parse(startDateStr);
			endDate = sdf.parse(endDateStr);
			endDate = DateUtils.addDays(endDate, 1);	
			logger.info("startDate:"+startDate);
			logger.info("endDate:"+endDate);
			logger.info("title:"+title);
			logger.info("modifyUser:"+modifyUser);
			
			// calculate count
			Long count = linePointUIService.getLinePointStatisticsReportTotalPages(startDate, endDate, modifyUser, title);
			if(count % 10L == 0L) {
				count /= 10;
			}else {
				count = count / 10 + 1;
			}
			
			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + count.toString() + "\"}", HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
    @RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReportExcel")
    @ResponseBody
    public void getLPStatisticsReportExcel(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, 
			@RequestParam(value = "startDate", required=false) String startDateStr, 
			@RequestParam(value = "endDate", required=false) String endDateStr,
			@RequestParam(value = "modifyUser", required=false) String modifyUser, 
			@RequestParam(value = "title", required=false) String title) throws IOException {
    	try {
    		logger.info("[exportLPStatisticsReportExcel]");
    		
			// null translation
			if(StringUtils.isBlank(startDateStr) || startDateStr.equals("null")) startDateStr = "1911-01-01";
			if(StringUtils.isBlank(endDateStr) || endDateStr.equals("null")) endDateStr = "3099-01-01";
			if(StringUtils.isBlank(title) || title.equals("null")) title = "";
			if(StringUtils.isBlank(modifyUser) || modifyUser.equals("null")) modifyUser = "";
			
			// parse date data
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date startDate = null, endDate = null;
			startDate = sdf.parse(startDateStr);
			endDate = sdf.parse(endDateStr);
			endDate = DateUtils.addDays(endDate, 1);	
			logger.info("startDate:"+startDate);
			logger.info("endDate:"+endDate);
			logger.info("title:"+title);
			logger.info("modifyUser:"+modifyUser);
			
	        // set file path
			String filePath = CoreConfigReader.getString("file.path");
			File folder = new File(filePath);
	        if(!folder.exists()){
	            folder.mkdirs();
	        }
	        
	        // set file name
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
			Date date = new Date();
	        String fileName = "LinePointStatisticReport_" + sdf2.format(date) + ".xlsx";
	
	        // combine & export excel file
	        String filePathAndName = filePath + System.getProperty("file.separator") + fileName;
	        linePointReportExcelService.exportExcel_LinePointStatisticsReport(filePathAndName, startDate, endDate, modifyUser, title);
	        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    	} catch (Exception e) {
    		e.printStackTrace();
            logger.error(ErrorRecord.recordError(e));
        }
    }
    
	// ---- Statistics Report Detail ----    
    
    @RequestMapping(method = RequestMethod.GET, value = "/edit/getLPStatisticsReportDetailExcel")
    @ResponseBody
    public void getLPStatisticsReportDetailExcel(HttpServletRequest request, HttpServletResponse response, 
    		@CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
    	try {
    		logger.info("[getLPStatisticsReportDetailExcel]");
    		
	        // set file path
			String filePath = CoreConfigReader.getString("file.path");
			File folder = new File(filePath);
	        if(!folder.exists()){
	            folder.mkdirs();
	        }
	        
	        // set file name
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
			Date date = new Date();
	        String fileName = "LinePointStatisticReportDetail_" + sdf2.format(date) + ".xlsx";
	
	        // combine & export excel file
	        String filePathAndName = filePath + System.getProperty("file.separator") + fileName;
	        linePointReportExcelService.exportExcel_LinePointStatisticsReportDetail(filePathAndName, linePointMainId);
	        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    	} catch (Exception e) {
    		e.printStackTrace();
            logger.error(ErrorRecord.recordError(e));
        }
    }  
    
//	//  匯出 Push API 成效報表
//	@ControllerLog(description="匯出Line Point Push API 成效報表")
//    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForLPPushApiEffects")
//    @ResponseBody
//    public void exportToExcelForLPPushApiEffects(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
//      
//		// file path
//        String filePath = CoreConfigReader.getString("file.path");
//        
//        // file name
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
//		Date date = new Date();
//        String fileName = "LPPushApiEffects_" + sdf.format(date) + ".xlsx";
//        
//        try {
//            File folder = new File(filePath);
//            if(!folder.exists()){
//                folder.mkdirs();
//            }
//            exportToExcelForLinePointPushApiEffects.exportExcel(filePath, fileName);
//        } catch (Exception e) {
//            logger.error(ErrorRecord.recordError(e));
//        }
//
//        try {
//			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//	// 匯出 Push API 成效報表
//	@ControllerLog(description="匯出Line Point Push API Detail 成效報表")
//    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForLPPushApiEffectsDetail/{mainId}/{status}")
//    @ResponseBody
//    public void exportToExcelForLPPushApiEffectsDetail(HttpServletRequest request, HttpServletResponse response, 
//    		@CurrentUser CustomUser customUser, @PathVariable Long mainId, @PathVariable String status) {
//      
//		// file path
//        String filePath = CoreConfigReader.getString("file.path");
//        
//        // file name
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
//		Date date = new Date();
//        String fileName = "LPPushApiEffectsDetail_" + sdf.format(date) + ".xlsx";
//        
//        try {
//            File folder = new File(filePath);
//            if(!folder.exists()){
//                folder.mkdirs();
//            }
//            exportToExcelForLinePointPushApiEffects.exportExcel(filePath, fileName, mainId, status);
//        } catch (Exception e) {
//            logger.error(ErrorRecord.recordError(e));
//        }
//
//        try {
//			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
}
