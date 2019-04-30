package com.bcs.core.taishin.circle.PNP.db.repository;

//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.bcs.core.db.persistence.EntityRepository;
//import com.bcs.core.taishin.circle.PNP.db.entity.PnpMain;
//
//public interface PnpMainRepository extends EntityRepository<PnpMain, Long>, PnpMainRepositoryCustom{
//	
//	@Modifying
//	@Query("update PnpMain a set a.sendCount = a.sendCount +1 where a.pnpMainId = ?1")
//	@Transactional(rollbackFor=Exception.class, timeout = 30)
//	void increaseSendCountByPnpMainId(Long pnpMainId);
//	
//	@Modifying
//	@Query("update PnpMain a set a.sendCount = a.sendCount +?2 where a.pnpMainId = ?1")
//	@Transactional(rollbackFor=Exception.class, timeout = 30)
//	void increaseSendCountByPnpMainId(Long pnpMainId, Long increase);
//}
