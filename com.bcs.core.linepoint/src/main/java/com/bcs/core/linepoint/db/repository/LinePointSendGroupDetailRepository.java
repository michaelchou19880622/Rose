package com.bcs.core.linepoint.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.linepoint.db.entity.LinePointSendGroupDetail;

public interface LinePointSendGroupDetailRepository extends EntityRepository<LinePointSendGroupDetail, Long>{
	@Transactional(readOnly = true, timeout = 30)
	public List<LinePointSendGroupDetail> findBySendGroupGroupId(Long groupId);
}
