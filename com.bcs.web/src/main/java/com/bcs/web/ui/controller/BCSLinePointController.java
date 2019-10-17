package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.service.ContentRichMsgService;
import com.bcs.core.db.service.ContentTemplateMsgService;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.db.service.MsgMainService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.SendMsgDetailModel;
import com.bcs.web.ui.model.SendMsgModel;
import com.bcs.web.ui.service.LinePointUIService;
import com.bcs.web.ui.service.LoadFileUIService;
import com.bcs.web.ui.service.SendGroupUIService;
import com.bcs.web.ui.service.SendMsgUIService;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.entity.LinePointScheduledDetail;
import com.bcs.core.linepoint.db.service.ExportToExcelForLinePointPushApiEffects;
import com.bcs.core.linepoint.db.service.LinePointDetailService;
import com.bcs.core.linepoint.utils.service.ExcelUtilService;

@Controller
@RequestMapping("/bcs")
public class BCSLinePointController extends BCSBaseController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSLinePointController.class);
	@Autowired
	private LinePointUIService linePointUIService;
	@Autowired
	private LinePointPushAkkaService linePointPushAkkaService;
	@Autowired
	private ExportToExcelForLinePointPushApiEffects exportToExcelForLinePointPushApiEffects;
	@Autowired
	private ExcelUtilService excelUtilService;
	@Autowired
	private SendGroupUIService sendGroupUIService;
	@Autowired
	private SendMsgUIService sendMsgUIService;
	@Autowired
	private LinePointDetailService linePointDetailService;
	@Autowired
	private OracleService oracleService;
	@Autowired
	private LineUserService lineUserService;

	
	@ControllerLog(description = "建立 Line Point 活動")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/linePointCreatePage")
	public String linePointCreatePage(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
		logger.info("linePointCreatePage");
		return BcsPageEnum.LinePointCreatePage.toString();
	}

	@ControllerLog(description = "Line Point 活動列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/linePointListPage")
	public String linePointListPage(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
		logger.info("linePointListPage");
		return BcsPageEnum.LinePointListPage.toString();
	}
	
	// ---- Data Creation ----
	
	@ControllerLog(description = "createLinePointMain")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createLinePointMain", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createLinePointMain(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @RequestBody LinePointMain linePointMain) throws IOException {
		try {
			// Null Exception
			logger.info("[createLinePointMain]");
			if (linePointMain == null) {
				throw new BcsNoticeException("LinePointMain is Null");
			}
			
			// get Oracle Account Information
			String empId = customUser.getAccount().toUpperCase();
			logger.info("empId:" + empId);
			if(StringUtils.isBlank(empId)) {
				throw new BcsNoticeException("empId is Null");
			}
			
			TaishinEmployee taishinEmployee = null;
			try {
				taishinEmployee = oracleService.findByEmployeeId(empId);		
			}catch(Exception e){
				throw new BcsNoticeException("The Employee Id Is Not Correct!"); 
			}
			if(taishinEmployee == null || StringUtils.isBlank(taishinEmployee.getDivisionName())){
				throw new BcsNoticeException("The Employee Id Is Not Correct!");
			}
			
			// get Department Full Name
			String departmentFullName = taishinEmployee.getDivisionName() + " " + 
				taishinEmployee.getDepartmentName() + " " + taishinEmployee.getGroupName();
			logger.info("departmentFullName:" + departmentFullName);
			
			
			//String departmentFullName = "XTREME LINEBC TAISHIN";
			linePointMain.setDepartmentFullName(departmentFullName);
			linePointMain.setModifyUser(customUser.getAccount());
			linePointMain.setModifyTime(new Date());
			LinePointMain result = linePointUIService.saveLinePointMain(linePointMain);
			logger.info("linePointMain : " + result);
			return new ResponseEntity<>(result, HttpStatus.OK);
				
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ControllerLog(description = "Save Line Point Detail List")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createLinePointDetailList", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @RequestBody List<LinePointDetail> linePointDetail) throws IOException {
		logger.info("[createLinePointDetailList]");
		try {
			if (linePointDetail == null) {
				throw new Exception("linePointDetail is Null");
			}
			
			Long linePointMainId =  linePointDetail.get(0).getLinePointMainId();
			logger.info("delete linePointDetail from linePointMainId :" + linePointMainId);
			linePointDetailService.deleteFromLinePointMainId(linePointMainId);
			logger.info("linePointDetail : " + linePointDetail);
			List<LinePointDetail> result = linePointUIService.saveLinePointDetailListFromUI(linePointDetail, customUser.getAccount());
			return new ResponseEntity<>(result, HttpStatus.OK);				
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// ---- Data Search ----
	
	@ControllerLog(description = "findOneMainByMainId")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/findOneLinePointMainByMainId")
	@ResponseBody
	public ResponseEntity<?> findOneLinePointMainByMainId(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
		try {
			logger.info("[findOneLinePointMainByMainId]");
			if (linePointMainId != null) {
				LinePointMain result = linePointUIService.linePointMainFindOne(linePointMainId);
				return new ResponseEntity<>(result, HttpStatus.OK);
			}else 
				throw new Exception("LinePointMain is Null");
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ControllerLog(description = "findAllLinePointDetailByMainId")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/findAllLinePointDetailByMainId")
	@ResponseBody
	public ResponseEntity<?> findAllLinePointDetailByMainId(HttpServletRequest request, HttpServletResponse response, 
			@CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
		try{
			try {
				logger.info("[findAllLinePointDetailByMainId] linePointMainId:"+linePointMainId);
				// get Details
				List<LinePointDetail> linePointDetails = linePointUIService.findByLinePointMainId(linePointMainId);
				logger.info("linePointDetails:"+linePointDetails);
				return new ResponseEntity<>(linePointDetails, HttpStatus.OK);
			}catch(Exception e) {
				throw new BcsNoticeException(e.getMessage());
			}
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@ControllerLog(description = "findAllBcsLinePointMain")
	@RequestMapping(method = {RequestMethod.GET}, value = {"/edit/findAllBcsLinePointMain"})
	@ResponseBody
	public ResponseEntity<?> findAllBcsLinePointMain(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, 
			@RequestParam(value = "startDate", required = false) String startDateStr, 
			@RequestParam(value = "endDate", required = false) String endDateStr) throws IOException {
	    try {
	    	logger.info("[findAllBcsLinePointMain]");

	    	// null translation
		    if (StringUtils.isBlank(startDateStr) || startDateStr.equals("null")) startDateStr = "1911-01-01"; 
		    if (StringUtils.isBlank(endDateStr) || endDateStr.equals("null")) endDateStr = "3099-01-01"; 
		    
	    	// parse date data
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    Date startDate = null, endDate = null;
	    	startDate = sdf.parse(startDateStr);
	    	endDate = sdf.parse(endDateStr);
	    	endDate = DateUtils.addDays(endDate, 1);
	    	logger.info("startDate:" + startDate);
		    logger.info("endDate:" + endDate);
		    
		    //List<LinePointMain> result = new ArrayList<LinePointMain>();
		    List<LinePointMain> list = this.linePointUIService.linePointMainFindBcsAndDate(startDate, endDate);
		    //logger.info("list:" + list);
		    List<LinePointMain> result = competence(list , customUser);
		    
//		    result.addAll(list);
		    logger.info("result:" + ObjectUtil.objectToJsonStr(result));
			return new ResponseEntity(result, HttpStatus.OK);
	    } catch (Exception e) {
	    	logger.error(ErrorRecord.recordError(e));
	    	return new ResponseEntity(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
	    }
	}
	
	@ControllerLog(description = "findAllLinePointMain")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/findAllLinePointMain")
	@ResponseBody
	public ResponseEntity<?> getAllLinePointMainList(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) throws IOException {
		logger.info("[findAllLinePointMain]");
		List<LinePointMain> result = new ArrayList<LinePointMain>();
		List<LinePointMain> list = linePointUIService.linePointMainFindAll();		
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	

	// ---- Front End Data Upload ----
	
	@ControllerLog(description="Check Active UIds")
    @RequestMapping(method = RequestMethod.POST, value = "/edit/checkActiveUids", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> checkActiveUids(HttpServletRequest request, HttpServletResponse response, 
    		@CurrentUser CustomUser customUser, @RequestBody List<String> uids) throws IOException {
        try { 
    		logger.info("[checkActiveUids]");
    		List<Integer> removeIndexs = new ArrayList();
    		for(int i = 0; i < uids.size(); i++) {
    			Boolean isUnactive = lineUserService.checkMIDAllActive(uids.get(i));
    			logger.info("i="+i+", uid="+uids.get(i)+", isUnactive="+isUnactive);
    			if(!isUnactive) {
    				removeIndexs.add(i);
    			}
    		}
    		logger.info("removeIndexs:" + ObjectUtil.objectToJsonStr(removeIndexs));
    		return new ResponseEntity<>(removeIndexs, HttpStatus.OK);
        } catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
        }
    }
	
	@ControllerLog(description="CSV to EXCEL")
    @RequestMapping(method = RequestMethod.POST, value = "/edit/csvToExcel")
    @ResponseBody
    public ResponseEntity<?> csvToExcel(HttpServletRequest request, HttpServletResponse response, 
    		@CurrentUser CustomUser customUser, @RequestPart MultipartFile filePart) throws IOException {
        try {
        	logger.info("---------csvToExcel----------");
    		// file path
            String filePath = CoreConfigReader.getString("file.path");
            
            // file name
    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
    		Date date = new Date();
            String fileName = "LinePointSendGroupCsvToXlsx_" + sdf.format(date) + ".xlsx";
            
        	// convert to excel file
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            InputStream isXlsx = excelUtilService.csvToXlsx(filePart.getInputStream(), filePath, fileName);
            
            // uploadMidSendGroup
			if(isXlsx != null){
				String modifyUser = customUser.getAccount();
				logger.info("modifyUser:" + modifyUser);
				
				Map<String, Object> result = sendGroupUIService.uploadMidSendGroup(isXlsx, modifyUser, new Date(), fileName);
				logger.info("result :" + result);
				return new ResponseEntity<>(result, HttpStatus.OK);
			}else{
				throw new Exception("Upload isXlsx Null");
			}            
        } catch (Exception e) {
        	logger.info("uploadMidSendGroup Exception : " +  e.getMessage().toString());
			if (e.getMessage().contains("RetrySaveUserEventSet"))
			{
				Map<String, Object> result = sendGroupUIService.RetrySaveUserEventSet();
				logger.info("uploadMidSendGroupResult1:" + result);
				
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			else if (e.getMessage().contains("TimeOut")) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
			}
			
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
        }
    }
	
	// ---- Front End Action ----
	
	@ControllerLog(description = "pressSendLinePointMain")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/pressSendLinePointMain")
	@ResponseBody
	public ResponseEntity<?> pressSendLinePointMain(HttpServletRequest request, HttpServletResponse response, 
			@CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
		try{
			// get linePointMain
			logger.info("[pressSendLinePointMain] linePointMainId:"+linePointMainId);
			LinePointMain linePointMain = linePointUIService.linePointMainFindOne(linePointMainId);
			if(linePointMain.getSendStartTime() != null) {
				throw new BcsNoticeException("此專案已發送");
			}
			
			if("ROLE_ADMIN".equals(customUser.getRole()) || "ROLE_LINE_VERIFY".equals(customUser.getRole())) {
				if( (!"ROLE_ADMIN".equals(customUser.getRole())) && customUser.getAccount().equals(linePointMain.getModifyUser())) {
					throw new BcsNoticeException("不可發送自己創專案的line Point");
				}
			}else {
				throw new BcsNoticeException("沒有權限可以發送line Point");
			}
			
			
			// switch allowToSend
			linePointMain.setAllowToSend(!linePointMain.getAllowToSend());
			linePointUIService.saveLinePointMain(linePointMain);
			
			// immediate
			if(LinePointMain.SEND_TIMING_TYPE_IMMEDIATE.equals(linePointMain.getSendTimingType())) {
				try {
					// send append message
					Long msgId = linePointMain.getAppendMessageId();
					logger.info("msgId:" + msgId);
					sendMsgUIService.createExecuteSendMsgRunnable(msgId);
					
					// save send start time
					linePointMain.setModifyUser(customUser.getAccount());
					linePointMain.setSendStartTime(new Date());
					linePointMain.setStatus(LinePointMain.STATUS_COMPLETE);					
					linePointMain.setModifyTime(new Date());
					linePointMain.setSendUser(customUser.getAccount());
					linePointUIService.saveLinePointMain(linePointMain);
					
					// get Details
					List<LinePointDetail> linePointDetails = linePointUIService.findByLinePointMainId(linePointMainId);
					logger.info("linePointDetails:"+linePointDetails);
					
					JSONArray detailIds = new JSONArray();
					for(LinePointDetail linePointDetail: linePointDetails) {
						detailIds.put(linePointDetail.getDetailId());
					}
					
					// combine LinePointPushModel
					LinePointPushModel linePointPushModel = new LinePointPushModel();
					linePointPushModel.setEventId(linePointMainId);
					linePointPushModel.setDetailIds(detailIds);
					linePointPushModel.setSource(LinePointMain.SEND_TYPE_BCS);
					linePointPushModel.setSendTimeType(LinePointMain.SEND_TIMING_TYPE_IMMEDIATE);
					linePointPushModel.setTriggerTime(new Date());
					
					linePointPushAkkaService.tell(linePointPushModel);
					
				}catch(Exception e) {
					throw new BcsNoticeException(e.getMessage());
				}
			}
			return new ResponseEntity<>("", HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@ControllerLog(description = "deleteLinePointMain")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/deleteLinePointMain")
	@ResponseBody
	public ResponseEntity<?> deleteLinePointMain(HttpServletRequest request, HttpServletResponse response, 
			@CurrentUser CustomUser customUser, @RequestParam Long linePointMainId) throws IOException {
			logger.info("[deleteLinePointMain]");
			logger.info("linePointMainId : " + linePointMainId);
			
		try{
			LinePointMain linePointMain = linePointUIService.linePointMainFindOne(linePointMainId);
			
			if("ROLE_LINE_SEND".equals(customUser.getRole()) || "ROLE_LINE_VERIFY".equals(customUser.getRole())) {
				if(!customUser.getAccount().equals(linePointMain.getModifyUser())) {
					throw new BcsNoticeException("沒有權限可以刪除此line Point專案");
				}
			}else if(!"ROLE_ADMIN".equals(customUser.getRole())){
				throw new BcsNoticeException("沒有權限可以刪除此line Point專案");
			}
			
			List<LinePointMain> result = linePointUIService.deleteByLinePointMainId(linePointMainId);
			logger.info("delete LinePointMain : " + result );
			return new ResponseEntity<>("", HttpStatus.OK);
			
			}catch(Exception e){
				logger.error(ErrorRecord.recordError(e));
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
	}
	
	 public List<LinePointMain> competence(List<LinePointMain> list , CustomUser customUser) throws Exception{
	    	List<LinePointMain> result = new ArrayList();
	    	
	    	//取得權限
			String role = customUser.getRole();
			String empId = customUser.getAccount();
			// reset service name
			for(LinePointMain main : list) {
				String serviceName = "BCS";
				if(main.getSendType().equals(LinePointMain.SEND_TYPE_API)) {
					List<LinePointDetail> details = linePointDetailService.findByLinePointMainId(main.getId());
					serviceName = details.get(0).getServiceName();
				}
				main.setSendType(serviceName);
				
				if("ROLE_ADMIN".equals(role) || "ROLE_REPORT".equals(role)) {
					result.add(main);
				}else if("ROLE_LINE_SEND".equals(role) || "ROLE_LINE_VERIFY".equals(role)){
					
					TaishinEmployee employee = oracleService.findByEmployeeId(empId);
//					TaishinEmployee employee = new TaishinEmployee();
//					
//					employee.setDivisionName("XTREME");
//					employee.setDepartmentId("LINEBC");
						
					
					
					
					String Department = main.getDepartmentFullName();
					String[] Departmentname = Department.split(" ");
					//Departmentname[0]; 處  DIVISION_NAME
					//Departmentname[1]; 部  DEPARTMENT_NAME
					//Departmentname[2]; 組  GROUP_NAME

					//判斷邏輯  如果登錄者有組 那只能看到同組 顧處部組全都要一樣，沒有組有部 那就是處跟部要一樣才可以，只有處 就是處一樣即可
					if(StringUtils.isNotBlank(employee.getGroupName())) {
						if(Departmentname[0].equals(employee.getDivisionName()) && Departmentname[1].equals(employee.getDepartmentName()) && Departmentname[2].equals(employee.getGroupName())) {
							result.add(main);
						}
					}else if (StringUtils.isNotBlank(employee.getDepartmentId())) {
						if(Departmentname[0].equals(employee.getDivisionName()) && Departmentname[1].equals(employee.getDepartmentName())) {
							result.add(main);
						}
					}else if(StringUtils.isNotBlank(employee.getDivisionName())) {
						if(Departmentname[0].equals(employee.getDivisionName())) {
							result.add(main);
						}
					}	
				}
			}
	    	return result;
	    }
	
//	@ControllerLog(description = "Get Manual Line Point Main")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getManualLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getManualLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser) throws IOException {
//		logger.info("[getManualLinePointMainList]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindManual();
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
	
//	@ControllerLog(description = "Get Auto Line Point Main")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getAutoLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getAutoLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser) throws IOException {
//		logger.info("[getAutoLinePointMainList]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindAuto();
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
	
	//----
//	@ControllerLog(description = "Get All Line Point Main")
////	@RequestMapping(method = RequestMethod.GET, value = "/edit/getAllLinePointMainListSearch/{searchText}")
//	@ResponseBody
//	public ResponseEntity<?> getAllLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
//		logger.info("[findAllLinePointMainList]");
//		try {
//			List<LinePointMain> result = new ArrayList();
//			List<LinePointMain> list = linePointUIService.linePointMainFindAll(searchText);
//			result.addAll(list);
//			logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//			return new ResponseEntity<>(result, HttpStatus.OK);
//		}catch(Exception e) {
//			logger.info("Error1: " + e.getMessage());
//			return new ResponseEntity<>("Error1: " + e.getMessage(), HttpStatus.OK);
//		}
//
//	}

//	@ControllerLog(description = "Get Manual Line Point Main")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getManualLinePointMainListSearch/{searchText}")
//	@ResponseBody
//	public ResponseEntity<?> getManualLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
//		logger.info("[getManualLinePointMainListSearch]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindManual(searchText);
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	
//	@ControllerLog(description = "Get Auto Line Point Main")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getAutoLinePointMainListSearch/{searchText}")
//	@ResponseBody
//	public ResponseEntity<?> getAutoLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
//		logger.info("[getAutoLinePointMainListSearch]");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindAuto(searchText);
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	//----
//	@ControllerLog(description = "Get Undone Manual Line Point Main")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getUndoneManualLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getUndoneManualLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser ) throws IOException {
//		logger.info("getUndoneManualLinePointMainList");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindUndoneManual();
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	
//	@ControllerLog(description = "Get Undone Auto Line Point Main")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getUndoneAutoLinePointMainList")
//	@ResponseBody
//	public ResponseEntity<?> getUndoneAutoLinePointMainList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser ) throws IOException {
//		logger.info("getUndoneAutoLinePointMainList");
//		List<LinePointMain> result = new ArrayList();
//		List<LinePointMain> list = linePointUIService.linePointMainFindUndoneAuto();
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}	
//	@ControllerLog(description = "Get Success Line Point Detail")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getSuccessLinePointDetailList/{linePointMainId}")
//	@ResponseBody
//	public ResponseEntity<?> getSuccessLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String linePointMainId) throws IOException {
//		logger.info("getSuccessLinePointDetailList");
//		Long mainId = Long.parseLong(linePointMainId);
//		
//		List<LinePointDetail> result = new ArrayList();
//		List<LinePointDetail> list = linePointUIService.findSuccess(mainId);
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	@ControllerLog(description = "Get Fail Line Point Detail")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getFailLinePointDetailList/{linePointMainId}")
//	@ResponseBody
//	public ResponseEntity<?> getFailLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable String linePointMainId) throws IOException {
//		logger.info("getFailLinePointDetailList");
//		Long mainId = Long.parseLong(linePointMainId);
//		
//		List<LinePointDetail> result = new ArrayList();
//		List<LinePointDetail> list = linePointUIService.findFail(mainId);
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}	
//	@ControllerLog(description = "Get Success Line Point Detail")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getLinePointScheduledDetailList/{mainId}")
//	@ResponseBody
//	public ResponseEntity<?> getLinePointScheduledDetailList(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable Long mainId) throws IOException {
//		logger.info("getLinePointScheduledDetailList");
//
//		List<LinePointScheduledDetail> result = new ArrayList();
//		List<LinePointScheduledDetail> list = linePointUIService.findScheduledDetailList(mainId);
//		result.addAll(list);
//		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
	
//	@ControllerLog(description="deleteLinePointMain")
//	@RequestMapping(method = RequestMethod.DELETE, value = "/edit/deleteLinePointMain")
//	@ResponseBody
//	public ResponseEntity<?> deleteLinePointMain( HttpServletRequest request,  HttpServletResponse response, @CurrentUser CustomUser customUser,
//			@RequestParam(required=false) String campaignId, @RequestParam(required=false) String listType) throws IOException {
//		logger.info("deleteLinePointMain");
//		try{
//			if(StringUtils.isNotBlank(campaignId)){
//				logger.info("campaignId:" + campaignId);
//				linePointUIService.deleteFromUI(Long.parseLong(campaignId), customUser.getAccount(), listType);
//				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
//			} else
//				throw new Exception("ID IS NULL");
//		} catch(Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			if(e instanceof BcsNoticeException)
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			else
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
}