package com.bcs.web.ui.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.AdminUser.RoleCode;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.m.controller.MobileUserController;

@Controller
@RequestMapping("/m")
public class BCSSsoAdminController {
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSSsoAdminController.class);

	@Autowired
	private AdminUserService adminUserService;
	@Autowired
	private MobileUserController mobileUserController;
	
	@RequestMapping(method = RequestMethod.GET, value = "/loginSSO")
	public String loginSSO(HttpServletRequest request, HttpServletResponse response, 
			Model model) throws Exception {
		logger.info("loginSSO");

		String id = request.getParameter("id");
		logger.info("id:" + id);

		boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
		if(isApi){
			return mobileUserController.indexPage(request, response, model); //APIDiff
		}
		else{
			return BcsPageEnum.LoginSSOPage.toString();
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/loginSSO/getusername") 
	public ResponseEntity<?> getUsername(HttpServletRequest request, HttpServletResponse response) throws Exception {

		boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
		if(isApi){
			return new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR); // APIDiff
		}
		else{
	        try {
				String id = request.getParameter("id");
				String linebc = request.getParameter("roles");
	//			String action = request.getParameter("action");
	
				byte[] decoded = Base64.getDecoder().decode(id);
				String account = new String(decoded);
				logger.info("account:" + account);
	
				AdminUser adminUser = adminUserService.findOne(account);
				
				if(adminUser == null){
					throw new Exception("登入失敗");
				}
	
				String role = "";
				boolean roleCheck = false;
				if(StringUtils.isBlank(linebc)){
	//				throw new Exception("LinebcNull");
					role = adminUser.getRole();
				}
				else{
					RoleCode[] roles = RoleCode.values();
					for(RoleCode code : roles){
						if(code.getRoleName().equals(linebc)){
							role = code.getRoleId();
							roleCheck = true;
						}
					}
					for(RoleCode code : roles){
						if(code.getRoleNameEn().equals(linebc)){
							role = code.getRoleId();
							roleCheck = true;
						}
					}
					if(roleCheck){
						String oldRole = adminUser.getRole();
						if(oldRole.equals(role)){
							// Skip
						}
						else{
							adminUser.setRole(role);
							adminUserService.save(adminUser);
						}
					}
					else{
						throw new Exception("權限設定錯誤");
					}
				}
				
				Map<String, String> result = new HashMap<String, String>();
				result.put("account", account);
				String prefix = CoreConfigReader.getString(CONFIG_STR.PASSWORD_PREFIX, true);
				String suffix = CoreConfigReader.getString(CONFIG_STR.PASSWORD_SUFFIX, true);
				result.put("password", prefix + account + suffix);
	
				String loginurl = CoreConfigReader.getString(CONFIG_STR.SSO_LOGIN_URL, true);
				result.put("loginurl", loginurl);
	
				logger.info("result:" + result);
				return new ResponseEntity<>(result, HttpStatus.OK);
	        }
	    	catch(Exception e){
				logger.error(ErrorRecord.recordError(e));
				
				Map<String, String> result = new HashMap<String, String>();
				String loginurl = CoreConfigReader.getString(CONFIG_STR.SSO_LOGIN_URL, true);
				result.put("loginurl", loginurl);
				result.put("msg", e.getMessage());
				
				return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
	        }
		}
	}
}
