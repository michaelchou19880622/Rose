package com.bcs.core.db.repository;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgInteractiveCampaign;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgInteractiveCampaignRepository extends EntityRepository<MsgInteractiveCampaign, Long>{

	@Transactional(readOnly = true, timeout = 30)
	MsgInteractiveCampaign findByiMsgId(Long iMsgId);
}
