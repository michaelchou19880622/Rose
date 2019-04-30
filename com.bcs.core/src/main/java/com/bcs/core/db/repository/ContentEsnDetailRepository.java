package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentEsnDetail;
import com.bcs.core.db.persistence.EntityRepository;

@Repository
public interface ContentEsnDetailRepository extends EntityRepository<ContentEsnDetail, Long> {
	
	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "select * from BCS_CONTENT_ESN_DETAIL where ESN_ID = ?1", nativeQuery = true)
    List<ContentEsnDetail> findByEsnId(String esnId);
	
	@Query(value = "select count(*) from BCS_CONTENT_ESN_DETAIL "
	        + "where ESN_ID = ?1 and UID is null", nativeQuery = true)
	int countNotUsedByEsnId(String esnId);
	
	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "select x from ContentEsnDetail x where x.esnId = ?1 and x.uid is null")
    Page<ContentEsnDetail> findNotUsedByEsnId(String esnId, Pageable pageable);
	
	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "select * from BCS_CONTENT_ESN_DETAIL where ESN_ID = ?1 and STATUS = ?2 and UID is not null", nativeQuery = true)
    List<ContentEsnDetail> findByEsnIdAndStatusAndUidNotNull(String esnId, String status);
	
	@Modifying
    @Query(value = "update BCS_CONTENT_ESN_DETAIL set STATUS = ?1, SEND_TIME = ?2 where ESN_DETAIL_ID in (?3)", nativeQuery = true)
    int updateStatusAndSendTimeByDetailIds(String status, Date sendTime, List<Long> detailIds);
}
