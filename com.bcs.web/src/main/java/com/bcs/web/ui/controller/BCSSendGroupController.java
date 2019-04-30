package com.bcs.web.ui.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.repository.GroupGenerateRepository;
import com.bcs.core.db.service.GroupGenerateService;
import com.bcs.core.db.service.SendGroupService;
import com.bcs.core.db.service.UserFieldSetService;
import com.bcs.core.enums.DEFAULT_SEND_GROUP;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.ExportExcelUIService;
import com.bcs.web.ui.service.SendGroupUIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Controller
@RequestMapping("/bcs")
public class BCSSendGroupController extends BCSBaseController {

	@Autowired
	private SendGroupService sendGroupService;
	@Autowired
	private SendGroupUIService sendGroupUIService;
	@Autowired
	private UserFieldSetService userFieldSetService;
	@Autowired
	private GroupGenerateService groupGenerateService;
	@Autowired
	private ExportExcelUIService exportExcelUIService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSSendGroupController.class);

	/**
	 * 建立發送群組頁面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/market/sendGroupCreatePage")
	public String sendGroupCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("sendGroupCreatePage");
		return BcsPageEnum.SendGroupCreatePage.toString();
	}

	/**
	 * 發送群組列表頁面
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/market/sendGroupListPage")
	public String sendGroupListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("sendGroupListPage");
		return BcsPageEnum.SendGroupListPage.toString();
	}

	/**
	 * 查詢發送群組列表
	 * 
	 * @param request
	 * @param response
	 * @return List<SendGroup>
	 * @throws IOException
	 */
	@ControllerLog(description="查詢發送群組列表")
	@RequestMapping(method = RequestMethod.GET, value = "/market/getSendGroupList")
	@ResponseBody
	public ResponseEntity<?> getSendGroupList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getSendGroupList");		
		List<SendGroup> result = sendGroupService.generateDefaultGroup();
		
		List<SendGroup> list = sendGroupService.findAll();
		result.addAll(list);
		
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	/**
	 * Get GroupId Title Map
	 * @param request
	 * @param response
	 * @return Map<Long, String>
	 * @throws IOException
	 */
	@ControllerLog(description="getSendGroupTitleList")
	@RequestMapping(method = RequestMethod.GET, value = "/market/getSendGroupTitleList")
	@ResponseBody
	public ResponseEntity<?> getSendGroupTitleList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getSendGroupTitleList");		
		Map<Long, String> map = sendGroupService.findGroupTitleMap();
		logger.debug("map:" + ObjectUtil.objectToJsonStr(map));
		return new ResponseEntity<>(map, HttpStatus.OK);
	}

	/**
	 * 取得發送群組
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取得發送群組")
	@RequestMapping(method = RequestMethod.GET, value = "/market/getSendGroup")
	@ResponseBody
	public ResponseEntity<?> getSendGroup(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam(required=false) String groupId
			) throws IOException {
		logger.info("getSendGroup");		
		
		try{
			if(StringUtils.isNotBlank(groupId)){
				logger.info("groupId:" + groupId);
				SendGroup sendGroup = sendGroupService.findOne(Long.parseLong(groupId));
				
				if(sendGroup != null){
					return new ResponseEntity<>(sendGroup, HttpStatus.OK);
				}
			}
			
			throw new Exception("Group Id Null");
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
	 * 刪除發送群組
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="刪除發送群組")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteSendGroup")
	@ResponseBody
	public ResponseEntity<?> deleteSendGroup(			
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam(required=false) String groupId) throws IOException {
		logger.info("deleteSendGroup");
	
		try{
			if(StringUtils.isNotBlank(groupId)){
				logger.info("groupId:" + groupId);
				sendGroupUIService.deleteFromUI(Long.parseLong(groupId), customUser.getAccount());
				
				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
			}
			else{
				throw new Exception("Group Id Null");
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
	 * 新增或修改發送群組
	 * 
	 * @param sendGroup
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="新增或修改發送群組")
	@RequestMapping(method = RequestMethod.POST, value = "/market/createSendGroup", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createSendGroup(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody SendGroup sendGroup			
			) throws IOException {
		logger.info("createSendGroup");
		
		try{
			if(sendGroup != null){
				if(StringUtils.isBlank(sendGroup.getGroupTitle())){
					throw new Exception("GroupTitle Null");
				}
				
				if(StringUtils.isBlank(sendGroup.getGroupDescription())){
					throw new Exception("GroupDescription Null");
				}
				
				String adminUserAccount = customUser.getAccount();
				
				SendGroup result = sendGroupUIService.saveFromUI(sendGroup, adminUserAccount);
				
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			else{
				throw new Exception("SendGroup Null");
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
	 * 取得群組條件各個下拉選項值
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取得群組條件各個下拉選項值")
	@RequestMapping(method = RequestMethod.GET, value = "/market/getSendGroupCondition")
	@ResponseBody
	public ResponseEntity<?> getSendGroupCondition(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getSendGroupCondition");
		
		try{
			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode sendGroupCondition = objectMapper.createObjectNode();
			List<Object[]> sendGroupQueryList = userFieldSetService.getFieldKeyAndNameAndType();
			
			for (Object[] sendGroupQuery : sendGroupQueryList) {
				
				if(StringUtils.isBlank((String)sendGroupQuery[0])){
					continue;
				}
				if(StringUtils.isBlank((String)sendGroupQuery[1])){
					continue;
				}
				if(StringUtils.isBlank((String)sendGroupQuery[2])){
					continue;
				}

				ObjectNode sendGroupQueryProperty = (new ObjectMapper()).createObjectNode();
				String queryFieldId = (String) sendGroupQuery[0];
				String queryFieldName = (String) sendGroupQuery[1];
				String queryFieldFormat = (String) sendGroupQuery[2];
				sendGroupQueryProperty
					.putPOJO("queryFieldOp", 
							(ArrayNode) objectMapper.valueToTree(GroupGenerateRepository.validQueryOp));
				sendGroupQueryProperty.put("queryFieldId", queryFieldId);
				sendGroupQueryProperty.put("queryFieldName", queryFieldName);
				sendGroupQueryProperty.put("queryFieldFormat", queryFieldFormat);
				if("Date".equals(queryFieldFormat)){
					sendGroupQueryProperty.put("queryFieldSet", "DatePicker");
				}
				else{
					sendGroupQueryProperty.put("queryFieldSet", "Input");
				}
				sendGroupCondition.putPOJO(queryFieldId, sendGroupQueryProperty);
			}
			
			return new ResponseEntity<>(sendGroupCondition, HttpStatus.OK);
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
	 * 取得條件結果
	 * 
	 * @param groupId
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取得條件結果")
	@RequestMapping(method = RequestMethod.POST, value = "/market/getSendGroupConditionResult", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> getSendGroupConditionResult(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody SendGroup sendGroup,
			@RequestParam(required=false) String startDate,
			@RequestParam(required=false) String endDate) throws IOException {
		logger.info("getSendGroupConditionResult");
		
		try{
			Long groupId = sendGroup.getGroupId() ;
			if(groupId == null){
				BigInteger result = groupGenerateService.findMIDCountBySendGroupDetail(sendGroup.getSendGroupDetail());
				
				logger.info("getSendGroupConditionResult Success");
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			else{
				Long result= 0L;

				if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date time = sdf.parse(endDate);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(time);
					calendar.add(Calendar.DATE, 1);

					endDate = sdf.format(calendar.getTime());
					logger.info("startDate:" + startDate);
					logger.info("endDate:" + endDate);
					
					result= sendGroupService.countDefaultGroupSize(groupId, startDate, endDate);
				}
				else{
					result= sendGroupService.countDefaultGroupSize(groupId);
				}
				if(result != null){
					logger.info("getSendGroupConditionResult Success");
					
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
				else{
					throw new Exception("SendGroup Send Error");
				}
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
	 * 取得條件結果
	 * 
	 * @param groupId
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取得條件結果")
	@RequestMapping(method = RequestMethod.GET, value = "/market/getSendGroupQueryResult")
	@ResponseBody
	public ResponseEntity<?> getSendGroupQueryResult(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestParam Long groupId
			) throws IOException {
		logger.info("getSendGroupQueryResult");
		
		try{

			SendGroup sendGroup = sendGroupService.findOne(groupId);
			if(sendGroup == null){

				throw new Exception("SendGroup Error");
			}

			// 行銷人員設定 群組
			if(groupId > 0){
				try{
					List<String> mids = groupGenerateService.findMIDBySendGroupDetailGroupId(groupId);
					if(mids != null && mids.size() >0){
	
						return new ResponseEntity<>(mids.size(), HttpStatus.OK);
					}
				}
				catch(Exception e){
					logger.error(ErrorRecord.recordError(e));
					throw new Exception("SendGroup Send Error");
				}
			}
			// 預設群祖
			else if(groupId < 0){
				Long result= sendGroupService.countDefaultGroupSize(groupId);
				if(result != null){
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
				else{
					throw new Exception("SendGroup Send Error");
				}
			}
			
			return new ResponseEntity<>(0, HttpStatus.OK);
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

	@ControllerLog(description="uploadMidSendGroup")
	@RequestMapping(method = RequestMethod.POST, value = "/market/uploadMidSendGroup")
	@ResponseBody
	public ResponseEntity<?> uploadMidSendGroup(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestPart MultipartFile filePart
			) throws IOException {
		logger.info("uploadMidSendGroup");

		try{
			if(filePart != null){
				
				String modifyUser = customUser.getAccount();
				logger.info("modifyUser:" + modifyUser);
				
				Map<String, Object> result = sendGroupUIService.uploadMidSendGroup(filePart, modifyUser, new Date());
				
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			else{
				throw new Exception("Upload Mid SendGroup Null");
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
	
	private Map<String, SendGroup> tempSendGroupMap = new HashMap<String, SendGroup>();
	
	@ControllerLog(description="createSendGroupMidExcelTemp")
	@RequestMapping(method = RequestMethod.POST, value = "/market/createSendGroupMidExcelTemp", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createSendGroupMidExcelTemp(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestBody SendGroup sendGroup
) throws IOException {
		logger.info("createSendGroupMidExcelTemp");
		
		try{
			Long groupId = sendGroup.getGroupId() ;
			if(groupId == null){
				BigInteger count = groupGenerateService.findMIDCountBySendGroupDetail(sendGroup.getSendGroupDetail());
				
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("count", count);
				
				String tempId = UUID.randomUUID().toString().toLowerCase();
				tempSendGroupMap.put(tempId, sendGroup);
				result.put("tempId", tempId);
				
				logger.info("createSendGroupMidExcelTemp Success");
				return new ResponseEntity<>(result, HttpStatus.OK);
			}
			else{
				Long count = 0L;
				count= sendGroupService.countDefaultGroupSize(groupId);
				if(count != null){
					Map<String, Object> result = new HashMap<String, Object>();
					result.put("count", count);
					
					logger.info("createSendGroupMidExcelTemp Success");
					
					tempSendGroupMap.put(groupId + "", sendGroup);
					result.put("tempId", groupId + "");
					
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
				else{
					throw new Exception("SendGroup Send Error");
				}
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
	
	@ControllerLog(description="exportToExcelForSendGroup")
	@RequestMapping(method = RequestMethod.GET, value = "/market/exportToExcelForSendGroup")
	@ResponseBody
	public void exportToExcelForSendGroup(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String tempId) throws Exception {
		logger.info("exportToExcelForSendGroup");
		
		SendGroup sendGroup = tempSendGroupMap.get(tempId);

		if(sendGroup == null){

			throw new Exception("SendGroup Error");
		}
		
		Long groupId = sendGroup.getGroupId();

		// 行銷人員設定 群組
		if(groupId == null){
			try{
				List<String> mids = groupGenerateService.findMIDBySendGroupDetail(sendGroup.getSendGroupDetail());
				if(mids != null && mids.size() >0){

					List<String> titles = new ArrayList<String>();
					titles.add("MID");
					List<List<String>> data = new ArrayList<List<String>>();
					data.add(mids);
					
					String title = "SendGroup";
					if(StringUtils.isNotBlank(sendGroup.getGroupTitle())){
						title += ":" + sendGroup.getGroupTitle();
					}
					
					exportExcelUIService.exportMidResultToExcel(request, response, "SendGroup", title , null, titles, data);
				}
			}
			catch(Exception e){
				logger.error(ErrorRecord.recordError(e));
				throw new Exception("SendGroup Send Error");
			}
		}
		// 預設群祖
		else if(groupId < 0){
			List<String> mids = new ArrayList<String>();
			
			int page = 0;
			while(true){
				List<String> list = sendGroupService.queryDefaultGroup(groupId, page);
				if(list != null && list.size() > 0){
					mids.addAll(list);
					logger.debug("queryDefaultGroup:" + list.size());
				}
				else{
					break;
				}
				page++;
			}
			
			if(mids != null && mids.size() >0){

				List<String> titles = new ArrayList<String>();
				titles.add("MID");
				List<List<String>> data = new ArrayList<List<String>>();
				data.add(mids);
				exportExcelUIService.exportMidResultToExcel(request, response, "SendGroup", DEFAULT_SEND_GROUP.getGroupByGroupId(groupId).getTitle(), null, titles, data);
			}
		}
	}
}
