package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgMainRepository extends EntityRepository<MsgMain, Long>{

	@Transactional(readOnly = true, timeout = 30)
	public List<MsgMain> findByStatus(String status);
}
