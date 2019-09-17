package com.bcs.core.taishin.circle.db.repository;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;
import org.springframework.stereotype.Repository;

/**
 * @author ???
 */
@Repository
public interface TaishinEmployeeRepository extends EntityRepository<TaishinEmployee, Long> {
}
