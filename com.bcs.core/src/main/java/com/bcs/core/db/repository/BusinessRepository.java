package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Business;
import com.bcs.core.db.persistence.EntityRepository;

public interface BusinessRepository extends EntityRepository<Business, Long> {
    
    @Transactional(readOnly = true, timeout = 30)
    public List<Business> findByGroupId(String groupId);
    
    public Long deleteByGroupId(String groupId);
}
