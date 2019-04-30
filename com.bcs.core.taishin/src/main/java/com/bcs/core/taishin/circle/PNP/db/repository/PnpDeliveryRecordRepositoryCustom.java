package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDeliveryRecord;


public interface PnpDeliveryRecordRepositoryCustom{
	
	public void bulkPersist(List<PnpDeliveryRecord> delivery);
	
	public void bulkPersist(PnpDeliveryRecord delivery);
}
