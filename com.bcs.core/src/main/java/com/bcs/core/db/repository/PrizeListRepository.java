package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.PrizeList;
import com.bcs.core.db.persistence.EntityRepository;

public interface PrizeListRepository extends EntityRepository<PrizeList, String>{
	@Query(value = "SELECT TOP 1 * FROM BCS_PRIZE_LIST "
			+ "WHERE PRIZE_ID = ?1 AND BCS_PRIZE_LIST.STATUS = 'notWinned' "
			+ "ORDER BY BCS_PRIZE_LIST.PRIZE_LIST_ID"
			, nativeQuery = true)
//	@Query(value = "SELECT * FROM BCS_PRIZE_LIST "
//			+ "WHERE PRIZE_ID = ?1 AND BCS_PRIZE_LIST.STATUS = 'notWinned' "
//			+ "ORDER BY BCS_PRIZE_LIST.PRIZE_LIST_ID LIMIT 1"
//			, nativeQuery = true)// MYSQL Difference
	public PrizeList findNotWinnedOneByPrizeId(String prizeId);
	
	@Query(value = "SELECT * FROM BCS_PRIZE_LIST "
			+ "WHERE PRIZE_LIST_ID = ?1"
			, nativeQuery = true)
	public PrizeList findOne(Integer prizeListId);
	
	@Query(value = "SELECT * FROM BCS_PRIZE_LIST "
			+ "WHERE PRIZE_ID = ?1 "
			+ "ORDER BY BCS_PRIZE_LIST.PRIZE_LIST_ID"
			, nativeQuery = true)
	public List<PrizeList> findByPrizeId(String prizeId);

	@Query(value = "SELECT * FROM BCS_PRIZE_LIST "
			+ "WHERE MID = ?1 AND STATUS = 'notAccepted' "
			+ "ORDER BY BCS_PRIZE_LIST.PRIZE_LIST_ID"
			, nativeQuery = true)
	public List<PrizeList> findNotAcceptedByMid(String mid);
}
