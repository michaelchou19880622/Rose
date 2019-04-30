package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentLinkRepository extends EntityRepository<ContentLink, String>{
	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT LINK_URL, LINK_TITLE, LINK_ID, MODIFY_TIME "
			+ "FROM BCS_CONTENT_LINK "
			+ "WHERE LINK_URL IS NOT NULL AND LINK_URL != '' "
			+ "ORDER BY MODIFY_TIME DESC, LINK_URL ", nativeQuery = true)
	public List<Object[]> findAllLinkUrl();

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT LINK_URL, LINK_TITLE, LINK_ID, MODIFY_TIME "
			+ "FROM BCS_CONTENT_LINK, BCS_CONTENT_FLAG "
			+ "WHERE CONTENT_TYPE = 'LINK' AND FLAG_VALUE = ?1 AND LINK_ID = REFERENCE_ID AND LINK_URL IS NOT NULL AND LINK_URL != '' "
			+ "ORDER BY MODIFY_TIME DESC, LINK_URL ", nativeQuery = true)
	public List<Object[]> findAllLinkUrlByFlag(String flag);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT LINK_URL, LINK_TITLE, LINK_ID, MODIFY_TIME "
			+ "FROM BCS_CONTENT_LINK, BCS_CONTENT_FLAG "
			+ "WHERE CONTENT_TYPE = 'LINK' AND FLAG_VALUE LIKE ?1 AND LINK_ID = REFERENCE_ID AND LINK_URL IS NOT NULL AND LINK_URL != '' "
			+ "ORDER BY MODIFY_TIME DESC, LINK_URL ", nativeQuery = true)
	public List<Object[]> findAllLinkUrlByLikeFlag(String flag);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT LINK_URL, LINK_TITLE, LINK_ID, MODIFY_TIME "
			+ "FROM BCS_CONTENT_LINK "
			+ "WHERE LINK_TITLE LIKE ?1 AND LINK_URL IS NOT NULL AND LINK_URL != '' "
			+ "ORDER BY MODIFY_TIME DESC, LINK_URL ", nativeQuery = true)
	public List<Object[]> findAllLinkUrlByLikeTitle(String title);
	
	@Transactional(readOnly = true, timeout = 30)
	public List<ContentLink> findByLinkUrl(String linkUrl);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x from ContentLink x where x.linkId in (?1) ")
//	@Query("select x from ContentLink x where x.linkId in ?1 ") // MYSQL Difference
	public List<ContentLink> findByLinkIdIn(List<String> linkIds);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT "
			+ "           COUNT('x') AS allCount, "
			+ "           COUNT(distinct BCS_USER_TRACE_LOG.MODIFY_USER) AS allDistinctCount "
			+ "FROM BCS_CONTENT_LINK, BCS_USER_TRACE_LOG "
			+ "WHERE LINK_ID = REFERENCE_ID AND ACTION = 'ClickLink' AND LINK_URL = ?1 AND BCS_USER_TRACE_LOG.MODIFY_DAY >= ?2 AND BCS_USER_TRACE_LOG.MODIFY_DAY < ?3  ", nativeQuery = true)
	public List<Object[]> countClickCountByLinkUrlAndTime(String linkUrl, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT "
			+ "      MODIFY_DAY AS Day, "
			+ "      COUNT('x') AS allCount, "
			+ "      COUNT(distinct BCS_USER_TRACE_LOG.MODIFY_USER) AS allDistinctCount "
			+ "FROM BCS_CONTENT_LINK, BCS_USER_TRACE_LOG "
			+ "WHERE LINK_ID = REFERENCE_ID AND ACTION = 'ClickLink' AND LINK_URL = ?1 "
			+ "group by MODIFY_DAY ", nativeQuery = true)
	public List<Object[]> countClickCountByLinkUrl(String linkUrl);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT "
			+ "      MODIFY_DAY AS Day, "
			+ "      COUNT('x') AS allCount, "
			+ "      COUNT(distinct BCS_USER_TRACE_LOG.MODIFY_USER) AS allDistinctCount "
			+ "FROM BCS_CONTENT_LINK, BCS_USER_TRACE_LOG "
			+ "WHERE LINK_ID = REFERENCE_ID AND ACTION = 'ClickLink' AND LINK_URL = ?1  AND BCS_USER_TRACE_LOG.MODIFY_DAY >= ?2 "
			+ "group by MODIFY_DAY ", nativeQuery = true)
	public List<Object[]> countClickCountByLinkUrl(String linkUrl, String start);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT "
			+ "          COUNT('x') AS allCount, "
			+ "          COUNT(distinct BCS_USER_TRACE_LOG.MODIFY_USER) AS allDistinctCount "
			+ "FROM BCS_CONTENT_LINK, BCS_USER_TRACE_LOG "
			+ "WHERE LINK_ID = REFERENCE_ID AND ACTION = 'ClickLink' AND LINK_ID = ?1 AND BCS_USER_TRACE_LOG.MODIFY_DAY >= ?2 AND BCS_USER_TRACE_LOG.MODIFY_DAY < ?3  ", nativeQuery = true)
	public List<Object[]> countClickCountByLinkIdAndTime(String linkId, String start, String end);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT "
			+ "      MODIFY_DAY AS Day, "
			+ "      COUNT('x') AS allCount, "
			+ "      COUNT(distinct BCS_USER_TRACE_LOG.MODIFY_USER) AS allDistinctCount "
			+ "FROM BCS_CONTENT_LINK, BCS_USER_TRACE_LOG "
			+ "WHERE LINK_ID = REFERENCE_ID AND ACTION = 'ClickLink' AND LINK_ID = ?1 "
			+ "group by MODIFY_DAY ", nativeQuery = true)
	public List<Object[]> countClickCountByLinkId(String LinkId);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT "
			+ "      MODIFY_DAY AS Day, "
			+ "      COUNT('x') AS allCount, "
			+ "      COUNT(distinct BCS_USER_TRACE_LOG.MODIFY_USER) AS allDistinctCount "
			+ "FROM BCS_CONTENT_LINK, BCS_USER_TRACE_LOG "
			+ "WHERE LINK_ID = REFERENCE_ID AND ACTION = 'ClickLink' AND LINK_ID = ?1  AND BCS_USER_TRACE_LOG.MODIFY_DAY >= ?2 "
			+ "group by MODIFY_DAY ", nativeQuery = true)
	public List<Object[]> countClickCountByLinkId(String LinkId, String start);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT distinct BCS_USER_TRACE_LOG.MODIFY_USER "
			+ "FROM BCS_CONTENT_LINK, BCS_USER_TRACE_LOG "
			+ "WHERE LINK_ID = REFERENCE_ID AND ACTION = 'ClickLink' AND LINK_URL = ?1 AND BCS_USER_TRACE_LOG.MODIFY_DAY >= ?2 AND BCS_USER_TRACE_LOG.MODIFY_DAY < ?3  ", nativeQuery = true)
	public List<String> findClickMidByLinkUrlAndTime(String linkUrl, String start, String end);
}
