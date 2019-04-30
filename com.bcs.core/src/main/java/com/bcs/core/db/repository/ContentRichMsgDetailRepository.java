package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentRichMsgDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentRichMsgDetailRepository extends EntityRepository<ContentRichMsgDetail, String>{
	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT * FROM BCS_CONTENT_RICH_DETAIL "
			+ "WHERE RICH_ID = ?1 AND (BCS_CONTENT_RICH_DETAIL.STATUS <> 'DELETE' OR BCS_CONTENT_RICH_DETAIL.STATUS IS NULL)  "
			+ "ORDER BY START_POINT_X", nativeQuery = true)
	public List<ContentRichMsgDetail> findByRichId(String richId);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT RICH_ID FROM BCS_CONTENT_RICH_DETAIL WHERE LINK_ID = ?1 AND (BCS_CONTENT_RICH_DETAIL.STATUS <> 'DELETE' OR BCS_CONTENT_RICH_DETAIL.STATUS IS NULL) ", nativeQuery = true)
	public List<String> getRichIdByLinkId(String linkId);
}
