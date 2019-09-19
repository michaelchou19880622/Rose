package com.bcs.web.ui.controller;

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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author ???
 */
@Slf4j
@Controller
@RequestMapping("/bcs")
public class BCSIndexController extends BCSBaseController {
    @Autowired
    private MobileUserController mobileUserController;

    @GetMapping("/login")
    public String loginPage(@CurrentUser CustomUser customUser, HttpServletRequest request, HttpServletResponse response, Model model) {
        log.info("loginPage");

        this.saveLog("Login", customUser.getAccount());

        boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
        log.info("isApi      : " + isApi);
        String url;
        if (isApi) {
            url = mobileUserController.indexPage(request, response, model);
        } else {
            url = BcsPageEnum.MainPage.toString();
        }
        log.info("Return Url : " + url);
        return url;
    }

    @GetMapping("/index")
    public String indexPage(@CurrentUser CustomUser customUser, HttpServletRequest request, HttpServletResponse response, Model model) {
        log.info("indexPage");

        boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
        log.info("isApi      : " + isApi);
        String url;
        if (isApi) {
            url = mobileUserController.indexPage(request, response, model);
        } else {
            url = BcsPageEnum.MainPage.toString();
        }

        log.info("Return Url : " + url);
        return url;
    }

    @GetMapping("/doLogin")
    public String doLoginGet(HttpServletRequest request, HttpServletResponse response, Model model) {
        log.info("doLoginGet");

        boolean isApi = CoreConfigReader.getBoolean(CONFIG_STR.SYSTEM_TYPE_IS_API);
        log.info("isApi      : " + isApi);
        String url;
        if (isApi) {
            url = mobileUserController.indexPage(request, response, model);
        } else {
            String web = request.getParameter("web");
            String loginUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_LOGIN_URL.toString(), true);
            log.info("Web        : " + web);
            log.info("Login URL  : " + loginUrl);
            if (StringUtils.isNotBlank(loginUrl) && StringUtils.isBlank(web)) {
                model.addAttribute("linkDefault", loginUrl);
                url = MobilePageEnum.PageRedirect.toString();
            } else {
                url = BcsPageEnum.LoginPage.toString();
            }
        }

        log.info("Return Url : " + url);
        return url;
    }

    @GetMapping("/doLogout")
    public String doLogout(@CurrentUser CustomUser customUser, HttpServletRequest request, HttpServletResponse response, Model model) {
        log.info("doLogout");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        this.saveLog("Logout", customUser.getAccount());

        return "redirect:/bcs/doLogin?logout";
    }

    private void saveLog(String action, String account) {
        SystemLogUtil.saveLogByLevel("AdminUser", action, account, null, null, SystemLog.SYSTEM_LOG_LEVEL_INFO);
    }
}
