package com.bcs.core.db.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.repository.ShareCampaignRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ShareCampaignService {
    public static final String SHARE_CAMPAIGN_SYNC = "SHARE_CAMPAIGN_SYNC";
    
    /** Logger */
    private static Logger logger = Logger.getLogger(ShareCampaignService.class);
    
	@Autowired
	private ShareCampaignRepository ShareCampaignRepository;
	
	protected LoadingCache<String, ShareCampaign> dataCache;
	
	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask {

        @Override
        public void run() {

            try {
                // Check Data Sync
                Boolean isReSyncData = DataSyncUtil.isReSyncData(SHARE_CAMPAIGN_SYNC);
                if (isReSyncData) {
                    dataCache.invalidateAll();
                    DataSyncUtil.syncDataFinish(SHARE_CAMPAIGN_SYNC);
                }
            } catch (Throwable e) {
                logger.error(ErrorRecord.recordError(e));
            }
        }
    }
	
	public ShareCampaignService() {
	    
	    flushTimer.schedule(new CustomTask(), 120000, 30000);

        dataCache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<Object, ShareCampaign>() {
                    @Override
                    public ShareCampaign load(Object key) throws Exception {
                        return new ShareCampaign();
                    }
                });
	}
	
	@PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] ShareCampaignService cleaning up...");
        try {
            if (dataCache != null) {
                dataCache.invalidateAll();
                dataCache = null;
            }
        } catch (Throwable e) {
        }

        System.gc();
        logger.info("[DESTROY] ShareCampaignService destroyed.");
    }
	
	private boolean notNull(ShareCampaign result) {
        if (result != null && result.getCampaignId() != null) {
            return true;
        }
        return false;
    }
	
	public ShareCampaign findOne(String campaignId) {
	    try {
	        ShareCampaign result = dataCache.get(campaignId);
            if (notNull(result)) {
                return result;
            }
        } catch (Exception e) {
        }

	    ShareCampaign result = ShareCampaignRepository.findOne(campaignId);
        if (result != null) {
            dataCache.put(campaignId, result);
        }
        return result;
	}
	
	public void save(ShareCampaign shareCampaign) {
	    ShareCampaignRepository.save(shareCampaign);
	    
	    if (shareCampaign != null) {
            dataCache.put(shareCampaign.getCampaignId(), shareCampaign);
            DataSyncUtil.settingReSync(SHARE_CAMPAIGN_SYNC);
        }
	}
	
	public List<ShareCampaign> findByStatus(String status){
        return ShareCampaignRepository.findByStatus(status);
	}

	public String generateCampaignId() {
        String campaignId = UUID.randomUUID().toString().toLowerCase();
        
        while (ShareCampaignRepository.findOne(campaignId) != null) {
            campaignId = UUID.randomUUID().toString().toLowerCase();
        }
        return campaignId;
    }
}
