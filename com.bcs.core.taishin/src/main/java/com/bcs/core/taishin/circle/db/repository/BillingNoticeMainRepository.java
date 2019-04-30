package com.bcs.core.taishin.circle.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeMain;

public interface BillingNoticeMainRepository extends EntityRepository<BillingNoticeMain, Long>{

	@Transactional(readOnly = true, timeout = 30)
	public List<BillingNoticeMain> findByStatus(String status);
	
	@Query("select x from BillingNoticeMain x where x.status in (?1) and x.tempId in (?2) ")
	public List<BillingNoticeMain> findByStatusAndTemplatIds(List<String> status, List<String> tempIds);
	
	@Modifying
	@Query("update BillingNoticeMain x set x.status = ?1 , x.modifyTime = ?2 where x.noticeMainId = ?3 ")
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	void updateBillingNoticeMainStatus(String status, Date modifyTime, Long noticeMainId);
	
	@Modifying
	@Query("update BillingNoticeMain x set x.status = ?1 , x.procApName = ?2 , x.modifyTime = ?3  where x.noticeMainId in (?4)  ")
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	public void updateStatusAndProcApName(String status, String procApName, Date modifyTime, List<Long> noticeMainId);
	
}
