package com.bcs.web.ui.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import com.bcs.core.aspect.annotation.WebServiceLog;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.UserUnbind;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.UserUnbindService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.IpUtil;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.utils.RestfulUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.AdminUserUIService;


@Controller
@RequestMapping("/bcs")
public class BCSTsmbUserLineController extends BCSBaseController {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSAdminUserController.class);
	@Autowired
	private UserUnbindService userUnbindService;
	
	/**
	 *  新增功能(解除綁定介面)
	 */
	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/market/adminUserBoardPage")
	public String adminUserBoardPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("adminUserBoardPage");
		return BcsPageEnum.AdminUserBoardPage.toString();
	}
	
	@WebServiceLog
	@RequestMapping(method = RequestMethod.POST, value = "/tsmb/inquireUserLineUrl" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> inquireUserLineUrl(HttpServletRequest request, 
												HttpServletResponse response ,
												@CurrentUser CustomUser customUser,
												@RequestParam String userId){
		try {
			logger.info("-------------------- api inquireUserId  --------------------");
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			
			JSONObject requestBody = new JSONObject();
			String url = CoreConfigReader.getString("inquireUserLineUrl");
			requestBody.put("SourceChannel", "TSLINE_P");
			requestBody.put("CustId", userId);
			logger.info("CserId : " + userId);
			logger.info("url : " + url);
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity,false);
			JSONObject responseObject = null;    

			try {
				responseObject = restfulUtil.execute();
				Map<String, Object> jsonMap = responseObject.toMap();
				logger.info("jsonMap : " + jsonMap);
				logger.info("responseObject:"+responseObject.toString());
				UserUnbind userUnbind = new UserUnbind();
				userUnbind.setAccountId(userId);
				userUnbind.setModifyIp(IpUtil.getIpAddress(request));
				userUnbind.setModifyId(customUser.getAccount());
				userUnbind.setAction(UserUnbind.CHECK);
				userUnbind.setModifyTime(new Date());
				userUnbind.setActionResult(jsonMap.get("ReturnCode").toString() + " " + jsonMap.get("ReturnMessage").toString() );
				if(UserUnbind.S001.equals(jsonMap.get("ReturnCode"))) {
					Map<String, Object> returnData = (Map<String, Object>)jsonMap.get("ReturnData");
					userUnbind.setAccountUid(returnData.get("Luid").toString());
				}else {
					userUnbind.setAccountUid("");
				}
				userUnbindService.save(userUnbind);
				//userUnbind.setActionResult();
				
				
			}catch(HttpClientErrorException e){
				logger.info("[inquireUserLineUrl]  Status code: " + e.getStatusCode());
				logger.info("[inquireUserLineUrl]  Response body: " + e.getResponseBodyAsString());
				return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
			}
			
			return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
		}catch(Exception e) {
			logger.info("e:"+e.toString());
			return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@WebServiceLog
	@RequestMapping(method = RequestMethod.POST, value = "/tsmb/unbindUserLineUrl" , consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> unbindUserLineUrl(HttpServletRequest request, 
													HttpServletResponse response,
													@CurrentUser CustomUser customUser,
													@RequestParam String userId) {
		try {
			logger.info("-------------------- api unbindUserLineUrl  --------------------");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			
			JSONObject requestBody = new JSONObject();
			String url = CoreConfigReader.getString("unbindUserLineUrl");
			requestBody.put("SourceChannel", "TSLINE_P");
			requestBody.put("CustId", userId);
			requestBody.put("UpdateUser", customUser.getAccount());
			
			logger.info("CserId : " + userId);
			logger.info("customUserid : " + customUser.getAccount());
			logger.info("url : " + url);
			
			HttpEntity<String> httpEntity = new HttpEntity<String>(requestBody.toString(), headers);
			RestfulUtil restfulUtil = new RestfulUtil(HttpMethod.POST, url, httpEntity , false);
			JSONObject responseObject = null;
			
			try {
				responseObject = restfulUtil.execute();
				logger.info("responseObject:"+responseObject.toString());
				
				Map<String, Object> jsonMap = responseObject.toMap();
				logger.info("jsonMap : " + jsonMap);
				UserUnbind userUnbind = new UserUnbind();
				userUnbind.setAccountId(userId);
				userUnbind.setModifyIp(IpUtil.getIpAddress(request));
				userUnbind.setModifyId(customUser.getAccount());
				userUnbind.setAction(UserUnbind.UNBIND);
				userUnbind.setModifyTime(new Date());
				userUnbind.setActionResult(jsonMap.get("ReturnCode").toString() + " " + jsonMap.get("ReturnMessage").toString() );
				if(UserUnbind.S001.equals(jsonMap.get("ReturnCode"))) {
					Map<String, Object> returnData = (Map<String, Object>)jsonMap.get("ReturnData");
					userUnbind.setAccountUid(returnData.get("Luid").toString());
				}else {
					userUnbind.setAccountUid("");
				}
				userUnbindService.save(userUnbind);
				
			}catch(HttpClientErrorException e){
				logger.info("[unbindUserLineUrl]  Status code: " + e.getStatusCode());
				logger.info("[unbindUserLineUrl]  Response body: " + e.getResponseBodyAsString());
				return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
			}

			return new ResponseEntity<>(responseObject.toString(), HttpStatus.OK);
			
			
			
		}catch(Exception e) {
			logger.info("e:"+e.toString());
			return new ResponseEntity<>("{\"error\": \"true\", \"message\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	
}
	