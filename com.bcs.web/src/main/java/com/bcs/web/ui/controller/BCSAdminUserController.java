package com.bcs.web.ui.controller;

import java.io.IOException;
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
public class BCSAdminUserController extends BCSBaseController {
	
	@Autowired
	private AdminUserService adminUserService;
	@Autowired
	private AdminUserUIService adminUserUIService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSAdminUserController.class);
	
	@RequestMapping(method = RequestMethod.GET, value = "/market/adminUserCreatePage")
	public String adminUserCreatePage(HttpServletRequest request, 
			HttpServletResponse response, 
			@CurrentUser CustomUser customUser
			) throws Exception {
		logger.info("adminUserCreatePage");
		String actionType = request.getParameter("actionType");
		
		// 一般使用者不能編輯別人的帳號
		if ("userEdit".equalsIgnoreCase(actionType)) {
			String account = request.getParameter("account");
			
			if (!account.equalsIgnoreCase(customUser.getAccount())) {
				throw new Exception("Save Admin User Fail : You can not edit other people's accounts");
			}
		}
				
		return BcsPageEnum.AdminUserCreatePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/adminUserListPage")
	public String adminUserListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("adminUserListPage");
		return BcsPageEnum.AdminUserListPage.toString();
	}
	
	/**
	 * 查詢權限管理列表
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	@ControllerLog(description="查詢權限管理列表")
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getAdminUserList")
	@ResponseBody
	public ResponseEntity<?> getAdminUserList(
			HttpServletRequest request, HttpServletResponse response,@CurrentUser CustomUser customUser) {
		logger.info("getAdminUserList");
		
		try {
			List<AdminUser> list = adminUserService.findAll();
			
			// 密碼不能傳送到前端
			for (AdminUser adminUser : list) {
				adminUser.setPassword(null);
			}
			
			logger.debug("list:" +ObjectUtil.objectToJsonStr(list));
			
			Map<String, Object> result = new HashMap<>();
			result.put("adminUserList", list);
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
	 * 取得權限管理者
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取得權限管理者")
	@RequestMapping(method = RequestMethod.GET, value = "/market/getAdminUser")
	@ResponseBody
	public ResponseEntity<?> getAdminUser(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String account) throws IOException {
		logger.info("getAdminUser");				
		
		try{
			if(StringUtils.isNotBlank(account)){
				logger.info("account:" + account);
				AdminUser adminUser = adminUserService.findOne(account);
				
				if(adminUser != null){
					adminUser.setPassword(null); // 密碼不能傳送到前端
					return new ResponseEntity<>(adminUser, HttpStatus.OK);
				}
			}
			
			throw new Exception("Account Null");
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
	 * 新增權限管理者(限管理者使用)
	 * 
	 * @param inputLineAccount true 表示有輸入 Line 帳號，false 表示沒有輸入
	 * @param adminUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="新增權限管理者(限管理者使用)")
	@RequestMapping(method = RequestMethod.POST, value = "/admin/createAdminUserForAdmin", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createAdminUserForAdmin(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestParam("inputLineAccount") boolean inputLineAccount, 
			@RequestBody AdminUser adminUser			
		) throws IOException {
		logger.info("createAdminUserForAdmin");
		
		try{
			checkAdminUser(adminUser, true);
			adminUserUIService.createFromUI(adminUser, customUser.getAccount(), inputLineAccount);
			
			// Update MID
			if (customUser.getAccount().equals(adminUser.getAccount()) && inputLineAccount) {
				customUser.setMid(adminUser.getMid());
			}
			return new ResponseEntity<>(null, HttpStatus.OK);
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
	 * 修改權限管理者(限管理者使用)
	 * 
	 * @param inputLineAccount
	 * @param adminUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="修改權限管理者(限管理者使用)")
	@RequestMapping(method = RequestMethod.POST, value = "/admin/saveAdminUserForAdmin", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> saveAdminUserForAdmin(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam("inputLineAccount") boolean inputLineAccount, 
			@RequestBody AdminUser adminUser
			) throws IOException {
		logger.info("saveAdminUserForAdmin");
		
		try{
			checkAdminUser(adminUser, false);
			adminUserUIService.saveFromUI(adminUser, customUser.getAccount(), inputLineAccount);
			
			// Update MID
			if (customUser.getAccount().equals(adminUser.getAccount()) && inputLineAccount) {
				customUser.setMid(adminUser.getMid());
			}
			
			return new ResponseEntity<>(null, HttpStatus.OK);
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
	 * 修改權限管理者(限一般使用者使用)
	 * 
	 * @param inputLineAccount
	 * @param adminUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="修改權限管理者(限一般使用者使用)")
	@RequestMapping(method = RequestMethod.POST, value = "/market/saveAdminUserForUser", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> saveAdminUserForUser(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestParam("inputLineAccount") boolean inputLineAccount, 
			@RequestBody AdminUser adminUser			
			) throws IOException {
		logger.info("saveAdminUserForUser");
		
		try{
			checkAdminUser(adminUser, false);
			adminUserUIService.saveFromUI(adminUser, customUser.getAccount(), inputLineAccount);
			
			// Update MID
			if (customUser.getAccount().equals(adminUser.getAccount()) && inputLineAccount) {
				customUser.setMid(adminUser.getMid());
			}
			
			return new ResponseEntity<>(null, HttpStatus.OK);
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
	 * 取消綁訂 Line 帳號
	 * 
	 * @param account
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="取消綁訂 Line 帳號")
	@RequestMapping(method = RequestMethod.POST, value = "/market/cancelBindLineAccount", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> cancelBindLineAccount(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestParam String account			
			) throws IOException {
		logger.info("cancelBindLineAccount");
		
		try{
			String adminUserAccount = customUser.getAccount();
			boolean isAdmin = customUser.isAdmin();
			
			// 一般使用者不能移除別人的 MID
			if (!isAdmin && !account.equalsIgnoreCase(adminUserAccount)) {
				return new ResponseEntity<>("Can't cancel other people's Line Account", HttpStatus.OK);
			}
			
			adminUserUIService.updateAdminUserMID(account, null, adminUserAccount);
			return new ResponseEntity<>(null, HttpStatus.OK);
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
	 * 刪除權限管理者(限管理者使用)
	 * 
	 * @param inputLineAccount
	 * @param adminUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ControllerLog(description="刪除權限管理者(限管理者使用)")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteAdminUserForAdmin")
	@ResponseBody
	public ResponseEntity<?> deleteAdminUserForAdmin(
			@RequestParam String account, 
			@CurrentUser CustomUser customUser, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("deleteAdminUserForAdmin");
		
		try{
			// 不能刪除自己
			if (account.equalsIgnoreCase(customUser.getAccount())) {
				return new ResponseEntity<>("You can not delete your own account", HttpStatus.OK);
			}
			
			adminUserUIService.deleteFromUI(account, customUser.getAccount());
			return new ResponseEntity<>("Delete Success", HttpStatus.OK);
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
	 * 驗證 adminUser 參數
	 * 
	 * @param adminUser
	 * @param create true 表示新增，false 表示修改
	 * @throws Exception
	 */
	private void checkAdminUser(AdminUser adminUser, boolean create) throws Exception {
		Validate.notNull(adminUser, "AdminUser Null");
		Validate.notBlank(adminUser.getAccount(), "Account Null");
		
		// 密碼，新增時必填，修改可不填
		if(create){
			Validate.notBlank(adminUser.getPassword(), "Password Null");
		}

		Validate.notBlank(adminUser.getRole(), "Role Null");
		Validate.notBlank(adminUser.getUserName(), "UserName Null");
	}
}
