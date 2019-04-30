package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ContentTemplateMsgAction;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentTemplateMsgActionRepository  extends EntityRepository<ContentTemplateMsgAction, String>{
	@Query(value = "SELECT * FROM BCS_CONTENT_TEMPLATE_ACTION "
			+ "WHERE TEMPLATE_ID = ?1 AND (BCS_CONTENT_TEMPLATE_ACTION.STATUS <> 'DELETE' OR BCS_CONTENT_TEMPLATE_ACTION.STATUS IS NULL) "
			+ "ORDER BY BCS_CONTENT_TEMPLATE_ACTION.ACTION_LETTER"
			, nativeQuery = true)
	public List<ContentTemplateMsgAction> findByTemplateId(String templateId);
}
