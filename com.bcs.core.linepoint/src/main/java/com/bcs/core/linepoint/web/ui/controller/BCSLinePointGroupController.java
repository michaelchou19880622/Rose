package com.bcs.core.linepoint.web.ui.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
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


// Core Original
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
// Core Added
import com.bcs.core.linepoint.web.ui.service.LinePointContentUIService;

// Web Original
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;


@Controller
@RequestMapping("/bcs")
public class BCSLinePointGroupController extends BCSBaseController {
	
//	/** Logger */
//	private static Logger logger = Logger.getLogger(BCSLinePointGroupController.class);	
//	@Autowired
//	RichMenuGroupService richMenuGroupService;
//	@Autowired
//	RichMenuContentService richMenuContentService;
//	@Autowired
//	LinePointContentUIService richMenuContentUIService;
//	
//	// RichMenuGroupList Page
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/richMenuGroupListPage")
//	public String richMenuGroupListPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("richMenuGroupListPage");
//		return BcsPageEnum.RichMenuGroupListPage.toString();
//	}
//
//	// RichMenuBindingPage Page
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/richMenuBindingPage")
//	public String richMenuBindingPage(HttpServletRequest request, HttpServletResponse response) {
//		logger.info("richMenuBindingPage");
//		return BcsPageEnum.RichMenuBindingPage.toString();
//	}
//	
//	// get All Active RichMenuGroup List
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/getRichMenuGroupList")
//	@ResponseBody
//	public ResponseEntity<?> getRichMenuGroupList(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		logger.info("getRichMenuGroupList");
//		try {
//			List<RichMenuGroup> result = new ArrayList();
//			List<RichMenuGroup> list = richMenuGroupService.findAllActiveListTimeDesc();
//			result.addAll(list);
//			logger.info("getRichMenuGroupList result:" + ObjectUtil.objectToJsonStr(result));
//			return new ResponseEntity<>(result, HttpStatus.OK);
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			if (e instanceof BcsNoticeException) 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			else 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//
//	// search Active RichMenuGroup List
//	@RequestMapping(method = RequestMethod.GET, value = "/edit/searchRichMenuGroupList")
//	@ResponseBody
//	public ResponseEntity<?> searchRichMenuGroupList(HttpServletRequest request, HttpServletResponse response,
//			@RequestParam String richMenuGroupName) throws IOException {
//		logger.info("searchRichMenuGroupList");
//		logger.info("richMenuGroupName:"+richMenuGroupName);
//		try {
//			List<RichMenuGroup> result = new ArrayList();
//			List<RichMenuGroup> list = richMenuGroupService.findLikeRichMenuGroupName(richMenuGroupName);
//			logger.info("list:" + list);
//			
//			result.addAll(list);
//			logger.info("searchRichMenuGroupList result:" + ObjectUtil.objectToJsonStr(result));
//			return new ResponseEntity<>(result, HttpStatus.OK);
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			if (e instanceof BcsNoticeException) 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			else 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//	
//	// create RichMenuGroup
//	@RequestMapping(method = RequestMethod.POST, value = "/edit/createRichMenuGroup", consumes = MediaType.APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public ResponseEntity<?> createRichMenuGroup(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @RequestBody RichMenuGroup richMenuGroup) throws IOException {
//		logger.info("createRichMenuGroup");
//		try {
//			// check null
//			if (richMenuGroup == null) {
//				throw new Exception("RichMenuGroup is null");
//			}
//			
//			// check duplicate
//			List<RichMenuGroup> duplicate = richMenuGroupService.findByRichMenuGroupName(richMenuGroup.getRichMenuGroupName());
//			logger.info("duplicate:" + duplicate.toString());
//			logger.info("duplicate.isEmpty():" + duplicate.isEmpty());
//			if(!duplicate.isEmpty()) {
//				return new ResponseEntity<>("Duplication", HttpStatus.OK);
//			}
//			
//			// save
//			richMenuGroup.setModifyUser(customUser.getAccount());
//			richMenuGroup.setModifyTime(new Date());
//			richMenuGroup.setStatus(RichMenuGroup.STATUS_ACTIVE);
//			richMenuGroupService.save(richMenuGroup);
//			return new ResponseEntity<>("Success", HttpStatus.OK);
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			if (e instanceof BcsNoticeException) 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			else 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//
//	// Delete RichMenuGroup
//	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteRichMenuGroup/{richMenuGroupId}")
//	@ResponseBody
//	public ResponseEntity<?> deleteRichMenuGroup(HttpServletRequest request, HttpServletResponse response,
//			@CurrentUser CustomUser customUser, @PathVariable Long richMenuGroupId) {
//		logger.info("deleteRichMenu");
//		try {
//			// check Admin
//			boolean isAdmin = customUser.isAdmin();
//			if(isAdmin) {
//				// findOne
//				RichMenuGroup richMenuGroup = richMenuGroupService.findOne(richMenuGroupId);
//				
//				// set status = Delete 
//				richMenuGroup.setModifyUser(customUser.getAccount());
//				richMenuGroup.setModifyTime(new Date());
//				richMenuGroup.setStatus(RichMenuGroup.STATUS_DELETE);
//				
//				// save
//				richMenuGroupService.save(richMenuGroup);
//				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
//			} else 
//				throw new BcsNoticeException("此帳號沒有刪除權限");
//		} catch(Exception e) {
//			logger.error(ErrorRecord.recordError(e));	
//			if(e instanceof BcsNoticeException)
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			else
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
//	
//	// Rich Menu Binding
//	@RequestMapping(method = RequestMethod.POST, value = "/edit/richMenuBinding", consumes = MediaType.APPLICATION_JSON_VALUE)
//	@ResponseBody
//	public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
//			@RequestBody List<String> uids, @RequestParam Long richMenuGroupId) throws IOException {
//		try {
//				// Get Main Page
//				logger.info("richMenuGroupId:"+richMenuGroupId);
//				List<RichMenuContent> richMenuContents = richMenuContentService.getRichMenuListByRichMenuGroupIdAndLevel(richMenuGroupId, RichMenuContent.LEVEL_MAIN);
//				logger.info("richMenuContents:"+richMenuContents);
//				if(richMenuContents.size() != 1) {
//					return new ResponseEntity<>("UniqueMainCheckFailed",HttpStatus.OK);
//				}
//				RichMenuContent richMenuContent = richMenuContents.get(0);
//				logger.info("richMenuContent:"+richMenuContent);
//				if(richMenuContent == null) {
//					return new ResponseEntity<>("PleaseActivateTheMainRichMenu",HttpStatus.OK);
//				}
//				String richMenuId = richMenuContent.getRichMenuId();
//				logger.info("richMenuId:"+richMenuId);
//				
//				// Modify
//				for(String uid : uids) {
//					richMenuContentUIService.callLinkRichMenuToUserAPI(richMenuId, uid);			
//				}
//				return new ResponseEntity<>("",HttpStatus.OK);
//		} catch (Exception e) {
//			logger.error(ErrorRecord.recordError(e));
//			if (e instanceof BcsNoticeException) 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
//			else 
//				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//	}
}
