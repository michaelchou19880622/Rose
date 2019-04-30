package com.bcs.core.db.repository;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.TurntableDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface TurntableDetailRepository extends EntityRepository<TurntableDetail, String>{
	@Query(value = "SELECT * FROM BCS_TURNTABLE_DETAIL "
			+ "WHERE GAME_ID = ?1"
			, nativeQuery = true)
	public TurntableDetail findOneByGameId(String gameId);
}
