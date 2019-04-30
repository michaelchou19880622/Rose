package com.bcs.core.bot.db.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgBotReceiveRepository extends EntityRepository<MsgBotReceive, String>, MsgBotReceiveRepositoryCustom{

	public Page<MsgBotReceive> findByUserStatus(String userStatus, Pageable pageable);
	
	@Query(value = "SELECT COUNT('x') FROM BCS_MSG_BOT_RECEIVE WHERE RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2", nativeQuery = true)
	public Long countReceive(String start, String end);

	@Query(value = "SELECT COUNT('x') AS ResponseCount, COUNT(distinct SOURCE_ID) AS ResponseDistinctCount FROM BCS_MSG_BOT_RECEIVE WHERE REFERENCE_ID = ?1 AND RECEIVE_DAY >= ?2 AND RECEIVE_DAY < ?3", nativeQuery = true)
	public List<Object[]> countReceiveByReferenceId(String referenceId, String start, String end);

	@Query(value = "SELECT COUNT('x') AS ResponseCount, COUNT(distinct SOURCE_ID) AS ResponseDistinctCount FROM BCS_MSG_BOT_RECEIVE WHERE REFERENCE_ID = ?1 AND RECEIVE_DAY >= ?2 AND RECEIVE_DAY < ?3 AND USER_STATUS = ?4", nativeQuery = true)
	public List<Object[]> countReceiveByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus);

	@Query(value = "SELECT distinct SOURCE_ID AS ResponseDistinctCount FROM BCS_MSG_BOT_RECEIVE WHERE REFERENCE_ID = ?1 AND RECEIVE_DAY >= ?2 AND RECEIVE_DAY < ?3 AND USER_STATUS = ?4", nativeQuery = true)
	public List<String> findReceiveMidByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus);

	@Query(value = "SELECT COUNT('x') FROM BCS_MSG_BOT_RECEIVE WHERE RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2 AND EVENT_TYPE = ?3", nativeQuery = true)
	public Long countReceiveByType(String start, String end, String eventType);
	
	@Query(value = "SELECT COUNT('x') FROM BCS_MSG_BOT_RECEIVE where REFERENCE_ID = ?1", nativeQuery = true)
	public Long countReferenceIdResponse(String referenceId);
	
	@Query(value = "SELECT COUNT(DISTINCT SOURCE_ID) FROM BCS_MSG_BOT_RECEIVE where REFERENCE_ID = ?1", nativeQuery = true)
	public Long countReferenceIdResponseDistinct(String referenceId);
	
	@Query(value = "SELECT DISTINCT REFERENCE_ID FROM BCS_MSG_BOT_RECEIVE WHERE EVENT_TYPE = 'message' AND SOURCE_TYPE = 'user' AND RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2", nativeQuery = true)
    public List<String> findReferenceId(String start, String end);
	
	@Query(value = "SELECT * FROM BCS_MSG_BOT_RECEIVE WHERE EVENT_TYPE = 'message' AND SOURCE_TYPE = 'user' AND RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2", nativeQuery = true)
	public List<MsgBotReceive> findByReceiveDay(String start, String end);
}
