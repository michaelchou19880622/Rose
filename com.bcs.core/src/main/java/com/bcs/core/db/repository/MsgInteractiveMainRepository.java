package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgInteractiveMainRepository extends EntityRepository<MsgInteractiveMain, Long>{
	
	@Modifying
	@Query("update MsgInteractiveMain msg set msg.sendCount = msg.sendCount +1 where msg.iMsgId = ?1")
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void increaseSendCountByMsgInteractiveId(Long iMsgId);
	
	@Modifying
	@Query("update MsgInteractiveMain msg set msg.sendCount = msg.sendCount +?2 where msg.iMsgId = ?1")
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void increaseSendCountByMsgInteractiveId(Long iMsgId, Long increase);

	@Transactional(readOnly = true, timeout = 30)
	public List<MsgInteractiveMain> findByInteractiveTypeAndInteractiveStatus(String interactiveType, String interactiveStatus);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value="select MSG_INTERACTIVE_ID from BCS_MSG_INTERACTIVE_MAIN where MAIN_KEYWORD = ?1", nativeQuery = true)
	public List<String> findIMsgIdByKeyword(String keyword);
}
