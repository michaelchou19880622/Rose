package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpSendRecord;


public interface PnpSendRecordRepositoryCustom{
	
	public void bulkPersist(List<PnpSendRecord> pnpSendRecords);
	
	public void bulkPersist(PnpSendRecord pnpSendRecord);
}
