package com.bcs.web.report.controller;

import java.io.IOException;
import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.SystemConfig;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;

@Controller
@RequestMapping("/bcs")
public class BCSConfigController extends BCSBaseController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSConfigController.class);
	@Autowired
	private SystemConfigService systemConfigService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/configListPage")
	public String configListPage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("configListPage");
		
		return BcsPageEnum.ConfigListPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/configCreatePage")
	public String configCreatePage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("configCreatePage");
		
		return BcsPageEnum.ConfigCreatePage.toString();
	}
	
	/**
	 * 取得參數列表
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getConfigList")
	@ResponseBody
	public ResponseEntity<?> getConfigList(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("getConfigList");

		try{
			List<SystemConfig> result = systemConfigService.findAll();

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
	 * 刪除參數
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteConfig/{configId}")
	@ResponseBody
	public ResponseEntity<?> deleteConfig(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, 
			HttpServletResponse response,
			@PathVariable String configId) {
		logger.info("deleteConfig");
		
		try {
			// Check Delete Right
			boolean isAdmin = customUser.isAdmin();
			if(isAdmin) {
				systemConfigService.delete(configId);
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getConfig")
	@ResponseBody
	public ResponseEntity<?> getConfig(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.info("getConfig");

		String configId = request.getParameter("configId");
		
		SystemConfig systemConfig = systemConfigService.findSystemConfig(configId);
		
		return new ResponseEntity<>(systemConfig, HttpStatus.OK);
	}
	
	/**
	 * 新增與更新圖文訊息
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/admin/settingConfig", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> settingConfig(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser,  
			@RequestBody SystemConfig systemConfig) throws IOException {
		try {
			if(systemConfig == null){
				throw new BcsNoticeException("設定錯誤");
			}
			
			if(StringUtils.isBlank(systemConfig.getConfigId())){
				throw new BcsNoticeException("設定參數錯誤");
			}
			
			if(StringUtils.isBlank(systemConfig.getValue())){
				throw new BcsNoticeException("設定數值錯誤");
			}
			
			systemConfigService.save(systemConfig);
			
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
}
