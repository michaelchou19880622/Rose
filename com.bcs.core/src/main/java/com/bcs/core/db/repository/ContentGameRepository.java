package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ContentGame;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentGameRepository  extends EntityRepository<ContentGame, String>{
    
    @Query(value = "SELECT GAME_ID, GAME_NAME FROM BCS_CONTENT_GAME WHERE STATUS <> 'DELETE' ORDER BY GAME_ID", nativeQuery = true)
    public List<Object[]> findAllGameIdAndGameName();
    
}
