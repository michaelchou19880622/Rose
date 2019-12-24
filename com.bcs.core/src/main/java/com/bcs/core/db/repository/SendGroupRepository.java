package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SendGroup;
import com.bcs.core.db.persistence.EntityRepository;

public interface SendGroupRepository extends EntityRepository<SendGroup, Long>{

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT GROUP_ID, GROUP_TITLE FROM BCS_SEND_GROUP ORDER BY GROUP_ID", nativeQuery = true)
	public List<Object[]> findAllGroupIdAndGroupTitle();

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.groupTitle from SendGroup x where x.groupId = ?1")
	public String findGroupTitleByGroupId(Long groupId);
	
	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "select count(*) from BCS_LINE_POINT_MAIN where APPEND_MESSAGE_ID = ?1", nativeQuery = true)
	public int findLinePointMaincount(Long msgId);
}
