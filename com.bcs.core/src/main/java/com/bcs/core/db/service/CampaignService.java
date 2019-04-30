package com.bcs.core.db.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.repository.CampaignRepository;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class CampaignService {
	public static final String CAMPAIGN_SYNC = "CAMPAIGN_SYNC";
	
	@Autowired
	private CampaignRepository campaignRepository;    

    protected LoadingCache<String, Campaign> dataCache;
    
    /** Logger */
    private static Logger logger = Logger.getLogger(CampaignService.class);

	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(CAMPAIGN_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(CAMPAIGN_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
    
    public CampaignService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);

        dataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Campaign>() {
                    @Override
                    public Campaign load(String key) throws Exception {
                        return new Campaign();
                    }
                });
    }
    
    @PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] CampaignService cleaning up...");
        try{
            if(dataCache != null){
                dataCache.invalidateAll();
                dataCache = null;
            }
        }
        catch(Throwable e){}
        
        System.gc();
        logger.info("[DESTROY] CampaignService destroyed.");
    }
    
    private boolean notNull(Campaign result){
        if(result != null && StringUtils.isNotBlank(result.getCampaignId())){
            return true;
        }
        return false;
    }
    
    public Campaign findByName(String name) {
        Campaign result = campaignRepository.findByCampaignName(name);
        if(result != null){
            dataCache.put(result.getCampaignId(), result);
        }
        return result;
    }
    

    public List<Campaign> findAll() {
        return campaignRepository.findAll();
    }

    public List<Campaign> findByIsActive(Boolean isActive) {
        return campaignRepository.findByIsActive(isActive);
    }

    public Long countAll() {
        return campaignRepository.count();
    }

    public void save(Campaign campaign) {
        campaignRepository.save(campaign);

        if(campaign != null){
            dataCache.put(campaign.getCampaignId(), campaign);
			DataSyncUtil.settingReSync(CAMPAIGN_SYNC);
        }
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public void delete(String campaignId) throws BcsNoticeException{
        logger.debug("delete:" + campaignId);
        
        Campaign campaign = campaignRepository.findOne(campaignId);
        
        campaignRepository.delete(campaign);
        dataCache.invalidate(campaignId);
		DataSyncUtil.settingReSync(CAMPAIGN_SYNC);
    }
    
    public String findCampaignNameByCampaignId(String campaignId) throws BcsNoticeException{
        try {
            Campaign result = dataCache.get(campaignId);
            if(notNull(result)){
                return result.getCampaignName();
            }
        } catch (Exception e) {}
        
        return campaignRepository.findCampaignNameByCampaignId(campaignId);
    }
    
    public Campaign findOne(String campaignId){
        try {
            Campaign result = dataCache.get(campaignId);
            if(notNull(result)){
                return result;
            }
        } catch (Exception e) {}
        
        Campaign result = campaignRepository.findOne(campaignId);
        if(result != null){
            dataCache.put(result.getCampaignId(), result);
        }
        return result;
    }
    
    public String generateCampaignId() {
        String campaignId = UUID.randomUUID().toString().toLowerCase();
        
        while (campaignRepository.findOne(campaignId) != null) {
            campaignId = UUID.randomUUID().toString().toLowerCase();
        }
        return campaignId;
    }
}
