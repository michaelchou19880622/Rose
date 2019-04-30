package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SystemConfig;
import com.bcs.core.db.persistence.EntityRepository;

public interface SystemConfigRepository extends EntityRepository<SystemConfig, String>{

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT CONFIG_ID, VALUE, MODIFY_TIME FROM BCS_SYSTEM_CONFIG WHERE CONFIG_ID LIKE ?1", nativeQuery = true)
	public List<Object[]> findLikeConfigId(String configId);
}
