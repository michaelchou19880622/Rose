package com.bcs.web.ui.service;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.LineUserService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.utils.LineIdUtil;

@Service
public class AdminUserUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(AdminUserUIService.class);
	
	@Autowired
	private AdminUserService adminUserService;
	
	@Autowired
	private LineUserService lineUserService;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	public AdminUser loginAdminUser(String tid, String tpasswd) throws Exception{
		
		if(StringUtils.isBlank(tid)){
			throw new Exception("Login Admin User Fail : Need Account");
		}

		if(StringUtils.isBlank(tpasswd)){
			throw new Exception("Login Admin User Fail : Need Password");
		}
		
		AdminUser adminUser = adminUserService.findByAccountAndPassword(tid, tpasswd);

		if(adminUser != null){
			adminUser.setPassword(null);
			return adminUser;
		}
		else{

			throw new Exception("Login Admin User Fail");
		}
	}
	
	/**
	 * 新增權限管理者
	 * 
	 * @param adminUser
	 * @param adminUserAccount
	 * @param inputLineAccount
	 * @return
	 * @throws Exception 
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public AdminUser createFromUI(AdminUser adminUser, String adminUserAccount, boolean inputLineAccount) throws Exception {
		logger.info("createFromUI:" + adminUser);

		// Set Modify Admin User
		adminUser.setModifyUser(adminUserAccount);
		adminUser.setModifyTime(new Date());
		adminUser.setCanCopy(false);
		adminUser.setCanPrinting(false);
		adminUser.setCanSave(false);
		
		// 前端傳來的 mid
		if (inputLineAccount) {
			String mid = adminUser.getMid();
			boolean UIDcheck = LineIdUtil.isLineUID(mid);
			if(UIDcheck){
				adminUser.setMid(mid);
			}
			else{
				throw new BcsNoticeException("UID 錯誤");
			}
		} else if (StringUtils.isBlank(adminUser.getMid())) {
			adminUser.setMid(null);
		}

		if(StringUtils.isNotBlank(adminUser.getPassword())){
			// Encryption password
			adminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
		}
		
		adminUserService.save(adminUser);
		adminUser = adminUserService.findOne(adminUser.getAccount());
		createSystemLog("Create", adminUser, adminUser.getModifyUser(), adminUser.getModifyTime());
		return adminUser;
	}
	
	/**
	 * 修改權限管理者
	 * 
	 * @param adminUser
	 * @param adminUserAccount
	 * @param inputLineAccount
	 * @return
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public AdminUser saveFromUI(AdminUser adminUser, String adminUserAccount, boolean inputLineAccount) throws Exception {
		AdminUser oldAdminUser = adminUserService.findOne(adminUser.getAccount());
		
		// 不能修改自己的管理權限
		if (adminUserAccount.equals(adminUser.getAccount()) 
				&& !oldAdminUser.getRole().equals(adminUser.getRole())) {
			throw new Exception("Save Admin User Fail : You can not edit your permission");
		}
		
		oldAdminUser.setRole(adminUser.getRole());
		oldAdminUser.setUserName(adminUser.getUserName());
		
		// 若有輸入密碼才修改密碼
		if (StringUtils.isNotBlank(adminUser.getPassword())) {
			// Encryption password
			oldAdminUser.setPassword(passwordEncoder.encode(adminUser.getPassword()));
		}
				
		// Set Modify Admin User
		oldAdminUser.setModifyUser(adminUserAccount);
		oldAdminUser.setModifyTime(new Date());
		oldAdminUser.setCanCopy(false);
		oldAdminUser.setCanPrinting(false);
		oldAdminUser.setCanSave(false);
		
		// 前端傳來的 mid
		if (inputLineAccount) {
			String mid = adminUser.getMid();
			boolean UIDcheck = LineIdUtil.isLineUID(mid);
			if(UIDcheck){
				oldAdminUser.setMid(mid);
			}
			else{
				throw new BcsNoticeException("UID 錯誤");
			}
		}
		
		adminUserService.save(oldAdminUser);
		createSystemLog("Edit", oldAdminUser, oldAdminUser.getModifyUser(), oldAdminUser.getModifyTime());
		return oldAdminUser;
	}
	
	/**
	 * 修改權限管理者所綁訂的 Line 帳號
	 * 
	 * @param account
	 * @param mid
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void updateAdminUserMID(String account, String mid, String adminUserAccount) {
		AdminUser adminUser = adminUserService.findOne(account);
		adminUser.setMid(mid);
		
		// Set Modify Admin User
		adminUser.setModifyUser(adminUserAccount);
		adminUser.setModifyTime(new Date());
		adminUserService.save(adminUser);
		createSystemLog("Edit", adminUser, adminUser.getModifyUser(), adminUser.getModifyTime());
	}
	
	/**
	 * 刪除權限管理者
	 * 
	 * @param account
	 */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteFromUI(String account, String adminUserAccount) {
		logger.info("deleteFromUI:" + account);
		adminUserService.delete(account);
		createSystemLog("Delete", account, adminUserAccount, new Date(), account);
	}
	
	/**
	 * 新增系統日誌，不記錄使用者密碼
	 * 
	 * @param action
	 * @param adminUser
	 * @param modifyUser
	 * @param modifyTime
	 */
	private void createSystemLog(String action, AdminUser adminUser, String modifyUser, Date modifyTime) {
		
		// 系統日誌不需知道使用者密碼
		String password = adminUser.getPassword();
		adminUser.setPassword(null);
		createSystemLog(action, (Object) adminUser, modifyUser, modifyTime, adminUser.getAccount());
		
		// 恢復密碼，不然交易結束時密碼真的會被清空
		adminUser.setPassword(password);
	}
	
	/**
	 * 新增系統日誌
	 * 
	 * @param action
	 * @param content
	 * @param modifyUser
	 * @param modifyTime
	 */
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
		SystemLogUtil.saveLogDebug("AdminUser", action, modifyUser, content, referenceId);
	}
}
