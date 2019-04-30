package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.db.persistence.EntityRepository;

public interface PushMessageRecordRepository extends EntityRepository<PushMessageRecord, Long>{
	@Query(value = "SELECT * FROM BCS_PUSH_MESSAGE_RECORD WHERE CREATE_TIME = ?1", nativeQuery = true)
	List<PushMessageRecord> findByCreateTime(String createTime);
}