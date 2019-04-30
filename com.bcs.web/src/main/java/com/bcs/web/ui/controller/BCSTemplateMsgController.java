package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentTemplateMsg;
import com.bcs.core.db.entity.ContentTemplateMsgAction;
import com.bcs.core.db.service.ContentTemplateMsgService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.model.TemplateMsgModel;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.TemplateActionModel;


@Controller
@RequestMapping("/bcs")
public class BCSTemplateMsgController {
	@Autowired
	private ContentTemplateMsgService contentTemplateMsgService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSTemplateMsgController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/templateMsgCreatePage")
	public String templateMsgCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("templateMsgCreatePage");
		return BcsPageEnum.TemplateMsgCreatePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/templateMsgListPage")
	public String templateMsgListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("templateMsgListPage");
		return BcsPageEnum.TemplateMsgListPage.toString();
	}
	
	/**
	 * 新增與更新樣板訊息
	 */
	@ControllerLog(description="新增與更新樣板訊息")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createTemplateMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createTemplateMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody List<TemplateMsgModel> createTemplateMsgModels, 
			@RequestParam String actionType, 
			@RequestParam String templateId) throws IOException {		
		try {
			if (!validateData(createTemplateMsgModels)) {
				throw new BcsNoticeException("必填欄位不可為空！");
			}
			
			String adminUserAccount = customUser.getAccount(); //取得登入者的帳號
			
			//初始化
			//儲存用的List
			List<ContentTemplateMsg> contentTemplateMsgs = new ArrayList<>();
			List<ContentTemplateMsgAction> contentTemplateMsgActions = new ArrayList<>();
			List<ContentLink> contentLinks = new ArrayList<>();
			//先前的IdList
			List<String> templateIds = new ArrayList<>();
			List<Map<String, String>> actionIdAndLinkIds;
			
			ContentTemplateMsg contentTemplateMsg;//儲存格式
			ContentLink contentLink;//儲存格式
			ContentTemplateMsgAction contentTemplateMsgAction;//儲存格式
			String templateParentId = "";
			String linkId = "";
			
			if (actionType.equals("Edit")) { //變更
				templateIds = contentTemplateMsgService.getPreTemplateIds(templateId);
			} else { //新增與複制
				for(int i=0; i<createTemplateMsgModels.size(); i++){
					templateIds.add(checkDuplicateUUID("1"));
				}
			}
			
			//取出每個templateMsg的資料
			TemplateMsgModel createTemplateMsgModel;
			TemplateActionModel templateActionModel;
			Map<String, String> map;
			templateParentId = templateIds.get(0);
			
			for(int i=0; i<createTemplateMsgModels.size(); i++){
				createTemplateMsgModel = createTemplateMsgModels.get(i);
				
				if (i+1 > templateIds.size()) { //新增樣板
					templateIds.add(checkDuplicateUUID("1"));
				}
				
				//tempalte的資料
				contentTemplateMsg = new ContentTemplateMsg();
				
				contentTemplateMsg.setTemplateId(templateIds.get(i));
				if(!createTemplateMsgModel.getAltText().isEmpty()){
					contentTemplateMsg.setAltText(createTemplateMsgModel.getAltText());
				}
				contentTemplateMsg.setTemplateType(createTemplateMsgModel.getTemplateType());
				contentTemplateMsg.setTemplateLevel(createTemplateMsgModel.getTemplateLevel());
				if(createTemplateMsgModel.getTemplateText().isEmpty()){
					contentTemplateMsg.setTemplateText(" ");
				}else{
					contentTemplateMsg.setTemplateText(createTemplateMsgModel.getTemplateText());
				}
				contentTemplateMsg.setModifyUser(adminUserAccount);
				contentTemplateMsg.setModifyTime(new Date());
				contentTemplateMsg.setStatus(ContentTemplateMsg.STATUS_ACTIVE);
				contentTemplateMsg.setTemplateLetter(createTemplateMsgModel.getTemplateLetter());
				if(i>0){
					contentTemplateMsg.setTemplateParentId(templateParentId);
				}
				
				if(!createTemplateMsgModel.getTemplateType().equals(ContentTemplateMsg.TEMPLATE_TYPE_CONFIRM)){
					if(!createTemplateMsgModel.getTemplateImageId().isEmpty()){
						contentTemplateMsg.setTemplateImageId(createTemplateMsgModel.getTemplateImageId());
					}
					if(!createTemplateMsgModel.getTemplateTitle().isEmpty()){
						contentTemplateMsg.setTemplateTitle(createTemplateMsgModel.getTemplateTitle());
					}
				}
				
				contentTemplateMsgs.add(contentTemplateMsg);
				
				//取出template的action
				List<TemplateActionModel> templateActions = createTemplateMsgModel.getTemplateActions();
				if (actionType.equals("Edit")) { //變更
					contentTemplateMsgService.getPreActionIdAndLinkId(templateIds.get(i));//取得原先圖文訊息的DetailId與LinkId
				}
				//新增與複制
				actionIdAndLinkIds = new ArrayList<>();
				for(int k=0; k<templateActions.size(); k++){
					map = new LinkedHashMap<>();
					map.put("actionId", checkDuplicateUUID("2"));
					map.put("linkId", null);
					actionIdAndLinkIds.add(map);
				}
				
				for(int j=0; j<templateActions.size(); j++){				
					templateActionModel = templateActions.get(j);
					
					if(j+1>actionIdAndLinkIds.size()){
						map = new LinkedHashMap<>();
						map.put("actionId", checkDuplicateUUID("2"));
						map.put("linkId", null);
						actionIdAndLinkIds.add(map);
					}
					
					linkId = actionIdAndLinkIds.get(j).get("linkId");
					//action資料
					contentTemplateMsgAction = new ContentTemplateMsgAction();
					contentTemplateMsgAction.setTemplateIdAction(actionIdAndLinkIds.get(j).get("actionId"));
					contentTemplateMsgAction.setTemplateId(templateIds.get(i));
					contentTemplateMsgAction.setActionLetter(templateActionModel.getActionLetter());
					contentTemplateMsgAction.setActionType(templateActionModel.getActionType());
					contentTemplateMsgAction.setActionLabel(templateActionModel.getActionLabel());
					contentTemplateMsgAction.setStatus(ContentTemplateMsgAction.STATUS_ACTIVE);
					
					switch(templateActionModel.getActionType()){
						case ContentTemplateMsgAction.ACTION_TYPE_URI:
							contentLink = new ContentLink();
							if(linkId==null){
								linkId = checkDuplicateUUID("3");
							}
							contentLink.setLinkId(linkId);
							contentLink.setLinkTitle(templateActionModel.getActionLabel());
							contentLink.setLinkUrl(templateActionModel.getActionText());
							contentLink.setModifyTime(new Date());
							contentLink.setModifyUser(adminUserAccount);
							contentLinks.add(contentLink);
							contentTemplateMsgAction.setLinkId(linkId);
							break;
						case ContentTemplateMsgAction.ACTION_TYPE_MESSAGE:
							contentTemplateMsgAction.setActionText(templateActionModel.getActionText());
							break;
						case ContentTemplateMsgAction.ACTION_TYPE_POSTBACK:
							//contentTemplateMsgAction.setActionText(templateActionModel.getActionText());
							contentTemplateMsgAction.setActionData(templateActionModel.getActionData());
							break;
					}
					
					contentTemplateMsgActions.add(contentTemplateMsgAction);
				}
			}
			
			contentTemplateMsgService.createTemplateMsg(contentTemplateMsgs, contentTemplateMsgActions, contentLinks);
			
			return new ResponseEntity<>("save success", HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}	
		}
	}
	
	/**
	 * 取得樣板訊息
	 */
	@ControllerLog(description="取得樣板訊息")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getTemplateMsg/{templateId}")
	@ResponseBody
	public ResponseEntity<?> getTemplateMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String templateId) throws IOException {
		logger.info("getTemplateMsg");

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getContentTemplateMsg(templateId);

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * 取得樣板訊息列表
	 */
	@ControllerLog(description="取得樣板訊息列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getTemplateMsgList")
	@ResponseBody
	public ResponseEntity<?> getRichMsgList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getTemplateMsgList");

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getAllContentTemplateMsg();

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/** 
	 * 檢查必填欄位不可為空
	 */
	public Boolean validateData(List<TemplateMsgModel> createTemplateMsgModels) {
		TemplateMsgModel createTemplateMsgModel;
		TemplateActionModel templateActionModel;
		Boolean imageIsEmpty = createTemplateMsgModels.get(0).getTemplateImageId().isEmpty();
		Boolean titleIsEmpty = createTemplateMsgModels.get(0).getTemplateTitle().isEmpty();
		String actionType;
		
		for(int i=0; i<createTemplateMsgModels.size(); i++){
			createTemplateMsgModel = createTemplateMsgModels.get(i);
			
			if(createTemplateMsgModel.getTemplateImageId().isEmpty() ^ imageIsEmpty){
				return false;
			}
			
			if(createTemplateMsgModel.getTemplateTitle().isEmpty() ^ titleIsEmpty){
				return false;
			}
			
			if(createTemplateMsgModel.getAltText().isEmpty() && i==0){
				return false;
			}
			
			List<TemplateActionModel> templateActions = createTemplateMsgModel.getTemplateActions();
			for(int j=0; j<templateActions.size(); j++){
				templateActionModel = templateActions.get(j);
				actionType = templateActionModel.getActionType();
				
				if(templateActionModel.getActionLabel().isEmpty()){
					return false;
				}
				
				switch(actionType){
					case "uri" :
						if(templateActionModel.getActionText().isEmpty()){
							return false;
						}
						break;
					case "message" :
						if(templateActionModel.getActionText().isEmpty()){
							return false;
						}
						break;
					case "postback" :
						if(templateActionModel.getActionData().isEmpty()){
							return false;
						}
						break;
				}
			}
		}
		return true;
	}
	
	/** 
	 * 回傳一個沒有重覆的uuid
	 */
	public String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean duplicateUUID = contentTemplateMsgService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = contentTemplateMsgService.checkDuplicateUUID(queryType, uuid);
		}
		
		return uuid;
	}
	
	/** 
	 * 刪除樣板訊息
	 */
	@ControllerLog(description="刪除樣板訊息")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteTemplateMsg/{templateId}")
	@ResponseBody
	public ResponseEntity<?> deleteTemplateMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@PathVariable String templateId) {
		logger.info("deleteTemplateMsg");
		
		try {
			// Check Delete Right
			boolean isAdmin = customUser.isAdmin();
			if(isAdmin) {
				contentTemplateMsgService.deleteTemplateMsg(templateId, customUser.getAccount());
				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
			} else {
				throw new BcsNoticeException("此帳號沒有刪除權限");
			}
		} catch(Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}
			else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}	
		}
	}
}

