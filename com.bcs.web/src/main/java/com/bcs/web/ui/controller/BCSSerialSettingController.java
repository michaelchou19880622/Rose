package com.bcs.web.ui.controller;

import java.io.IOException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.SerialSetting;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.SerialSettingService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.SerialSettingUIService;


@Controller
@RequestMapping("/bcs")
public class BCSSerialSettingController extends BCSBaseController {

	@Autowired
	private SerialSettingUIService serialSettingUIService;
	@Autowired
	private SerialSettingService serialSettingService;
	@Autowired
	private AdminUserService adminUserService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSSerialSettingController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/serialSettingPage")
	public String serialSettingPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("serialSettingPage");
		return BcsPageEnum.SerialSettingPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/serialSettingListPage")
	public String serialSettingListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("serialSettingListPage");
		return BcsPageEnum.SerialSettingListPage.toString();
	}
	
	@ControllerLog(description="uploadSerialSetting")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/uploadSerialSetting")
	@ResponseBody
	public ResponseEntity<?> uploadSerialSetting(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestPart MultipartFile filePart,
			@RequestParam(required=false) String SerialId
			) throws IOException {
		logger.info("uploadSerialSetting");

		try{
			if(filePart != null){
				if(StringUtils.isBlank(SerialId)){
					SerialId = UUID.randomUUID().toString().toLowerCase();
				}
				
				String modifyUser = customUser.getAccount();
				logger.info("modifyUser:" + modifyUser);
				
				Map<String, Object> result = serialSettingUIService.uploadSerialSetting(filePart, modifyUser, new Date(), SerialId);
				
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
	
	@ControllerLog(description="getSerialSettingList")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getSerialSettingList")
	public ResponseEntity<?> getSerialSettingList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) {
		logger.info("getSerialSettingList");
		
		Map<String, Object> result = new HashMap<String, Object>();
		List<SerialSetting> list = serialSettingService.findByLevel(SerialSetting.SERIAL_LEVEL_MAIN);
		
		result.put("SerialSettingList", list);

		/**
		 * AdminUser Result Map
		 */
		try{
			Map<String, AdminUser> admins = adminUserService.findAllMap();
			Map<String, String> adminMap = new HashMap<String, String>();
			for(SerialSetting serialSetting : list){
				String userAccount = serialSetting.getModifyUser();
				if(admins.containsKey(userAccount)){
					adminMap.put(userAccount, admins.get(userAccount).getUserName());
				}
			}
			result.put("AdminUser", adminMap);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ControllerLog(description="getSerialSetting")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getSerialSetting")
	@ResponseBody
	public ResponseEntity<?> getSerialSetting(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam(required=false) String SerialId) throws IOException {
		logger.info("getSerialSetting");

		try{
			boolean hasData = false;
			
			Map<String, Object> result = new HashMap<String, Object>();
			
			if(StringUtils.isNotBlank(SerialId)){
			
				SerialSetting serialSetting = serialSettingService.findOne(SerialId);
				if(serialSetting != null){

					result.put("settingTime", serialSetting.getModifyTime().toString());
					result.put("settingFile", serialSetting.getSerialTitle());
					
					result.put("settingCount", serialSetting.getSerialCount());
					
					hasData = true;
				}
			}
			
			if(!hasData){
				result.put("settingTime", "未設定");
				result.put("settingFile", "未設定");
				result.put("settingCount", "未設定");
			}
			
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
	
	@ControllerLog(description="deleteSerialSetting")
	@RequestMapping(method = RequestMethod.POST, value = "/admin/deleteSerialSetting")
	@ResponseBody
	public ResponseEntity<?> deleteSerialSetting(		
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String SerialId) throws IOException {
		logger.info("deleteSerialSetting");

		try{			
			Map<String, Object> result = serialSettingUIService.deleteSerialSetting(SerialId, customUser.getAccount());

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
}
