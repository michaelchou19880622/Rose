package com.bcs.core.db.repository;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ScratchCardDetail;
import com.bcs.core.db.persistence.EntityRepository;

public interface ScratchCardDetailRepository extends EntityRepository<ScratchCardDetail, String>{
	@Query(value = "SELECT * FROM BCS_SCRATCHCARD_DETAIL WHERE GAME_ID = ?1", nativeQuery = true)
	public ScratchCardDetail findOneByGameId(String gameId);
}
