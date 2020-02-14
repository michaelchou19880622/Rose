package com.bcs.core.taishin.circle.db.repository;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BillingNoticeRepositoryCustom {
    /**
     * Update Status
     *
     * @param procApName procApName
     * @param tempIds    tempIds
     * @return Object[]{mainIds, detailList)};
     */
    Object[] updateStatus(String procApName, List<String> tempIds);

    @Transactional(rollbackFor = Exception.class)
    void restoreNotSendDetail();
}
