package com.bcs.core.taishin.circle.pnp.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.pnp.db.entity.PnpSendRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PnpSendRecordRepository extends EntityRepository<PnpSendRecord, Long>, PnpSendRecordRepositoryCustom {

    @Transactional(readOnly = true, timeout = 30)
    public List<PnpSendRecord> findByPnpMainId(Long pnpMainId);

    @Query(value = "select a.PHONE, "
            + "a.SEND_TIME, "
            + "case when a.RECORD = '200-' and b.UID is null then 'BLOCK' else a.RECORD end as RECORD "
            + "from BCS_PNP_SEND_RECORD a "
            + "left join BCS_PNP_DELIVERY_RECORD b on a.PNP_DETAIL_ID = b.PNP_DETAIL_ID "
            + "where a.PNP_MAIN_ID = ?1", nativeQuery = true)
    List<Object[]> findReportByPnpMainId(Long pnpMainId);
}
