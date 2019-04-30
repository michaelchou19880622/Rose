package com.bcs.core.db.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.repository.AdminUserRepository;

@Service
public class AdminUserService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(AdminUserService.class);
	
	@Autowired
	private AdminUserRepository adminUserRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
		
	public AdminUser findOne(String account) {
		return adminUserRepository.findOne(account);
	}
	
	public AdminUser findByAccountIgnoreCase(String account) {
		return adminUserRepository.findByAccountIgnoreCase(account);
	}
	
	public List<AdminUser> findAll(){
		return adminUserRepository.findAll();
	}
	
	public Map<String, AdminUser> findAllMap(){
		List<AdminUser> admins = adminUserRepository.findAll();
		Map<String, AdminUser> result = new HashMap<String, AdminUser>();
		
		for(AdminUser user : admins){
			result.put(user.getAccount(), user);
		}
		
		return result;
	}
	
	public AdminUser findByAccountAndPassword(String account, String password) throws Exception{
		// Encryption password
		AdminUser admin = adminUserRepository.findByAccountAndPassword(account, passwordEncoder.encode(password));
		return admin;
	}
	
	public List<AdminUser> findByMidNotNull(){
		return adminUserRepository.findByMidNotNull();
	}
	
	public void save(AdminUser adminUser){
		adminUserRepository.save(adminUser);
	}
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void delete(String account){
		logger.debug("delete:" + account);
		adminUserRepository.delete(account);
	}
}
