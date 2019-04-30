package com.bcs.core.db.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.persistence.EntityRepository;

public interface ShareCampaignRepository extends EntityRepository<ShareCampaign, String>{

    @Query("select x from ShareCampaign x where x.status = ?1 order by x.modifyTime desc")
    List<ShareCampaign> findByStatus(String status);
}
