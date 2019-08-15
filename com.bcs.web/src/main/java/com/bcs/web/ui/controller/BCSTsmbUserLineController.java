package com.bcs.web.ui.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import com.bcs.core.taishin.circle.db.service.OracleService;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
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

	/**
	 *  新增功能(解除綁定介面)
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/market/adminUserBoardPage")
	public String adminUserBoardPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("adminUserBoardPage");
		return BcsPageEnum.AdminUserBoardPage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/tsmb/inquireUserLineUrl")
	public ResponseEntity<String> inquireUserLineUrl(HttpServletRequest request, HttpServletResponse response) {
		
		String Url = CoreConfigReader.getString("inquireUserLineUrl");
		return new ResponseEntity<String>(Url, HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/tsmb/unbindUserLineUrl")
	public ResponseEntity<String> unbindUserLineUrl(HttpServletRequest request, HttpServletResponse response) {
		
		String Url = CoreConfigReader.getString("unbindUserLineUrl");
		return new ResponseEntity<String>(Url, HttpStatus.OK);
	}
	

	
}
	