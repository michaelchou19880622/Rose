package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentLinkTracing;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentLinkTracingRepository extends EntityRepository<ContentLinkTracing, Long>{
	@Transactional(readOnly = true, timeout = 60)
	@Query(value = "SELECT * FROM (" +
	               "SELECT" + 
			       "    bclt.TRACING_ID," +
			       "	bclt.LINK_ID," +
			       "    bclt.LINK_ID_BINDED," +
			       "	bclt.LINK_ID_UNMOBILE," +
			       "	bclt.MODIFY_TIME," +
			       "	(CASE WHEN bau.USER_NAME IS NULL THEN bclt.MODIFY_USER ELSE bau.USER_NAME END) AS MODIFY_USER," +
			       "    ROW_NUMBER() OVER (ORDER BY bclt.TRACING_ID DESC) AS ROW_ID " +
			       "FROM BCS_CONTENT_LINK_TRACING bclt " +
			       "LEFT JOIN BCS_ADMIN_USER bau ON bclt.MODIFY_USER = bau.USER_NAME " +
			       "WHERE bclt.LINK_ID IN (SELECT REFERENCE_ID FROM BCS_CONTENT_FLAG bcf WHERE CONTENT_TYPE='LINK' AND FLAG_VALUE LIKE ?3) " +
			       "OR bclt.LINK_ID_BINDED IN (SELECT REFERENCE_ID FROM BCS_CONTENT_FLAG bcf WHERE CONTENT_TYPE='LINK' AND FLAG_VALUE LIKE ?3) " +
			       "OR bclt.LINK_ID_UNMOBILE IN (SELECT REFERENCE_ID FROM BCS_CONTENT_FLAG bcf WHERE CONTENT_TYPE='LINK' AND FLAG_VALUE LIKE ?3) " +
			       ") AS NewTable " +
			       "WHERE ROW_ID >= ?1 AND ROW_ID < (?1 + ?2) " +
	               "ORDER BY TRACING_ID DESC", nativeQuery = true)
	public List<Object[]> findListByFlag(int offset, int recordNum, String flag);
	
	@Transactional(readOnly = true, timeout = 60)
	@Query(value = "SELECT * FROM (" +
	               "SELECT" + 
			       "    bclt.TRACING_ID," +
			       "	bclt.LINK_ID," +
			       "    bclt.LINK_ID_BINDED," +
			       "	bclt.LINK_ID_UNMOBILE," +
			       "	bclt.MODIFY_TIME," +
			       "	(CASE WHEN bau.USER_NAME IS NULL THEN bclt.MODIFY_USER ELSE bau.USER_NAME END) AS MODIFY_USER," +
			       "    ROW_NUMBER() OVER (ORDER BY bclt.TRACING_ID DESC) AS ROW_ID " +
			       "FROM BCS_CONTENT_LINK_TRACING bclt " +
			       "LEFT JOIN BCS_ADMIN_USER bau ON bclt.MODIFY_USER = bau.USER_NAME " +
			       ") AS NewTable " +
			       "WHERE ROW_ID >= ?1 AND ROW_ID < (?1 + ?2) " +
	               "ORDER BY TRACING_ID DESC", nativeQuery = true)
	public List<Object[]> findListByPageNo(int offset, int recordNum);
}
