package com.bcs.core.bot.db.repository;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.db.persistence.EntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Date;

/**
 * The interface Msg bot receive repository.
 *
 * @author ???
 */
@Repository
public interface MsgBotReceiveRepository extends EntityRepository<MsgBotReceive, String> {

    /**
     * Find by user status page.
     *
     * @param userStatus the user status
     * @param pageable   the pageable
     * @return the page
     */
    Page<MsgBotReceive> findByUserStatus(String userStatus, Pageable pageable);

    /**
     * Count receive long.
     *
     * @param start the start
     * @param end   the end
     * @return the long
     */
    @Query(value = "SELECT COUNT('x') FROM BCS_MSG_BOT_RECEIVE WHERE RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2", nativeQuery = true)
    Long countReceive(String start, String end);

    /**
     * Count receive by reference id list.
     *
     * @param referenceId the reference id
     * @param start       the start
     * @param end         the end
     * @return the list
     */
    @Query(value = "SELECT COUNT('x') AS ResponseCount, COUNT(distinct SOURCE_ID) AS ResponseDistinctCount FROM BCS_MSG_BOT_RECEIVE WHERE REFERENCE_ID = ?1 AND RECEIVE_DAY >= ?2 AND RECEIVE_DAY < ?3", nativeQuery = true)
    List<Object[]> countReceiveByReferenceId(String referenceId, String start, String end);

    /**
     * Count receive by reference id and status list.
     *
     * @param referenceId the reference id
     * @param start       the start
     * @param end         the end
     * @param userStatus  the user status
     * @return the list
     */
    @Query(value = "SELECT COUNT('x') AS ResponseCount, COUNT(distinct SOURCE_ID) AS ResponseDistinctCount FROM BCS_MSG_BOT_RECEIVE WHERE REFERENCE_ID = ?1 AND RECEIVE_DAY >= ?2 AND RECEIVE_DAY < ?3 AND USER_STATUS = ?4", nativeQuery = true)
    List<Object[]> countReceiveByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus);

    /**
     * Find receive mid by reference id and status list.
     *
     * @param referenceId the reference id
     * @param start       the start
     * @param end         the end
     * @param userStatus  the user status
     * @return the list
     */
    @Query(value = "SELECT distinct SOURCE_ID AS ResponseDistinctCount FROM BCS_MSG_BOT_RECEIVE WHERE REFERENCE_ID = ?1 AND RECEIVE_DAY >= ?2 AND RECEIVE_DAY < ?3 AND USER_STATUS = ?4", nativeQuery = true)
    List<String> findReceiveMidByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus);

    /**
     * Count receive by type long.
     *
     * @param start     the start
     * @param end       the end
     * @param eventType the event type
     * @return the long
     */
    @Query(value = "SELECT COUNT('x') FROM BCS_MSG_BOT_RECEIVE WHERE RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2 AND EVENT_TYPE = ?3", nativeQuery = true)
    Long countReceiveByType(String start, String end, String eventType);

    /**
     * Count reference id response long.
     *
     * @param referenceId the reference id
     * @return the long
     */
    @Query(value = "SELECT COUNT('x') FROM BCS_MSG_BOT_RECEIVE where REFERENCE_ID = ?1", nativeQuery = true)
    Long countReferenceIdResponse(String referenceId);

    /**
     * Count reference id response distinct long.
     *
     * @param referenceId the reference id
     * @return the long
     */
    @Query(value = "SELECT COUNT(DISTINCT SOURCE_ID) FROM BCS_MSG_BOT_RECEIVE where REFERENCE_ID = ?1", nativeQuery = true)
    Long countReferenceIdResponseDistinct(String referenceId);

    /**
     * Find reference id list.
     *
     * @param start the start
     * @param end   the end
     * @return the list
     */
    @Query(value = "SELECT DISTINCT REFERENCE_ID FROM BCS_MSG_BOT_RECEIVE WHERE EVENT_TYPE = 'message' AND SOURCE_TYPE = 'user' AND RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2", nativeQuery = true)
    List<String> findReferenceId(String start, String end);

    /**
     * Find by receive day list.
     *
     * @param start the start
     * @param end   the end
     * @return the list
     */
    @Query(value = "SELECT * FROM BCS_MSG_BOT_RECEIVE WHERE EVENT_TYPE = 'message' AND SOURCE_TYPE = 'user' AND RECEIVE_DAY >= ?1 AND RECEIVE_DAY < ?2", nativeQuery = true)
    List<MsgBotReceive> findByReceiveDay(String start, String end);
    
    
    /**
     * 更新PNP Mitake Detail Table 狀態
     * 狀態更新為 PNP_COMPLETE並更新接收時間
     *
     * @param sendTime    Send Time
     * @param detailId    Detail Id
     */
	@Transactional(isolation=Isolation.REPEATABLE_READ , rollbackFor = Exception.class, timeout = 30)
	@Modifying(flushAutomatically = true)
	@Query(value = "update BCS_PNP_DETAIL_MITAKE set PNP_STATUS = 'PNP_COMPLETE' , SEND_TIME = ?1 , MODIFY_TIME = ?1, PNP_DELIVERY_TIME = ?1 WHERE PNP_DETAIL_ID = ?2", nativeQuery = true)            
	void updatePnpMitakeDetailStatus( Date sendTime, String detailId);    

    /**
     * 更新PNP Ming Detail Table 狀態
     * 狀態更新為 PNP_COMPLETE並更新接收時間
     *
     * @param sendTime    Send Time
     * @param detailId    Detail Id
     */
	@Transactional(isolation=Isolation.REPEATABLE_READ , rollbackFor = Exception.class, timeout = 30)
	@Modifying(flushAutomatically = true)
	@Query(value = "update BCS_PNP_DETAIL_MING set PNP_STATUS = 'PNP_COMPLETE' , SEND_TIME = ?1 , MODIFY_TIME = ?1, PNP_DELIVERY_TIME = ?1 WHERE PNP_DETAIL_ID = ?2", nativeQuery = true)            
	void updatePnpMingDetailStatus( Date sendTime, String detailId);    

    /**
     * 更新PNP EVERY8D Detail Table 狀態
     * 狀態更新為 PNP_COMPLETE並更新接收時間
     *
     * @param sendTime    Send Time
     * @param detailId    Detail Id
     */
	@Transactional(isolation=Isolation.REPEATABLE_READ , rollbackFor = Exception.class, timeout = 30)
	@Modifying(flushAutomatically = true)
	@Query(value = "update BCS_PNP_DETAIL_EVERY8D set PNP_STATUS = 'PNP_COMPLETE' , SEND_TIME = ?1 , MODIFY_TIME = ?1, PNP_DELIVERY_TIME = ?1 WHERE PNP_DETAIL_ID = ?2", nativeQuery = true)            
	void updatePnpEvery8DDetailStatus( Date sendTime, String detailId);    

    /**
     * 更新PNP UNICA Detail Table 狀態
     * 狀態更新為 PNP_COMPLETE並更新接收時間
     *
     * @param sendTime    Send Time
     * @param detailId    Detail Id
     */
	@Transactional(isolation=Isolation.REPEATABLE_READ , rollbackFor = Exception.class, timeout = 30)
	@Modifying(flushAutomatically = true)
	@Query(value = "update BCS_PNP_DETAIL_UNICA set PNP_STATUS = 'PNP_COMPLETE' , SEND_TIME = ?1 , MODIFY_TIME = ?1, PNP_DELIVERY_TIME = ?1 WHERE PNP_DETAIL_ID = ?2", nativeQuery = true)            
	void updatePnpUnicaDetailStatus( Date sendTime, String detailId);    

}
