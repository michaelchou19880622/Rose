package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpDeliveryRecord;

import java.util.List;


public interface PnpDeliveryRecordRepositoryCustom {

    public void bulkPersist(List<PnpDeliveryRecord> delivery);

    public void bulkPersist(PnpDeliveryRecord delivery);
}
