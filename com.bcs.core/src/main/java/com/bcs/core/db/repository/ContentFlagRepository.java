package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentFlagRepository extends EntityRepository<ContentFlag, Long> {

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.flagValue "
			+ "from ContentFlag x "
			+ "where x.referenceId = ?1 "
			+ "and x.contentType = ?2 "
			+ "order by x.flagValue asc")
	List<String> findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(String referenceId, String contentType);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select distinct x.flagValue "
			+ "from ContentFlag x "
			+ "where x.flagValue like ?1 "
			+ "order by x.flagValue asc")
	List<String> findDistinctFlagValueByFlagValueLikeOrderByFlagValueAsc(String flagValue);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select distinct x.flagValue "
			+ "from ContentFlag x "
			+ "where x.flagValue like ?1 "
			+ "and x.contentType = ?2 "
			+ "order by x.flagValue asc")
	List<String> findDistinctFlagValueByFlagValueLikeAndContentTypeOrderByFlagValueAsc(String flagValue, String contentType);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select distinct x.flagValue "
			+ "from ContentFlag x "
			+ "where x.contentType = ?1 "
			+ "order by x.flagValue asc")
	List<String> findDistinctFlagValueByContentTypeOrderByFlagValueAsc(String contentType);
	
	Long deleteByReferenceIdAndContentType(String referenceId, String contentType);
}
