package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.persistence.EntityRepository;

@Repository
public interface CampaignRepository extends EntityRepository<Campaign, String> {
    
    @Transactional(readOnly = true, timeout = 30)
    public Campaign findByCampaignName(String name);
    
    @Transactional(readOnly = true, timeout = 30)
    @Query("select x.campaignName from Campaign x where x.campaignId = ?1")
    public String findCampaignNameByCampaignId(String campaignId);

    @Transactional(readOnly = true, timeout = 30)
    public List<Campaign> findByIsActive(Boolean isActive);
}
