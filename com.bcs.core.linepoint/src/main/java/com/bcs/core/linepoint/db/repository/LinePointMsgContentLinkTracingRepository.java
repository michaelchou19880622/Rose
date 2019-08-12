package com.bcs.core.linepoint.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.linepoint.db.entity.LinePointMsgContentLinkTracing;
import com.bcs.core.db.persistence.EntityRepository;


public interface LinePointMsgContentLinkTracingRepository extends EntityRepository<LinePointMsgContentLinkTracing, Long>{
	
	@Transactional(readOnly = true, timeout = 30)
	public List<LinePointMsgContentLinkTracing> findByLinkId(String linkId);
}
