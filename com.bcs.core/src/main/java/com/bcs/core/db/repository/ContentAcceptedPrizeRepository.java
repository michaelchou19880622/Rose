package com.bcs.core.db.repository;

import java.util.List;

import com.bcs.core.db.entity.ContentAcceptedPrize;
import com.bcs.core.db.persistence.EntityRepository;

public interface ContentAcceptedPrizeRepository extends EntityRepository<ContentAcceptedPrize, String>{

    public List<ContentAcceptedPrize> findByGameIdAndMid(String gameId, String mid);
    
    public List<ContentAcceptedPrize> findByGameId(String gameId);
}
