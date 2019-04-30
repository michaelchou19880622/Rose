package com.bcs.web.ui.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.m.controller.MobileUserController;

@Controller
@RequestMapping("/bcs")
public class BCSIndexController extends BCSBaseController {
	@Autowired
	private MobileUserController mobileUserController;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSIndexController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/login")
	public String loginPage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response, 
			Model model) throws IOException {
		logger.info("loginPage");
		
		this.saveLog("Login", customUser.getAccount());

		boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
		if(isApi){
			return mobileUserController.indexPage(request, response, model); //APIDiff
		}
		else{
			return BcsPageEnum.MainPage.toString();
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/index")
	public String indexPage(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response, 
			Model model) {
		logger.info("indexPage");

		boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
		if(isApi){
			return mobileUserController.indexPage(request, response, model); //APIDiff
		}
		else{
			return BcsPageEnum.MainPage.toString();
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/doLogin")
	public String doLoginGet(HttpServletRequest request, HttpServletResponse response, 
			Model model) {
		logger.info("doLoginGet");

		boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
		if(isApi){
			return mobileUserController.indexPage(request, response, model); //APIDiff
		}
		else{
			String web = request.getParameter("web");
			String loginUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_LOGIN_URL.toString(), true);
			if(StringUtils.isNotBlank(loginUrl) && StringUtils.isBlank(web)){
				model.addAttribute("linkDefault", loginUrl);
				
				return MobilePageEnum.PageRedirect.toString();
			}
			else{
				return BcsPageEnum.LoginPage.toString();
			}
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/doLogout")
	public String doLogout(
			@CurrentUser CustomUser customUser,  
			HttpServletRequest request, HttpServletResponse response, 
			Model model) {
		logger.info("doLogout");
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){    
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        
        this.saveLog("Logout", customUser.getAccount());
        
        return "redirect:/bcs/doLogin?logout";
	}
	
	private void saveLog(String action, String account){
		SystemLogUtil.saveLogByLevel("AdminUser", action, account, null, null, SystemLog.SYSTEM_LOG_LEVEL_INFO);
	}
}
