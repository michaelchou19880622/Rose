package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SendGroupDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface SendGroupDetailRepository extends EntityRepository<SendGroupDetail, Long>{

	@Transactional(readOnly = true, timeout = 30)
	public List<SendGroupDetail> findBySendGroupGroupId(Long groupId);
}
