package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.persistence.EntityRepository;

public interface AdminUserRepository extends EntityRepository<AdminUser, String>{
	@Transactional(readOnly = true, timeout = 30)
	AdminUser findByAccountAndPassword(String account, String password);

	@Transactional(readOnly = true, timeout = 30)
	AdminUser findByAccountIgnoreCase(String account);

	@Transactional(readOnly = true, timeout = 30)
	List<AdminUser> findByMidNotNull();
}
