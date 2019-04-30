package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgInteractiveDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgInteractiveDetailRepository extends EntityRepository<MsgInteractiveDetail, Long>{

	@Transactional(readOnly = true, timeout = 30)
	List<MsgInteractiveDetail> findByiMsgId(Long iMsgId);
}
