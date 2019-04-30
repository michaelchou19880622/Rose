package com.bcs.core.db.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgSendMainRepository extends EntityRepository<MsgSendMain, Long>, MsgSendMainRepositoryCustom{
	
	@Modifying
	@Query("update MsgSendMain msg set msg.sendCount = msg.sendCount +1 where msg.msgSendId = ?1")
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void increaseSendCountByMsgSendId(Long msgSendId);
	
	@Modifying
	@Query("update MsgSendMain msg set msg.sendCount = msg.sendCount +?2 where msg.msgSendId = ?1")
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void increaseSendCountByMsgSendId(Long msgSendId, Long increase);
}
