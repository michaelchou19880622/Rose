package com.bcs.core.taishin.circle.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeDetail;

public interface BillingNoticeDetailRepository extends EntityRepository<BillingNoticeDetail, Long>{

    @Transactional(readOnly = true, timeout = 30)
    List<BillingNoticeDetail> findByNoticeDetailIdAndParentType(Long noticeDetailId, String parentType);
    
    @Transactional(readOnly = true, timeout = 30)
    List<BillingNoticeDetail> findByNoticeDetailIdAndStatus(Long noticeDetailId, String status);
    
    @Query("select  b from BillingNoticeDetail b  where b.noticeDetailId in (?1) ")
    public List<BillingNoticeDetail> findByNoticeDetailIds(List<Long> noticeDetailId);
    
    @Query(value="select count(noticeDetailId) from BillingNoticeDetail b  where b.noticeMainId = ?1 and b.status in (?2) ")
    public Long countByNoticeMainIdAndStatus(Long noticeMainId, List<String> status);
    
    @Modifying
   	@Query("update BillingNoticeDetail x set x.status = ?1 , x.modifyTime = ?2 where x.noticeMainId = ?3  ")
   	@Transactional(rollbackFor = Exception.class, timeout = 30)
   	void updateStatusByMainId(String status, Date modifyTime,  Long noticeMainId);
        
}
