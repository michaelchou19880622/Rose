package com.bcs.core.linepoint.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.linepoint.db.entity.LinePointSendGroup;

public interface LinePointSendGroupRepository extends EntityRepository<LinePointSendGroup, Long>{

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT GROUP_ID, GROUP_TITLE FROM BCS_RICH_MENU_SEND_GROUP ORDER BY GROUP_ID", nativeQuery = true)
	public List<Object[]> findAllGroupIdAndGroupTitle();

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.groupTitle from LinePointSendGroup x where x.groupId = ?1")
	public String findGroupTitleByGroupId(Long groupId);
	
	@Transactional(timeout = 30)
	@Query("select x from LinePointSendGroup x where x.groupType = ?1 order by x.modifyTime desc")
	public List<LinePointSendGroup> findByGroupType(String groupType);
	
	@Transactional(timeout = 30)
	public List<LinePointSendGroup> findByRichMenuGroupId(Long richMenuGroupId);
}
