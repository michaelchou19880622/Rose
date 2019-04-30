package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.api.msg.model.RichMsgAction;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentRichMsg;
import com.bcs.core.db.entity.ContentRichMsgDetail;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.ContentFlagService;
import com.bcs.core.db.service.ContentResourceService;
import com.bcs.core.db.service.ContentRichMsgService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.RichMsgModel;
import com.bcs.web.ui.service.SendMsgUIService;


@Controller
@RequestMapping("/bcs")
public class BCSRichMsgController extends BCSBaseController {
	@Autowired
	private ContentRichMsgService contentRichMsgService;
	@Autowired
	private ContentFlagService contentFlagService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSRichMsgController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/richMsgListPage")
	public String richMsgListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("richMsgListPage");
			return BcsPageEnum.RichMsgListPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/richMsgCreatePage")
	public String richMsgCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("richMsgCreatePage");
			return BcsPageEnum.RichMsgCreatePage.toString();
	}
	
	/**
	 * 取得圖文訊息
	 */
	@ControllerLog(description="取得圖文訊息")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getRichMsg/{richId}")
	@ResponseBody
	public ResponseEntity<?> getRichMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String richId) throws IOException {
		logger.info("getRichMsg");

		try{
			Map<String, List<String>> result = contentRichMsgService.getContentRichMsg(richId);

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
	 * 取得圖文訊息列表
	 */
	@ControllerLog(description="取得圖文訊息列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getRichMsgList")
	@ResponseBody
	public ResponseEntity<?> getRichMsgList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getRichMsgList");

		try{
			Map<String, List<String>> result = contentRichMsgService.getAllContentRichMsg();

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
	 * 新增與更新圖文訊息
	 */
	@ControllerLog(description=" 新增與更新圖文訊息")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createRichMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createRichMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@RequestBody RichMsgModel createRichMsgModel, 
			@RequestParam String actionType, 
			@RequestParam String richId) throws IOException {
		try {
			if (!validateData(createRichMsgModel)) {
				throw new BcsNoticeException("必填欄位不可為空！");
			}
			
			String adminUserAccount = customUser.getAccount(); //取得登入者的帳號
			
			ContentRichMsg contentRichMsg = new ContentRichMsg();
			
			List<Map<String, String>> richDetailIdAndLinkIds = new ArrayList<>();
			if (actionType.equals("Edit")) { //變更
				contentRichMsg = contentRichMsgService.getSelectedContentRichMsg(richId);
				contentRichMsgService.getPreDetailIdAndLinkId(richId); //取得原先圖文訊息的DetailId與LinkId
			} else { //新增與複制
				richId = checkDuplicateUUID("1");
				contentRichMsg.setRichId(richId);
			}
			
			contentRichMsg.setRichType(createRichMsgModel.getRichType());
			contentRichMsg.setRichImageId(createRichMsgModel.getRichImageId());
			contentRichMsg.setRichTitle(createRichMsgModel.getRichTitle());
			contentRichMsg.setModifyTime(new Date());
			contentRichMsg.setModifyUser(adminUserAccount);
			contentRichMsg.setStatus(ContentRichMsg.STATUS_ACTIVE);
			
			List<ContentRichMsgDetail> contentRichMsgDetails = new ArrayList<>();
			List<ContentLink> contentLinks = new ArrayList<>();
			Map<String, List<String>> contentFlagMap = new HashMap<>();
			
			for (int i=0, max=createRichMsgModel.getRichMsgImgUrls().size(); i<max; i++) {
				RichMsgModel url = createRichMsgModel.getRichMsgImgUrls().get(i);
				String richDetailId = "";
				String linkId = "";
				if (i+1 > richDetailIdAndLinkIds.size()) { //新增連結
					richDetailId = checkDuplicateUUID("2");
					linkId = checkDuplicateUUID("3");
				} else {
					richDetailId = richDetailIdAndLinkIds.get(i).get("richDetailId");
					linkId = richDetailIdAndLinkIds.get(i).get("linkId");
				}
				
				ContentRichMsgDetail contentRichMsgDetail = new ContentRichMsgDetail();
				contentRichMsgDetail.setRichDetailId(richDetailId);
				contentRichMsgDetail.setRichId(richId);
				contentRichMsgDetail.setRichDetailLetter(url.getRichDetailLetter());
				contentRichMsgDetail.setStartPointX(url.getStartPointX());
				contentRichMsgDetail.setStartPointY(url.getStartPointY());
				contentRichMsgDetail.setEndPointX(url.getEndPointX());
				contentRichMsgDetail.setEndPointY(url.getEndPointY());
				contentRichMsgDetail.setStatus(ContentRichMsgDetail.STATUS_ACTIVE);

				contentRichMsgDetail.setActionType(url.getActionType());
				
				contentRichMsgDetails.add(contentRichMsgDetail);
				
				if(RichMsgAction.ACTION_TYPE_SEND_MESSAGE.equals(url.getActionType())){
					contentRichMsgDetail.setLinkId(url.getLinkUrl());
				}
				else{

					contentRichMsgDetail.setLinkId(linkId);
					
					ContentLink contentLink = new ContentLink();
					contentLink.setLinkId(linkId);
					contentLink.setLinkTag(contentFlagService.concat(url.getLinkTagList(), 50));
					contentLink.setLinkTitle(url.getLinkTitle());
					contentLink.setLinkUrl(url.getLinkUrl());
					contentLink.setModifyTime(new Date());
					contentLink.setModifyUser(adminUserAccount);
					
					contentLinks.add(contentLink);
					
					contentFlagMap.put(contentLink.getLinkId(), url.getLinkTagList());
				}
			}
			
			contentRichMsgService.createRichMsg(contentRichMsg, contentRichMsgDetails, contentLinks, contentFlagMap);
			
			return new ResponseEntity<>("save success", HttpStatus.OK);
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
	 * 檢查必填欄位不可為空
	 */
	public Boolean validateData(RichMsgModel createRichMsgModel) {
		if (StringUtils.isBlank(createRichMsgModel.getRichType()) 
				|| StringUtils.isBlank(createRichMsgModel.getRichType())
				|| StringUtils.isBlank(createRichMsgModel.getRichTitle())
				|| StringUtils.isBlank(createRichMsgModel.getRichImageId())) {
			return false;
		}
		for (int i=0, max=createRichMsgModel.getRichMsgImgUrls().size(); i<max; i++) {
			RichMsgModel url = createRichMsgModel.getRichMsgImgUrls().get(i);
			if (StringUtils.isBlank(url.getLinkUrl())) return false;
		}
		return true;
	}
	
	/** 
	 * 回傳一個沒有重覆的uuid
	 */
	public String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean duplicateUUID = contentRichMsgService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = contentRichMsgService.checkDuplicateUUID(queryType, uuid);
		}
		
		return uuid;
	}
	
	/** 
	 * 刪除圖文訊息
	 */
	@ControllerLog(description="刪除圖文訊息")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteRichMsg/{richId}")
	@ResponseBody
	public ResponseEntity<?> deleteRichMsg(			
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@PathVariable String richId) {
		logger.info("deleteRichMsg");
		
		try {
			// Check Delete Right
			boolean isAdmin = customUser.isAdmin();
			if(isAdmin) {
				contentRichMsgService.deleteRichMsg(richId, customUser.getAccount());
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
