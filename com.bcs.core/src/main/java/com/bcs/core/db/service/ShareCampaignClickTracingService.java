package com.bcs.core.db.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ShareCampaignClickTracing;
import com.bcs.core.db.repository.ShareCampaignClickTracingRepository;

@Service
public class ShareCampaignClickTracingService {
		
	@Autowired
	private ShareCampaignClickTracingRepository shareCampaignClickTracingRepository;
	
	public void save(ShareCampaignClickTracing clickTracing) {
	    shareCampaignClickTracingRepository.save(clickTracing);
	}
	
	public ShareCampaignClickTracing findByUidAndShareUserRecordId(String uid, String shareUserRecordId){
	    return shareCampaignClickTracingRepository.findByUidAndShareUserRecordId(uid, shareUserRecordId);
	}

	public List<Object[]> findByModifyTimeAndCampaignId(Date start, Date end, String campaignId){
	    return shareCampaignClickTracingRepository.findByModifyTimeAndCampaignId(start, end, campaignId);
	}
}
