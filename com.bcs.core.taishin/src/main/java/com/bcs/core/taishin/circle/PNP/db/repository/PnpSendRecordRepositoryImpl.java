package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.db.repository.EntityManagerControl;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpSendRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class PnpSendRecordRepositoryImpl implements PnpSendRecordRepositoryCustom {

    @Autowired
    private EntityManagerControl entityManagerControl;

    @Override
    public void bulkPersist(List<PnpSendRecord> pnpSendRecords) {

        if (CollectionUtils.isEmpty(pnpSendRecords)) {
            return;
        }

        for (PnpSendRecord pnpSendRecord : pnpSendRecords) {
            entityManagerControl.persist(pnpSendRecord);
        }
    }

    @Override
    public void bulkPersist(PnpSendRecord pnpSendRecord) {
        entityManagerControl.persist(pnpSendRecord);
    }
}
