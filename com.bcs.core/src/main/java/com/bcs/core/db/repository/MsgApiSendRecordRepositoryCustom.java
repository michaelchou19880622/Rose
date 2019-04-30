package com.bcs.core.db.repository;

import java.util.List;

import com.bcs.core.db.entity.MsgApiSendRecord;

public interface MsgApiSendRecordRepositoryCustom{
	
	public void bulkPersist(List<MsgApiSendRecord> msgApiSendRecords);
	
	public void bulkPersist(MsgApiSendRecord msgApiSendRecord);
}
