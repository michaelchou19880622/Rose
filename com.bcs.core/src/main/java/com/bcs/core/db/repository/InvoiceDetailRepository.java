package com.bcs.core.db.repository;

import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.InvoiceDetail;
import com.bcs.core.db.persistence.EntityRepository;

@Repository
public interface InvoiceDetailRepository extends EntityRepository<InvoiceDetail, Long> {

}
