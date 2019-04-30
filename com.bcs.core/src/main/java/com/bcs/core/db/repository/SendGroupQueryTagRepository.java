package com.bcs.core.db.repository;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SendGroupQueryTag;
import com.bcs.core.db.persistence.EntityRepository;

public interface SendGroupQueryTagRepository extends EntityRepository<SendGroupQueryTag, Long>{

	@Transactional(readOnly = true, timeout = 30)
	public long countBySendGroupQueryQueryFieldIdAndQueryFieldTagValue(String queryFieldId, String queryFieldTagValue);
}
