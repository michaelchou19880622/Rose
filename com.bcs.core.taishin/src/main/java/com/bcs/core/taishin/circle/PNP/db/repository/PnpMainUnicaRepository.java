package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpMainUnica;

public interface PnpMainUnicaRepository extends EntityRepository<PnpMainUnica, Long>{
	 
//	@Modifying
//	@Query("update PnpMainUnica a set a.sendCount = a.sendCount +1 where a.pnpMainId = ?1")
//	@Transactional(rollbackFor=Exception.class, timeout = 30)
//	void increaseSendCountByPnpMainId(Long pnpMainId);
//	
//	@Modifying
//	@Query("update PnpMainUnica a set a.sendCount = a.sendCount +?2 where a.pnpMainId = ?1")
//	@Transactional(rollbackFor=Exception.class, timeout = 30)
//	void increaseSendCountByPnpMainId(Long pnpMainId, Long increase);
	
	@Transactional(readOnly = true, timeout = 30)
	public List<PnpMainUnica> findByStatus(String status);
	
//	@Query("select x from PnpMainUnica x where x.status in (?1) and x.tempId in (?2) ")
//	public List<BillingNoticeMain> findByStatusAndTemplatIds(List<String> status, List<String> tempIds);
	
	@Modifying
	@Query("update PnpMainUnica x set x.status = ?1 , x.modifyTime = ?2 where x.pnpMainId = ?3 ")
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	void updatePnpMainUnicaStatus(String status, Date modifyTime, Long pnpMainId);
	
	@Modifying
	@Query("update PnpMainUnica x set x.status = ?1 , x.procApName = ?2 , x.modifyTime = ?3  where x.pnpMainId in (?4)  ")
	@Transactional(rollbackFor = Exception.class, timeout = 30)
	public void updateStatusAndProcApName(String status, String procApName, Date modifyTime, List<Long> pnpMainId);
	
	
	
}
