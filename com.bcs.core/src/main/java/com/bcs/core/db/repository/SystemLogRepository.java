package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SystemLog;
import com.bcs.core.db.persistence.EntityRepository;

public interface SystemLogRepository extends EntityRepository<SystemLog, Long>, SystemLogRepositoryCustom{

	@Transactional(readOnly = true, timeout = 30)
	public Page<SystemLog> findByTargetAndAction(String target, String action, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<SystemLog> findByLevel(String level, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT('x') FROM BCS_SYSTEM_LOG WHERE LEVEL = ?1 AND MODIFY_DAY >= ?2 AND MODIFY_DAY < ?3", nativeQuery = true)
	public Long countByLevel(String level, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT('x') FROM BCS_SYSTEM_LOG WHERE TARGET = ?1 AND ACTION = ?2 AND MODIFY_DAY >= ?3 AND MODIFY_DAY < ?4", nativeQuery = true)
	public Long countByTargetAndAction(String target, String action, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT('x') FROM BCS_SYSTEM_LOG WHERE MODIFY_DAY >= ?1 AND MODIFY_DAY < ?2", nativeQuery = true)
	public Long countAll(String start, String end);
	
	@Transactional(readOnly = true, timeout = 30)
	@Query(value="select * from BCS_SYSTEM_LOG where TARGET = ?1 and ACTION = ?2 and MODIFY_TIME >= ?3 and MODIFY_TIME < ?4 order by MODIFY_USER, MODIFY_TIME", nativeQuery = true)
    public List<SystemLog> findByTargetAndAction(String target, String action, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
    @Query(value="select * from BCS_SYSTEM_LOG where MODIFY_USER = ?1 and LEVEL = ?2 and MODIFY_TIME > ?3 and MODIFY_TIME < ?4", nativeQuery = true)
	public List<SystemLog> findByModifyUserAndLevel(String modifyUser, String level, String start, String end);
}
