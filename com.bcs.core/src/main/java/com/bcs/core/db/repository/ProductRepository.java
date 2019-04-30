package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Product;
import com.bcs.core.db.persistence.EntityRepository;

@Repository
public interface ProductRepository extends EntityRepository<Product, Long> {
    
    @Transactional(readOnly = true, timeout = 30)
    public Product findByProductNameAndGroupId(String productName, String groupId);
    
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.productName from Product x where x.productId = ?1")
    public String findProductNameByProductId(Long productId);
    
    @Transactional(readOnly = true, timeout = 30)
    public List<Product> findByGroupId(String groupId);
    
    public Long deleteByGroupId(String groupId);
}
