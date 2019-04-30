package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgSendRecord;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgSendRecordRepository extends EntityRepository<MsgSendRecord, Long>, MsgSendRecordRepositoryCustom{

	@Transactional(readOnly = true, timeout = 30)
	public List<MsgSendRecord> findByMsgSendId(Long msgSendId);
}
