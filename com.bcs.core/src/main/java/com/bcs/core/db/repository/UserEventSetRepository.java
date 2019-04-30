package com.bcs.core.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.UserEventSet;
import com.bcs.core.db.persistence.EntityRepository;

public interface UserEventSetRepository extends EntityRepository<UserEventSet, Long>{

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserEventSet> findByTargetAndAction(String target, String action, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Long countByTargetAndAction(String target, String action);

	@Transactional(readOnly = true, timeout = 30)
	public Long countByTargetAndActionAndReferenceId(String target, String action, String referenceId);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserEventSet> findByTargetAndActionAndReferenceId(String target, String action, String referenceId, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserEventSet> findByReferenceId(String referenceId, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserEventSet> findByMid(String mid, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserEventSet> findByMidAndTargetAndAction(String mid, String target, String action, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserEventSet> findByMidAndReferenceId(String mid, String referenceId, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	public Page<UserEventSet> findByMidAndTargetAndActionAndReferenceId(String mid, String target, String action, String referenceId, Pageable pageable);

	public Long deleteByTargetAndAction(String target, String action);

	public Long deleteByTargetAndActionAndReferenceId(String target, String action, String referenceId);
}
