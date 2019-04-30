package com.bcs.core.db.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ShareCampaignClickTracing;
import com.bcs.core.db.persistence.EntityRepository;

public interface ShareCampaignClickTracingRepository extends EntityRepository<ShareCampaignClickTracing, Long>{
    
    @Transactional(readOnly = true, timeout = 30)
    @Query(value = "select * from BCS_SHARE_CAMPAIGN_CLICK_TRACING where UID = ?1 and SHARE_USER_RECORD_ID = ?2", nativeQuery = true)
    ShareCampaignClickTracing findByUidAndShareUserRecordId(String uid, String shareUserRecordId);
    
    @Transactional(readOnly = true, timeout = 30)
    @Query(value = "select b.uid as share_uid, "
            + "a.uid as click_uid, "
            + "a.modifyTime as click_time "
            + "from ShareCampaignClickTracing a, ShareUserRecord b "
            + "where a.shareUserRecordId = b.shareUserRecordId "
            + "and a.modifyTime >= ?1 and a.modifyTime < ?2 "
            + "and b.campaignId = ?3 "
            + "order by share_uid, click_time")
    List<Object[]> findByModifyTimeAndCampaignId(Date start, Date end, String campaignId);
}
