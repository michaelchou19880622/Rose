package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ContentTemplateMsg;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentTemplateMsgRepository extends EntityRepository<ContentTemplateMsg, String>{
	@Query(value = "SELECT * FROM BCS_CONTENT_TEMPLATE "
			+ "WHERE (TEMPLATE_ID = ?1 OR TEMPLATE_PARENT_ID = ?1) AND (BCS_CONTENT_TEMPLATE.STATUS <> 'DELETE' OR BCS_CONTENT_TEMPLATE.STATUS IS NULL) "
			+ "ORDER BY BCS_CONTENT_TEMPLATE.TEMPLATE_LETTER"
			, nativeQuery = true)
	public List<ContentTemplateMsg> findMainAndColumnByTemplateId(String templateId);
}
