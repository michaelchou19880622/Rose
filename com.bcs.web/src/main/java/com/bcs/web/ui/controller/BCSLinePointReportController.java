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
import com.bcs.web.ui.service.LoadFileUIService;
import com.bcs.web.ui.service.PNPMaintainUIService;
import com.bcs.web.ui.service.SendMsgUIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Controller
@RequestMapping("/bcs")
public class BCSLinePointReportController extends BCSBaseController {	
//	@Autowired
//	private PNPMaintainUIService pnpMaintainUIService;
//	@Autowired
//	private PnpReportExcelService pnpReportExcelService;
//	
//	/** Logger */
//	private static Logger logger = Logger.getLogger(BCSLinePointReportController.class);
//	
//	@RequestMapping(method = RequestMethod.GET, value ="/pnpEmployee/pnpDetailReportPage")
//	public String pnpDetailReportPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("pnpDetailReportPage");
//		return BcsPageEnum.PnpDetailReportPage.toString();
//	}
//	
//	@RequestMapping(method = RequestMethod.GET, value = "/pnpEmployee/getPNPDetailReport")
//	@ResponseBody
//	public ResponseEntity<?> getPNPDetailReport(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
//			@RequestParam(value = "startDate", required=false) String startDate, 
//			@RequestParam(value = "endDate", required=false) String endDate,
//			@RequestParam(value = "account", required=false) String account, 
//			@RequestParam(value = "pccCode", required=false) String pccCode, 
//			@RequestParam(value = "sourceSystem", required=false) String sourceSystem, 
//			@RequestParam(value = "page", required=false) Integer page) throws IOException {
//		if(startDate == null) startDate = "2019-03-01";
//		if(endDate == null) endDate = "2019-07-30";
//		try{
//			String empId =  customUser.getAccount().toUpperCase();
//			Map<String, List<String>> result = pnpMaintainUIService.getPNPDetailReport(startDate, endDate, account, pccCode, sourceSystem, page, empId);
//			return new ResponseEntity<>(result, HttpStatus.OK);
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));	
//			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//	
//	@RequestMapping(method = RequestMethod.GET, value = "/pnpEmployee/getPNPDetailReportTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//	@ResponseBody
//	public ResponseEntity<?> getPNPDetailReportTotalPages(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
//			@RequestParam(value = "startDate", required=false) String startDate, 
//			@RequestParam(value = "endDate", required=false) String endDate,
//			@RequestParam(value = "account", required=false) String account, 
//			@RequestParam(value = "pccCode", required=false) String pccCode, 
//			@RequestParam(value = "sourceSystem", required=false) String sourceSystem) throws IOException {
//	
//		logger.info("getPNPDetailReportTotalPages");
//		if(startDate == null) startDate = "1911-01-01";
//		if(endDate == null) endDate = "3099-01-01";
//		
//		try{
//			String empId =  customUser.getAccount().toUpperCase();
//			String count = pnpMaintainUIService.getPNPDetailReportTotalPages(startDate, endDate, account, pccCode, sourceSystem, empId);
//			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + count + "\"}", HttpStatus.OK);
//		}catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//	
//    @RequestMapping(method = RequestMethod.GET, value = "/pnpEmployee/exportPNPDetailReportExcel")
//    @ResponseBody
//    public void exportPNPDetailReportExcel(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, 
//			@RequestParam(value = "startDate", required=false) String startDate, 
//			@RequestParam(value = "endDate", required=false) String endDate,
//			@RequestParam(value = "account", required=false) String account, 
//			@RequestParam(value = "pccCode", required=false) String pccCode, 
//			@RequestParam(value = "sourceSystem", required=false) String sourceSystem) throws IOException {
//      
//		// file path
//        String filePath = CoreConfigReader.getString("file.path");
//        
//        // file name
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
//		Date date = new Date();
//        String fileName = "PNPDetailReport_" + sdf.format(date) + ".xlsx";
//        
//        try {
//            File folder = new File(filePath);
//            if(!folder.exists()){
//                folder.mkdirs();
//            }
//            String empId =  customUser.getAccount().toUpperCase();
//            pnpReportExcelService.exportPNPDetailReportExcel(filePath, fileName, startDate, endDate, account, pccCode, sourceSystem, empId);
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
