package com.bcs.core.db.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ShareUserRecord;
import com.bcs.core.db.repository.ShareUserRecordRepository;

@Service
public class ShareUserRecordService {
		
	@Autowired
	private ShareUserRecordRepository shareUserRecordRepository;
	
	public void save(ShareUserRecord userRecord) {
	    shareUserRecordRepository.save(userRecord);
	}
	
	public ShareUserRecord findOne(String shareUserRecordId) {
	    return shareUserRecordRepository.findOne(shareUserRecordId);
	}
	
	public ShareUserRecord findByCampaignIdAndUid(String campaignId, String uid) {
	    return shareUserRecordRepository.findByCampaignIdAndUid(campaignId, uid);
	}
	
	public Integer countByCampaignId(String campaignId){
	    return shareUserRecordRepository.countByCampaignId(campaignId);
	}
	
	public List<Object[]> findByModifyTimeAndCampaignId(Date start, Date end, String campaignId){
	    return shareUserRecordRepository.findByModifyTimeAndCampaignId(start, end, campaignId);
	}
	
    public String generateShareUserRecordId() {
        String shareUserRecordId = UUID.randomUUID().toString().toLowerCase();
        
        while (shareUserRecordRepository.findOne(shareUserRecordId) != null) {
            shareUserRecordId = UUID.randomUUID().toString().toLowerCase();
        }
        return shareUserRecordId;
    }
    
    public List<Object[]> findCompletedByModifyTimeAndCampaignId(Date start, Date end, String campaignId){
        return shareUserRecordRepository.findCompletedByModifyTimeAndCampaignId(start, end, campaignId);
    }
    
    public List<Object[]> findUncompletedByModifyTimeAndCampaignId(Date start, Date end, String campaignId){
        return shareUserRecordRepository.findUncompletedByModifyTimeAndCampaignId(start, end, campaignId);
    }
}
