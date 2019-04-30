package com.bcs.core.db.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.CampaignFlow;
import com.bcs.core.db.repository.CampaignFlowRepository;

@Service
public class CampaignFlowService {
	
	@Autowired
	private CampaignFlowRepository campaignFlowRepository;    
    
    /** Logger */
    private static Logger logger = Logger.getLogger(CampaignFlowService.class);
    
    public CampaignFlowService(){
    	
    }

    public CampaignFlow save(CampaignFlow campaignFlow) {
    	return campaignFlowRepository.save(campaignFlow);
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public void delete(String mid) throws Exception{
        logger.debug("delete:" + mid);
        
        campaignFlowRepository.delete(mid);
    }
    
    public CampaignFlow findOne(String mid){
        
        return campaignFlowRepository.findOne(mid);
    }
}
