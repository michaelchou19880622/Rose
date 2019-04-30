package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgApiSendRecord;
import com.bcs.core.db.persistence.EntityRepository;

public interface MsgApiSendRecordRepository extends EntityRepository<MsgApiSendRecord, String>, MsgApiSendRecordRepositoryCustom{

	@Transactional(readOnly = true, timeout = 30)
	public List<MsgApiSendRecord> findByMsgSource(String msgSource);
}
