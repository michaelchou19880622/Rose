package com.bcs.core.taishin.circle.db.repository;

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
}
