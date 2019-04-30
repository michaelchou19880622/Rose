package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ProductGroup;
import com.bcs.core.db.persistence.EntityRepository;

@Repository
public interface ProductGroupRepository extends EntityRepository<ProductGroup, String> {

    @Transactional(readOnly = true, timeout = 30)
    public ProductGroup findByGroupName(String name);
    
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.groupName from ProductGroup x where x.groupId = ?1")
    public String findGroupNameByGroupId(String groupId);
    
    @Query(value = "SELECT GROUP_ID, GROUP_NAME FROM BCS_PRODUCT_GROUP ORDER BY GROUP_ID", nativeQuery = true)
    public List<Object[]> findAllGroupIdAndGroupName();
}
