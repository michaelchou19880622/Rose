package com.bcs.core.db.repository;

import org.springframework.stereotype.Repository;

import com.bcs.core.db.entity.CampaignFlow;
import com.bcs.core.db.persistence.EntityRepository;

@Repository
public interface CampaignFlowRepository extends EntityRepository<CampaignFlow, String> {

}
