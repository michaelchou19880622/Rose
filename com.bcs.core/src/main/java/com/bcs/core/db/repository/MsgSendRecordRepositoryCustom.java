package com.bcs.core.db.repository;

import java.util.List;

import com.bcs.core.db.entity.MsgSendRecord;

public interface MsgSendRecordRepositoryCustom{
	
	public void bulkPersist(List<MsgSendRecord> msgSendRecords);
	
	public void bulkPersist(MsgSendRecord msgSendRecord);
}
