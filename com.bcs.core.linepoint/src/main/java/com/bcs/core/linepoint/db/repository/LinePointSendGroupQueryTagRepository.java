package com.bcs.core.linepoint.db.repository;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.linepoint.db.entity.LinePointSendGroupQueryTag;

public interface LinePointSendGroupQueryTagRepository extends EntityRepository<LinePointSendGroupQueryTag, Long>{

	@Transactional(readOnly = true, timeout = 30)
	public long countBySendGroupQueryQueryFieldIdAndQueryFieldTagValue(String queryFieldId, String queryFieldTagValue);
}
