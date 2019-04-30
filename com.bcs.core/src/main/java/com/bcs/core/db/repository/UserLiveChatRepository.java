package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.UserLiveChat;
import com.bcs.core.db.persistence.EntityRepository;

public interface UserLiveChatRepository extends EntityRepository<UserLiveChat, Long> {	
	@Query(value = "SELECT TOP 1 * FROM BCS_USER_LIVE_CHAT WHERE UID = ?1", nativeQuery = true)
	UserLiveChat findByUID(String UID);

	UserLiveChat findByChatId(Long chatId);
	
	UserLiveChat findByChatIdAndHash(Long chatId, String hash);
	
	List<UserLiveChat> findByStatus(String status);
	
	@Query(value = "SELECT * FROM BCS_USER_LIVE_CHAT WHERE UID = ?1 AND LEAVE_MESSAGE_STATE = ?2", nativeQuery = true)
	List<UserLiveChat> findByUIDAndState(String UID, String leaveMessageState);
	
	@Query(value = "SELECT * FROM BCS_USER_LIVE_CHAT WHERE STATUS = 'WAITING' OR STATUS = 'IN_PROGRESS'", nativeQuery = true)
	List<UserLiveChat> findWaitingAndInProgressUser();
	
	@Query(value = "SELECT TOP 1 * FROM BCS_USER_LIVE_CHAT WHERE UID = ?1 AND (STATUS = 'WAITING' OR STATUS = 'IN_PROGRESS')", nativeQuery = true)
	UserLiveChat findWaitingAndInProgressByUID(String UID);
	
	@Query(value = "SELECT TOP 1 * FROM BCS_USER_LIVE_CHAT WHERE UID = ?1 AND STATUS = ?2", nativeQuery = true)
	UserLiveChat findByUIDAndStatus(String UID, String status);
	
	@Query(value = "SELECT TOP 1 * FROM BCS_USER_LIVE_CHAT WHERE UID = ?1 AND STATUS <> 'FINISH' AND STATUS <> 'DISCARD'", nativeQuery = true)
	UserLiveChat findByUIDAndNotFinishAndNotDiscrad(String UID);
	
	@Query(value = "SELECT TOP 1 * FROM BCS_USER_LIVE_CHAT WHERE UID = ?1 AND LEAVE_MESSAGE_STATE = ?2", nativeQuery = true)
	UserLiveChat findLeaveMsgUserByUIDAndState(String UID, String state);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE BCS_USER_LIVE_CHAT SET LEAVE_MESSAGE_STATE = ?2, MODIFY_TIME = CURRENT_TIMESTAMP WHERE CHAT_ID = ?1", nativeQuery = true)
	void updateLeaveMsgState(Long chatId, String state);

	@Modifying
	@Transactional
	@Query(value = "UPDATE BCS_USER_LIVE_CHAT SET LEAVE_MESSAGE_STATE = NULL, MESSAGE = NULL WHERE UID = ?1 AND LEAVE_MESSAGE_STATE = ?2", nativeQuery = true)
	Integer updateLeaveMessageState(String UID, String state);
}