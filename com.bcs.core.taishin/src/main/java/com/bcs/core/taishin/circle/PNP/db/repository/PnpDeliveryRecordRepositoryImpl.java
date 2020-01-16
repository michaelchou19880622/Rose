package com.bcs.core.taishin.circle.PNP.db.repository;

import com.bcs.core.db.repository.EntityManagerControl;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDeliveryRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PnpDeliveryRecordRepositoryImpl implements PnpDeliveryRecordRepositoryCustom {

    @Autowired
    private EntityManagerControl entityManagerControl;

    @Override
    public void bulkPersist(List<PnpDeliveryRecord> deliverys) {

        if (CollectionUtils.isEmpty(deliverys)) {
            return;
        }

        for (PnpDeliveryRecord delivery : deliverys) {
            entityManagerControl.persist(delivery);
        }
    }

    @Override
    public void bulkPersist(PnpDeliveryRecord delivery) {
        entityManagerControl.persist(delivery);
    }
}
