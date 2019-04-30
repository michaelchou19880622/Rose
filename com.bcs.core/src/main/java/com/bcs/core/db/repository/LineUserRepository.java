package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.persistence.EntityRepository;

public interface LineUserRepository extends EntityRepository<LineUser, String>, LineUserRepositoryCustom {

	@Transactional(readOnly = true, timeout = 30)
	LineUser findByMid(String mid);
	
	@Transactional(readOnly = true, timeout = 30)
	List<LineUser> findByMobileAndBirthday(String mobile, String birthday);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.mid from LineUser x where x.mid in ( ?1 )")
//	@Query("select x.mid from LineUser x where x.mid in ?1") // MYSQL Difference
	List<String> findMidByMidIn(List<String> mids);

	@Query("select x.mid from LineUser x where x.mid in ( ?1 ) and (x.status = 'BINDED' or x.status = 'UNBIND')")
//	@Query("select x.mid from LineUser x where x.mid in  ?1  and (x.status = 'BINDED' or x.status = 'UNBIND')")// MYSQL Difference
	List<String> findMidByMidInAndActive(List<String> mids);

	@Transactional(readOnly = true, timeout = 30)
	Long countByStatus(String status);

	@Transactional(readOnly = true, timeout = 30)
	@Query(value = "SELECT COUNT(DISTINCT MID) FROM BCS_LINE_USER WHERE  STATUS = ?1 AND CREATE_TIME >= ?2 AND CREATE_TIME < ?3", nativeQuery = true)
	public Long countByStatus(String status, String start, String end);
	
	List<LineUser> findByStatus(String status);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.mid from LineUser x where x.status in ( ?1 )")
//	@Query("select x.mid from LineUser x where x.status in ?1") // MYSQL Difference
	Page<String> findMIDByStatus(String status, Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.mid from LineUser x where x.status in ( ?1 ) and x.mid = (?2)")
//	@Query("select x.mid from LineUser x where x.status in ?1 and x.mid = (?2)") // MYSQL Difference
	String checkMIDByStatus(String status, String mid);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.mid from LineUser x where x.status = 'BINDED' or x.status = 'UNBIND'")
	Page<String> findMIDAllActive(Pageable pageable);

	@Transactional(readOnly = true, timeout = 30)
	@Query("select x.mid from LineUser x where (x.status = 'BINDED' or x.status = 'UNBIND') and x.mid = (?1)")
	String checkMIDAllActive(String mid);
	
	@Transactional(readOnly = true, timeout = 30)
    @Query(value = "select * from BCS_LINE_USER where CREATE_TIME >= ?1 and CREATE_TIME < ?2 order by CREATE_TIME", nativeQuery = true)
	public List<LineUser> findByCreateTime(String start, String end);
}
