package com.bcs.core.db.repository;

import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.BusinessGroup;
import com.bcs.core.db.persistence.EntityRepository;

public interface BusinessGroupRepository extends EntityRepository<BusinessGroup, String> {
    
    @Transactional(readOnly = true, timeout = 30)
    public BusinessGroup findByGroupName(String name);
}
