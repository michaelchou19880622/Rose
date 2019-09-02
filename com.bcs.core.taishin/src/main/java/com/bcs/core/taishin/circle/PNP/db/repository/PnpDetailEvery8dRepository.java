package com.bcs.core.taishin.circle.PNP.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetailEvery8d;

public interface PnpDetailEvery8dRepository extends EntityRepository<PnpDetailEvery8d, Long>{
	
    @Transactional(readOnly = true, timeout = 30)
    List<PnpDetailEvery8d> findByPnpDetailId(Long noticeDetailId);
    
    @Transactional(readOnly = true, timeout = 30)
    List<PnpDetailEvery8d> findByPnpDetailIdAndStatus(Long pnpDetailId, String status);
    
    @Query("select  b from PnpDetailEvery8d b  where b.pnpDetailId in (?1) ")
    public List<PnpDetailEvery8d> findByPnpDetailIds(List<Long> pnpDetailId);
    
    @Query(value="select count(pnpDetailId) from PnpDetailEvery8d b  where b.pnpMainId = ?1 and b.status in (?2) ")
    public Long countByPnpMainIdAndStatus(Long pnpMainId, List<String> status);
    
    @Query(value="select count(pnpDetailId) from PnpDetailEvery8d b  where b.pnpDetailId = ?1 and b.status in (?2) ")
    public Long countByPnpDetailIdAndStatus(Long pnpDetailId, List<String> status);
    
    @Modifying
   	@Query("update PnpDetailEvery8d x set x.status = ?1 , x.modifyTime = ?2 where x.pnpMainId = ?3  ")
   	@Transactional(rollbackFor = Exception.class, timeout = 30)
   	void updateStatusByMainId(String status, Date modifyTime,  Long pnpMainId);
}
