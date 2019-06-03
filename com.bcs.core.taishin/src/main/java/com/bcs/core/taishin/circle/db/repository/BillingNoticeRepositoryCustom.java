package com.bcs.core.taishin.circle.db.repository;

import java.util.List;
import java.util.Set;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;

public interface BillingNoticeRepositoryCustom {

	public void updateStatus(String procApName, List<String> tempIds, Set<Long>  allMainIds, List<BillingNoticeDetail> allDetails);
	
	public void batchInsertBillingNoticeDetail(final List<BillingNoticeDetail> list);
}
