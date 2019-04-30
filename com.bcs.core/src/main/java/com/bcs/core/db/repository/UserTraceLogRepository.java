package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.persistence.EntityRepository;

public interface UserTraceLogRepository extends EntityRepository<UserTraceLog, Long>, UserTraceLogRepositoryCustom{

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserTraceLog> findByTargetAndAction(String target, String action, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserTraceLog> findByReferenceId(String referenceId, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserTraceLog> findByLevel(String level, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserTraceLog> findByModifyUser(String modifyUser, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT(DISTINCT MID) "
			+ "FROM BCS_USER_TRACE_LOG, BCS_LINE_USER "
			+ "WHERE ACTION = 'Binded2Block' AND MID = MODIFY_USER AND STATUS = 'BLOCK'", nativeQuery = true)
	public Long countBinded2Block();

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT(DISTINCT MID) "
			+ "FROM BCS_USER_TRACE_LOG, BCS_LINE_USER "
			+ "WHERE ACTION = 'Binded2Block' AND MID = MODIFY_USER AND STATUS = 'BLOCK' AND BCS_LINE_USER.MODIFY_TIME >= ?1 AND BCS_LINE_USER.MODIFY_TIME < ?2", nativeQuery = true)
	public Long countBinded2Block(String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT('x') "
			+ "FROM BCS_USER_TRACE_LOG "
			+ "WHERE LEVEL = ?1 AND MODIFY_DAY >= ?2 AND MODIFY_DAY < ?3", nativeQuery = true)
	public Long countByLevel(String level, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT('x') "
			+ "FROM BCS_USER_TRACE_LOG "
			+ "WHERE TARGET = ?1 AND ACTION = ?2 AND MODIFY_DAY >= ?3 AND MODIFY_DAY < ?4", nativeQuery = true)
	public Long countByTargetAndAction(String target, String action, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT('x') "
			+ "FROM BCS_USER_TRACE_LOG "
			+ "WHERE MODIFY_DAY >= ?1 AND MODIFY_DAY < ?2", nativeQuery = true)
	public Long countAll(String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT('x') AS allCount, COUNT(distinct BCS_USER_TRACE_LOG.MODIFY_USER) AS allDistinctCount "
			+ "FROM BCS_USER_TRACE_LOG "
			+ "WHERE TARGET = ?1 AND ACTION = ?2 AND REFERENCE_ID = ?3 AND BCS_USER_TRACE_LOG.MODIFY_DAY >= ?4 AND BCS_USER_TRACE_LOG.MODIFY_DAY < ?5  ", nativeQuery = true)
	public List<Object[]> countByReferenceIdAndTime(String target, String action, String referenceId, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT DISTINCT MODIFY_USER "
			+ "FROM BCS_USER_TRACE_LOG "
			+ "WHERE REFERENCE_ID = ?1", nativeQuery = true)
	public List<String> findMidByReferenceId(String referenceId);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT DISTINCT MODIFY_USER "
			+ "FROM BCS_USER_TRACE_LOG "
			+ "WHERE REFERENCE_ID = ?1 AND BCS_USER_TRACE_LOG.MODIFY_DAY >= ?2", nativeQuery = true)
	public List<String> findMidByReferenceId(String referenceId, String start);
	
	@Transactional(readOnly = true, timeout = 30)
	@Query(value="SELECT * FROM BCS_USER_TRACE_LOG WHERE MODIFY_USER= ?1 AND ACTION= ?2 ORDER BY MODIFY_TIME DESC", nativeQuery = true)
	public List<UserTraceLog> findByModifyUserAndAction(String modifyUser, String action);
	
	@Transactional(readOnly = true, timeout = 30)
	@Query(value="SELECT * FROM BCS_USER_TRACE_LOG WHERE TARGET= ?1 AND ACTION= ?2 AND MODIFY_DAY >= ?3 AND MODIFY_DAY < ?4", nativeQuery = true)
    public List<UserTraceLog> findByTargetAndAction(String target, String action, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "SELECT COUNT(DISTINCT MID) "
            + "FROM BCS_USER_TRACE_LOG, BCS_LINE_USER "
            + "WHERE ACTION = 'Block' AND MID = MODIFY_USER AND STATUS = 'BLOCK' AND BCS_USER_TRACE_LOG.MODIFY_TIME >= ?1 AND BCS_USER_TRACE_LOG.MODIFY_TIME < ?2", nativeQuery = true)
    public Long countBlock(String start, String end);
	
	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "SELECT COUNT(DISTINCT MID) "
            + "FROM BCS_USER_TRACE_LOG, BCS_LINE_USER "
            + "WHERE ACTION = 'Binded2Block' AND MID = MODIFY_USER AND STATUS = 'BLOCK' AND BCS_USER_TRACE_LOG.MODIFY_TIME >= ?1 AND BCS_USER_TRACE_LOG.MODIFY_TIME < ?2", nativeQuery = true)
    public Long countBinded2Block2(String start, String end);
}
