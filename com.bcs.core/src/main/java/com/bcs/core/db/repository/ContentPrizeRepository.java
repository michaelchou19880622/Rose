package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ContentPrize;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentPrizeRepository extends EntityRepository<ContentPrize, String>{
	@Query(value = "SELECT * FROM BCS_CONTENT_PRIZE "
			+ "WHERE GAME_ID = ?1 AND (BCS_CONTENT_PRIZE.STATUS <> 'DELETE' OR BCS_CONTENT_PRIZE.STATUS IS NULL) "
			+ "ORDER BY BCS_CONTENT_PRIZE.PRIZE_LETTER"
			, nativeQuery = true)
	public List<ContentPrize> findByGameId(String gameId);
}
