package com.bcs.core.taishin.circle.pnp.service;

import com.bcs.core.taishin.circle.pnp.db.entity.PnpSendRecord;
import com.bcs.core.taishin.circle.pnp.db.repository.PnpSendRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PnpSendRecordService {
    @Autowired
    private PnpSendRecordRepository pnpSendRecordRepository;

    public void save(PnpSendRecord pnpSend) {
        pnpSendRecordRepository.save(pnpSend);
    }

    public List<PnpSendRecord> findByPnpMainId(Long pnpMainId) {
        return pnpSendRecordRepository.findByPnpMainId(pnpMainId);
    }

    public void bulkPersist(PnpSendRecord pnpSend) {
        pnpSendRecordRepository.bulkPersist(pnpSend);
    }

    public List<Object[]> findReportByPnpMainId(Long pnpMainId) {
        return pnpSendRecordRepository.findReportByPnpMainId(pnpMainId);
    }
}
