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
import com.bcs.core.taishin.circle.PNP.db.entity.EmployeeRecord;
import com.bcs.core.taishin.circle.PNP.db.entity.PNPMaintainAccountModel;
import com.bcs.core.taishin.circle.db.service.OraclePnpService;
import com.bcs.core.taishin.service.PNPMaintainExcelService;
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
public class BCSPNPMaintainController extends BCSBaseController {	
	@Autowired
	private PNPMaintainUIService pnpMaintainUIService;
	@Autowired	
	private PNPMaintainExcelService pnpMaintainExcelService;
	@Autowired	
	private OraclePnpService oraclePnpService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSPNPMaintainController.class);
	
	@RequestMapping(method = RequestMethod.GET, value ="/edit/pnpNormalAccountListPage")
	public String pnpNormalAccountListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("pnpNormalAccountListPage");
		return BcsPageEnum.PNPNormalAccountListPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value ="/edit/pnpNormalAccountCreatePage")
	public String pnpNormalAccountCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("pnpNormalAccountCreatePage");
		return BcsPageEnum.PNPNormalAccountCreatePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value ="/edit/pnpUnicaAccountListPage")
	public String pnpUnicaAccountListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("pnpUnicaAccountListPage");
		return BcsPageEnum.PNPUnicaAccountListPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value ="/edit/pnpUnicaAccountCreatePage")
	public String pnpUnicaAccountCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("pnpUnicaAccountCreatePage");
		return BcsPageEnum.PNPUnicaAccountCreatePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/findAll/{maxRange}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> findAll(HttpServletRequest request,  HttpServletResponse response, 
			@CurrentUser CustomUser customUser, @PathVariable Integer maxRange) throws IOException {		
		try {
			logger.info("findAll maxRange=" + maxRange);
			oraclePnpService.findAll(maxRange);	
			return new ResponseEntity<>("{}", HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getEmpAccount", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getEmpAccount(HttpServletRequest request,  HttpServletResponse response, 
			@CurrentUser CustomUser customUser) throws IOException {		
		try {
			String empId = customUser.getAccount().toUpperCase();
			logger.info("getEmpAccount empId=" + empId);
			EmployeeRecord result = oraclePnpService.findByEmployeeId(empId);		
			
			if(result == null){
				throw new BcsNoticeException("Not Found Result for this Employee Id!");
			}
			result.setModifyTime(new Date());
			result.setModifyUser(empId);
			oraclePnpService.save(result);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createPNPMaintainAccount", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createPNPMaintainAccount(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, 
			@RequestBody PNPMaintainAccountModel pnpMaintainAccountModel) throws IOException {
		try{
			logger.info("pnpMaintainAccountModel:"+pnpMaintainAccountModel);
			// Check Duplication
			String account = pnpMaintainAccountModel.getAccount();
			String sourceSystem = pnpMaintainAccountModel.getSourceSystem();
			String pnpContent = pnpMaintainAccountModel.getPnpContent();
			List<PNPMaintainAccountModel> sameCheck = pnpMaintainUIService.findByAccountAndSourceSystemAndPnpContent(account, sourceSystem, pnpContent);
			if(sameCheck.size() >= 2) {
				throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！"); 
			}else if(sameCheck.size() == 1) {
				Long addId = pnpMaintainAccountModel.getId();
				logger.info("addId:"+addId);
				if(addId==null) { // Create Mode
					throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！"); 
				}
				
				Long sameId = sameCheck.get(0).getId();
				logger.info("sameId:"+sameId);
				
				if(sameId!=addId) { //  Edit Mode & Not same Id
					throw new BcsNoticeException("帳號、前方來源系統、簡訊內容不可與之前資料重複！"); 
				}
			}
			
			// save
			pnpMaintainAccountModel.setModifyTime(new Date());
			pnpMaintainAccountModel.setModifyUser(customUser.getAccount());
			pnpMaintainUIService.save(pnpMaintainAccountModel);

			return new ResponseEntity<>("save success", HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}	
		}
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/edit/deletePNPMaintainAccount")
	@ResponseBody
	public ResponseEntity<?> deletePNPMaintainAccount(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @RequestParam Long id) throws IOException {
		try{
			if(!customUser.isAdmin()) {
				throw new BcsNoticeException("您無權限刪除");
			}

			PNPMaintainAccountModel pnpMaintainAccountModel = pnpMaintainUIService.findOne(id);			
			if(pnpMaintainAccountModel == null){
				throw new BcsNoticeException("刪除搜查錯誤");
			}
			
			pnpMaintainUIService.delete(pnpMaintainAccountModel);					
			return new ResponseEntity<>("刪除成功", HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException)
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/edit/getPNPMaintainAccount", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getPNPMaintainAccount(
			HttpServletRequest request,  HttpServletResponse response, 
			@CurrentUser CustomUser customUser, @RequestParam Long id) throws IOException {		
		try {
			PNPMaintainAccountModel pnpMaintainAccountModel = pnpMaintainUIService.findOne(id);			
			if(pnpMaintainAccountModel == null){
				throw new BcsNoticeException("刪除搜查錯誤");
			}
			return new ResponseEntity<>(pnpMaintainAccountModel, HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/edit/getPNPMaintainAccountList", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getPNPMaintainAccountList(
			HttpServletRequest request,  HttpServletResponse response,  @CurrentUser CustomUser customUser, 
			@RequestBody PNPMaintainAccountModel pnpMaintainAccountModel, @RequestParam Boolean status) throws IOException {		
		try {
			String divisionName = pnpMaintainAccountModel.getDivisionName();
			String departmentName = pnpMaintainAccountModel.getDepartmentName();
			String groupName = pnpMaintainAccountModel.getGroupName();
			String pccCode = pnpMaintainAccountModel.getPccCode();
			String account = pnpMaintainAccountModel.getAccount();
			String employeeId = pnpMaintainAccountModel.getEmployeeId();
			String accountType = pnpMaintainAccountModel.getAccountType();
			
			List<PNPMaintainAccountModel> list = pnpMaintainUIService.queryUsePageCoditions(
					divisionName, departmentName, groupName, pccCode, account, employeeId, accountType, status);
			return new ResponseEntity<>(list, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}	
		}
	}
	
	// 匯出 EXCEL
	@ControllerLog(description="匯出 EXCEL")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForPNPMaintainAccount")
    @ResponseBody
    public void exportToExcelForBNPushApiEffects(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, 
    		@RequestParam String divisionName, @RequestParam String departmentName, @RequestParam String groupName, @RequestParam String pccCode,
    		@RequestParam String account, @RequestParam String employeeId, @RequestParam String accountType) throws IOException {
		
		// file path
        String filePath = CoreConfigReader.getString("file.path");
        
        // file name
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		Date date = new Date();
        String fileName = "PNPMaintainAccount_" + sdf.format(date) + ".xlsx";
        
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            pnpMaintainExcelService.exportExcel(filePath, fileName, divisionName, departmentName, groupName, pccCode, account, employeeId, accountType);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        try {
			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
//	/**
//	 * 建立訊息 導頁
//	 * 
//	 * @param request
//	 * @param response
//	 * @return MsgCreatePage
//	 */
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/msgCreatePage")
//	public String msgCreatePage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("msgCreatePage");
//		
//		return BcsPageEnum.MsgCreatePage.toString();
//	}
//
//	/**
//	 * 訊息列表 導頁
//	 * 
//	 * @param request
//	 * @param response
//	 * @return MsgListDraftPage
//	 */
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/msgListPage")
//	public String msgListPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("msgListPage");
//		
//		return BcsPageEnum.MsgListDraftPage.toString();
//	}
//
//	/**
//	 * 訊息列表 草稿 導頁
//	 * 
//	 * @param request
//	 * @param response
//	 * @return MsgListDraftPage
//	 */
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/msgListDraftPage")
//	public String msgListDraftPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("msgListDraftPage");
//		
//		return BcsPageEnum.MsgListDraftPage.toString();
//	}
//
//	/**
//	 * 訊息列表 預約 導頁
//	 * 
//	 * @param request
//	 * @param response
//	 * @return MsgListDelayPage
//	 */
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/msgListDelayPage")
//	public String msgListDelayPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("msgListDelayPage");
//		return BcsPageEnum.MsgListDelayPage.toString();
//	}
//
//	/**
//	 * 訊息列表 已傳送 導頁
//	 * 
//	 * @param request
//	 * @param response
//	 * @return MsgListSendedPage
//	 */
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/msgListSendedPage")
//	public String msgListSendedPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("msgListSendedPage");
//		return BcsPageEnum.MsgListSendedPage.toString();
//	}
//
//	/**
//	 * 訊息列表 排程傳送 導頁
//	 * 
//	 * @param request
//	 * @param response
//	 * @return MsgListSchedulePage
//	 */
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/msgListSchedulePage")
//	public String msgListSchedulePage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("msgListSchedulePage");
//		return BcsPageEnum.MsgListSchedulePage.toString();
//	}
//
//	/**
//	 * 取得 訊息資料 by msgId or msgSendId
//	 * 
//	 * @param request
//	 * @param response
//	 * @return Map<String, Object>
//	 * @throws IOException
//	 */
//	@ControllerLog(description="取得 訊息資料 by msgId or msgSendId")
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/getSendMsg")
//	@ResponseBody
//	public ResponseEntity<?> getSendMsg(
//			HttpServletRequest request, 
//			HttpServletResponse response,
//			@CurrentUser CustomUser customUser,
//			@RequestParam(required=false) String msgId,
//			@RequestParam(required=false) String msgSendId) throws IOException {
//		logger.info("getSendMsg");
//
//		try{
//			if(StringUtils.isNotBlank(msgId)){
//				logger.info("msgId:" + msgId);
//				Map<String, Object> result = new LinkedHashMap<String, Object>();
//				
//				Map<MsgMain, List<MsgDetail>> map = msgMainService.queryGetMsgMainDetailByMsgId(Long.parseLong(msgId));
//
//				if(map != null && map.size() == 1){
//					result.put("MsgMain", map);
//					
//					/**
//					 * SendGroup Result
//					 */
//					sendMsgUIService.setGroups(result);
//					
//					/**
//					 * Set Detail Content
//					 */
//					for(List<MsgDetail> details : map.values()){
//						sendMsgUIService.setDetailContent(result, details);
//					}
//					
//					return new ResponseEntity<>(result, HttpStatus.OK);
//				}
//			}
//			else if(StringUtils.isNotBlank(msgSendId)){
//				logger.info("msgSendId:" + msgSendId);
//				Map<String, Object> result = new LinkedHashMap<String, Object>();
//				
//				Map<MsgSendMain, List<MsgDetail>> map = msgSendMainService.queryGetMsgSendMainDetailByMsgId(Long.parseLong(msgSendId));
//				
//				if(map != null && map.size() == 1){
//					result.put("MsgMain", map);
//					
//					/**
//					 * SendGroup Result
//					 */
//					sendMsgUIService.setGroups(result);
//
//					/**
//					 * Set Detail Content
//					 */
//					for(List<MsgDetail> details : map.values()){
//						sendMsgUIService.setDetailContent(result, details);
//					}
//					
//					return new ResponseEntity<>(result, HttpStatus.OK);
//				}
//			}
//			
////			throw new Exception("Status Null");
//			logger.error("Status Null");
//			throw new BcsNoticeException("查詢參數錯誤");
//		}
//		catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//
//			if(e instanceof BcsNoticeException){
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			}
//			else{
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		}
//	}
//
//	/**
//	 * 取得 訊息資料列表
//	 * 
//	 * @param request
//	 * @param response
//	 * @return Map<String, Object>
//	 * @throws IOException
//	 */
//	@ControllerLog(description="取得訊息資料列表")
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/getSendMsgList")
//	@ResponseBody
//	public ResponseEntity<?> getSendMsgList(
//			HttpServletRequest request, 
//			HttpServletResponse response,
//			@CurrentUser CustomUser customUser,
//			@RequestParam String status,
//    		@RequestParam(required=false) String sendType) throws IOException {
//		logger.info("getSendMsgList");
//
//		try{
//			if(StringUtils.isNotBlank(status)){
//				logger.info("status:" + status);
//				Map<String, Object> result = new LinkedHashMap<String, Object>();
//				
//				Map<MsgMain, List<MsgDetail>> map = null;
//
//				if(StringUtils.isNotBlank(sendType)){
//					map = msgMainService.queryGetMsgMainDetailByStatusAndSendType(status, sendType);
//				}
//				else{
//					map = msgMainService.queryGetMsgMainDetailByStatus(status);
//				}
//				
//				if(map != null){
//					result.put("MsgMain", map);
//
//					/**
//					 * SendGroup Result
//					 */
//					sendMsgUIService.setGroups(result);
//
//					/**
//					 * AdminUser Result Map
//					 */
//					try{
//						Map<String, AdminUser> admins = adminUserService.findAllMap();
//						Map<String, String> adminMap = new HashMap<String, String>();
//						for(MsgMain msg : map.keySet()){
//							String userAccount = msg.getModifyUser();
//							if(admins.containsKey(userAccount)){
//								adminMap.put(userAccount, admins.get(userAccount).getUserName());
//							}
//						}
//						result.put("AdminUser", adminMap);
//					}
//					catch(Exception e){
//						logger.error(ErrorRecord.recordError(e));
//					}
//
//					/**
//					 * Set Detail Content
//					 */
//					for(List<MsgDetail> details : map.values()){
//						sendMsgUIService.setDetailContent(result, details);
//					}
//					
//					return new ResponseEntity<>(result, HttpStatus.OK);
//				}
//			}
//			
////			throw new Exception("Status Null");
//			logger.error("Status Null");
//			throw new BcsNoticeException("查詢參數錯誤");
//		}
//		catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//
//			if(e instanceof BcsNoticeException){
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			}
//			else{
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		}
//	}
//
//	/**
//	 * 取得 訊息資料列表 (已傳送)
//	 * 
//	 * @param request
//	 * @param response
//	 * @return Map<String, Object>
//	 * @throws IOException
//	 */
//	@ControllerLog(description="取得訊息資料列表(已傳送)")
//	@RequestMapping(method = RequestMethod.GET, value ="/edit/getSendedMsgList")
//	@ResponseBody
//	public ResponseEntity<?> getSendedMsgList(
//			HttpServletRequest request, 
//			HttpServletResponse response,
//			@CurrentUser CustomUser customUser) throws IOException {
//		logger.info("getSendedMsgList");
//
//		try{
//			Map<String, Object> result = new LinkedHashMap<String, Object>();
//				
//			Map<MsgSendMain, List<MsgDetail>> map = msgSendMainService.queryGetMsgSendMainDetailAll();
//			
//			if(map != null){
//				result.put("MsgMain", map);
//				
//				/**
//				 * SendGroup Result
//				 */
//				try{
//					Map<Long, String> groups = sendGroupService.findGroupTitleMap();
//					result.put("SendGroup", groups);
//				}
//				catch(Exception e){
//					logger.error(ErrorRecord.recordError(e));
//				}
//
//				/**
//				 * AdminUser Result Map
//				 */
//				try{
//					Map<String, AdminUser> admins = adminUserService.findAllMap();
//					Map<String, String> adminMap = new HashMap<String, String>();
//					for(MsgSendMain msg : map.keySet()){
//						String userAccount = msg.getModifyUser();
//						if(admins.containsKey(userAccount)){
//							adminMap.put(userAccount, admins.get(userAccount).getUserName());
//						}
//					}
//					result.put("AdminUser", adminMap);
//				}
//				catch(Exception e){
//					logger.error(ErrorRecord.recordError(e));
//				}
//
//				/**
//				 * Set Detail Content
//				 */
//				for(List<MsgDetail> details : map.values()){
//					sendMsgUIService.setDetailContent(result, details);
//				}
//				
//				return new ResponseEntity<>(result, HttpStatus.OK);
//			}
//			
////			throw new Exception("Data Null");
//			logger.error("Data Null");
//			throw new BcsNoticeException("查詢參數錯誤");
//		}
//		catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//
//			if(e instanceof BcsNoticeException){
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			}
//			else{
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		}
//	}
//
//	/**
//	 * 傳送, 儲存 訊息
//	 * 
//	 * @param sendMsgModel
//	 * @param customUser
//	 * @param request
//	 * @param response
//	 * @return String Result
//	 * @throws IOException
//	 */
//	@ControllerLog(description="傳送,儲存訊息")
//	@RequestMapping(method = RequestMethod.POST, value ="/edit/sendingMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public ResponseEntity<?> sendingMsg(
//			HttpServletRequest request,
//			HttpServletResponse response,
//			@CurrentUser CustomUser customUser,  
//			@RequestBody SendMsgModel sendMsgModel			
//			) throws IOException {
//		logger.info("sendingMsg");
//
//		try{
//			if(sendMsgModel != null){
//				if(StringUtils.isBlank(sendMsgModel.getActionType())){
//					throw new Exception("ActionType Null");
//				}
//				
//				/**
//				 * Send To Me Message
//				 */
//				if(SendMsgModel.ACTION_TYPE.SendToMe.toString().equals(sendMsgModel.getActionType())){
//					
//					return sendToMe(sendMsgModel, customUser);
//				}
//				/**
//				 * Send To Test Group Admin User
//				 */
//				else if(SendMsgModel.ACTION_TYPE.SendToTestGroup.toString().equals(sendMsgModel.getActionType())){
//					
//					return sendToTestGroup(sendMsgModel, customUser);
//				}
//				/**
//				 * Save Draft Message
//				 */
//				else if(SendMsgModel.ACTION_TYPE.SaveDraft.toString().equals(sendMsgModel.getActionType())){
//
//					return saveToDraft(sendMsgModel, customUser);
//				}
//				/**
//				 * Send Message or Save Draft
//				 */
//				else if(SendMsgModel.ACTION_TYPE.SendMsg.toString().equals(sendMsgModel.getActionType())){
//
//					if(customUser.isAdmin()){
//						return sendMsg(sendMsgModel, customUser);
//					}
//					else{
//						throw new BcsNoticeException("權限錯誤");
//					}
//				}
//				else{
//					throw new Exception("Validate ActionType Error");
//				}
//			}
//			else{
//				throw new Exception("SendMsgModel Null");
//			}
//		}
//		catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//
//			if(e instanceof BcsNoticeException){
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			}
//			else{
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		}
//	}
//
//	/**
//	 * 傳送, 儲存 訊息
//	 * 
//	 * @param sendMsgModel
//	 * @param customUser
//	 * @param request
//	 * @param response
//	 * @return String Result
//	 * @throws IOException
//	 */
//	@RequestMapping(method = RequestMethod.POST, value ="/edit/redeisgnSendMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public ResponseEntity<?> redeisgnSendMsg(
//			HttpServletRequest request,
//			HttpServletResponse response,
//			@CurrentUser CustomUser customUser,  
//			@RequestBody SendMsgModel sendMsgModel			
//			) throws IOException {
//		logger.info("redeisgnSendMsg");
//
//		try{
//			if(sendMsgModel != null){
//				if(StringUtils.isBlank(sendMsgModel.getActionType())){
//					throw new Exception("ActionType Null");
//				}
//				
//				/**
//				 * Redesign Msg
//				 */
//				if(SendMsgModel.ACTION_TYPE.RedesignMsg.toString().equals(sendMsgModel.getActionType())){
//					
//					return redesignMsg(sendMsgModel, customUser);
//				}
//				else{
//					throw new Exception("Validate ActionType Error");
//				}
//			}
//			else{
////				throw new Exception("SendMsgModel Null");
//				logger.error("SendMsgModel Null");
//				throw new BcsNoticeException("請選擇正確的訊息");
//			}
//		}
//		catch(Exception e){
//			logger.error(ErrorRecord.recordError(e));
//
//			if(e instanceof BcsNoticeException){
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			}
//			else{
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		}
//	}
//
//	/**
//	 * 刪除訊息
//	 * 
//	 * @param customUser
//	 * @param request
//	 * @param response
//	 * @return String
//	 * @throws IOException
//	 */
//	@ControllerLog(description="刪除訊息")
//	@RequestMapping(method = RequestMethod.DELETE, value ="/admin/deleteSendMsg")
//	@ResponseBody
//	public ResponseEntity<?> deleteSendMsg(
//			HttpServletRequest request, 
//			HttpServletResponse response,
//			@CurrentUser CustomUser customUser,
//			@RequestParam(required=false) String msgId,
//			@RequestParam(required=false) String msgSendId
//			) throws IOException {
//		logger.info("deleteSendMsg");
//		
//		// Check Delete Right
//		boolean isAdmin = customUser.isAdmin();
//		if(isAdmin){
//			
//			try{
//				if(StringUtils.isNotBlank(msgId)){
//					logger.info("msgId:" + msgId);
//					sendMsgUIService.deleteMessageMain(Long.parseLong(msgId));
//					
//					return new ResponseEntity<>("Delete Success", HttpStatus.OK);
//				}
//				else if(StringUtils.isNotBlank(msgSendId)){
//					logger.info("msgSendId:" + msgSendId);
//					sendMsgUIService.deleteMessageSendMain(Long.parseLong(msgSendId));
//					
//					return new ResponseEntity<>("Delete Success", HttpStatus.OK);
//				}
//				else{
////					throw new Exception("msgId msgSendId Null");
//					logger.error("msgId msgSendId Null");
//					throw new BcsNoticeException("請選擇正確的訊息");
//				}
//			}
//			catch(Exception e){
//				logger.error(ErrorRecord.recordError(e));
//				
//				if(e instanceof BcsNoticeException){
//					return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//				}
//				else{
//					return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//				}
//			}
//		}
//		else{
//
//			return new ResponseEntity<>("User No Delete Right", HttpStatus.OK);
//		}
//	}
//	
//	/**
//	 * 傳送給我
//	 * 
//	 * @param sendMsgModel
//	 * @param customUser
//	 * @return String
//	 * @throws Exception
//	 */
//	private ResponseEntity<?> sendToMe(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception{
//
//		String account = customUser.getAccount();
//		String mid = customUser.getMid();
//		if(StringUtils.isBlank(mid)){
////			throw new Exception("You Not Setting LINE MID");
//			logger.error("You Not Setting LINE MID");
//			throw new BcsNoticeException("請設定綁定LINE帳號");
//		}
//
//		// 設定 Test Message Notice Start
//		 List<SendMsgDetailModel> details = sendMsgModel.getSendMsgDetails();
//		 details.add(0, generateTestMsgNotice());
//		
//		sendMsgUIService.sendMsgToMid(mid, sendMsgModel.getSendMsgDetails(), account, sendMsgModel);
//		
//		String result = "Sending Message To Me Success";
//		
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	
//	/**
//	 * 傳送給測試群組
//	 * 
//	 * @param sendMsgModel
//	 * @return String
//	 * @throws Exception
//	 */
//	private ResponseEntity<?> sendToTestGroup(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception{
//
//		String account = customUser.getAccount();
//		
//		List<AdminUser> list = adminUserService.findByMidNotNull();
//		List<String> mids = new ArrayList<String>();
//		if(list != null && list.size() > 0){
//			for(AdminUser adminUser : list){
//				if(StringUtils.isNotBlank(adminUser.getMid())){
//					mids.add(adminUser.getMid());
//				}
//			}
//		}
//		else{
////			throw new Exception("No Test Group");
//			logger.error("No Test Group");
//			throw new BcsNoticeException("測試人員都沒有綁定LINE帳號");
//		}
//		
//		// 設定 Test Message Notice Start
//		 List<SendMsgDetailModel> details = sendMsgModel.getSendMsgDetails();
//		 details.add(0, generateTestMsgNotice());
//
//		sendMsgUIService.sendMsgToMids(mids, sendMsgModel.getSendMsgDetails(), account, sendMsgModel);
//		
//		String result = "Sending Message To Test Group Success";
//		
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	
//	/**
//	 * 儲存草稿
//	 * 
//	 * @param sendMsgModel
//	 * @param customUser
//	 * @return String
//	 * @throws Exception
//	 */
//	private ResponseEntity<?> saveToDraft(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception{
//		String adminUserAccount = customUser.getAccount();
//		
//		sendMsgUIService.saveDraftMessage(sendMsgModel, adminUserAccount);
//		
//		String result = "Save Message Draft Success";
//		
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	
//	private SendMsgDetailModel generateTestMsgNotice(){
//
//		 SendMsgDetailModel detail = new SendMsgDetailModel();
//		 detail.setDetailType(MsgGenerator.MSG_TYPE_TEXT);
//
//		ObjectNode content = (new ObjectMapper()).createObjectNode();
//		content.put("Text", "***此為測試訊息***");
//		 
//		 detail.setDetailContent(ObjectUtil.objectToJsonStr(content));
//		 
//		 return detail;
//	}
//	
//	/**
//	 * 傳送訊息
//	 * 
//	 * @param sendMsgModel
//	 * @param customUser
//	 * @return String
//	 * @throws Exception
//	 */
//	private ResponseEntity<?> sendMsg(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception{
//		String adminUserAccount = customUser.getAccount();
//		
//		sendMsgUIService.sendMessage(sendMsgModel, adminUserAccount);
//		
//		String result = "Send Message Success";
//		
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	
//	/**
//	 * 重設訊息
//	 * 
//	 * @param sendMsgModel
//	 * @param customUser
//	 * @return String
//	 * @throws Exception
//	 */
//	private ResponseEntity<?> redesignMsg(SendMsgModel sendMsgModel, CustomUser customUser) throws Exception{
//		String adminUserAccount = customUser.getAccount();
//		
//		sendMsgUIService.redesignMsg(sendMsgModel, adminUserAccount);
//		
//		String result = "Redesign Message Success";
//		
//		return new ResponseEntity<>(result, HttpStatus.OK);
//	}
//	
//	@ControllerLog(description="exportToExcelForSendedMsg")
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForSendedMsg")
//	@ResponseBody
//	public void exportToExcelForSendedMsg(
//			HttpServletRequest request,
//			HttpServletResponse response,
//			@CurrentUser CustomUser customUser,
//			@RequestParam(required=false) String msgSendId) throws Exception {
//		logger.info("exportToExcelForSendedMsg");
//		
//		 if(StringUtils.isNotBlank(msgSendId)){
//				logger.info("msgSendId:" + msgSendId);
//				List<MsgDetail> details = msgDetailService.findByMsgIdAndMsgParentType(Long.parseLong(msgSendId), MsgSendMain.THIS_PARENT_TYPE);
//				
//				String title = "";
//				for(MsgDetail detail : details){
//					if(StringUtils.isNotBlank(detail.getText())){
//						title += detail.getText();
//					}
//				}
//
//				List<MsgSendRecord> records = msgSendRecordService.findByMsgSendId(Long.parseLong(msgSendId));
//				
//				if(records != null && records.size() > 0){
//					
//					Set<String> mids = new HashSet<String>();
//					for(MsgSendRecord record : records){
//						String sendRecord = record.getSendRecord();
//						if(StringUtils.isNotBlank(sendRecord) && sendRecord.equals("\"200-\"")){
//							mids.add(record.getMid());
//						}
//					}
//					List<String> midsList = new ArrayList<String>();
//					midsList.addAll(mids);
//					
//					List<String> titles = new ArrayList<String>();
//					titles.add("收訊人UID");
//					List<List<String>> data = new ArrayList<List<String>>();
//					data.add(midsList);
//	
//					exportExcelUIService.exportMidResultToExcel(request, response, "SendedMsg", "發送訊息:" + title , null, titles, data);
//					return;
//				}
//		 }
//		 throw new Exception("資料產生錯誤");
//	}
}
