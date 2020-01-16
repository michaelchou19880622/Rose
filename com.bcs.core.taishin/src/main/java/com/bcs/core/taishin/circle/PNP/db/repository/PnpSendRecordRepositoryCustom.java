package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.taishin.circle.PNP.db.entity.PnpSendRecord;

import java.util.List;


public interface PnpSendRecordRepositoryCustom {

    public void bulkPersist(List<PnpSendRecord> pnpSendRecords);

    public void bulkPersist(PnpSendRecord pnpSendRecord);
}
