package com.bcs.core.taishin.circle.db.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.persistence.EntityRepository;
import com.bcs.core.taishin.circle.db.entity.TaishinEmployee;

/**
 * @author ???
 */
@Repository
public interface TaishinEmployeeRepository extends EntityRepository<TaishinEmployee, Long> {

	@Transactional(readOnly = true, timeout = 30)
	TaishinEmployee	findByEmployeeId(String employeeId);
}
