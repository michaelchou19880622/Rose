package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.UserFieldSet;
import com.bcs.core.db.persistence.EntityRepository;

public interface UserFieldSetRepository extends EntityRepository<UserFieldSet, Long>{

	@Transactional(readOnly = true, timeout = 30)
	public List<UserFieldSet> findByMid(String mid);

	@Transactional(readOnly = true, timeout = 30)
	public List<UserFieldSet> findByMidAndKeyData(String mid, String keyData);

	public Long deleteByMid(String mid);
	
	public Long deleteByMidAndKeyData(String mid, String keyData);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT "
			+ "KEY_DATA, NAME, TYPE "
			+ "FROM BCS_USER_FIELD_SET "
			+ "GROUP BY KEY_DATA, NAME, TYPE ", nativeQuery = true)
	public List<Object[]> getFieldKeyAndNameAndType();
}
