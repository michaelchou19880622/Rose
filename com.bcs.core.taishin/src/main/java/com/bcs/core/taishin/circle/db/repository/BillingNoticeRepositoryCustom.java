package com.bcs.core.taishin.circle.db.repository;

import java.util.List;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;

public interface BillingNoticeRepositoryCustom {

	public List<BillingNoticeDetail> findDetailByStatusForUpdate(List<String> status, Long mainId);
	public BillingNoticeDetail findFirstDetailByStatusForUpdate(String status, List<String> tempIds);
	
	public BillingNoticeMain findFirstMainByStatusForUpdate(String status, List<String> tempIds);
}
