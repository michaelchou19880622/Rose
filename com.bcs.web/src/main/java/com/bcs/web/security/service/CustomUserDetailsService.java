package com.bcs.web.security.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.web.security.CustomUser;

/**
 * Spring Security 查詢登入者 UserDetails 的 Service
 * 
 * @author Kevin
 * 
 */
public class CustomUserDetailsService implements UserDetailsService {

	private static Logger logger = Logger
			.getLogger(CustomUserDetailsService.class);

	@Autowired
	private AdminUserService adminUserService;

	@Override
	public UserDetails loadUserByUsername(String account)
			throws UsernameNotFoundException {
		AdminUser adminUser = adminUserService.findByAccountIgnoreCase(account);

		if (adminUser == null) {
			logger.info("User not found");
			throw new UsernameNotFoundException("Account not found");
		}

		return new CustomUser(adminUser);
	}
}
