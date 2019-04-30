package com.bcs.web.ui.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.service.CampaignService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;

@Service
public class CampaignUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(CampaignUIService.class);
	
	@Autowired
	private CampaignService campaignService;
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public void deleteFromUI(String campaignId, String account) throws BcsNoticeException {
        logger.info("deleteFromUI:" + campaignId);
        
        String campaignName = campaignService.findCampaignNameByCampaignId(campaignId);
        campaignService.delete(campaignId);
        createSystemLog("Delete", campaignName, account, new Date(), campaignId.toString());
    }
	
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
        SystemLogUtil.saveLogDebug("Campaign", action, modifyUser, content, referenceId);
    }
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public Campaign saveFromUI(Campaign campaign, String account) throws BcsNoticeException{
        logger.info("saveFromUI:" + campaign);

        String campaignId = campaign.getCampaignId();
        
        String action = "Edit";
        if (campaignId == null) {
            action = "Create";
            
            campaignId = campaignService.generateCampaignId();
            campaign.setCampaignId(campaignId);
        }
            
        // Set Modify Admin User
        campaign.setModifyUser(account);
        campaign.setModifyTime(new Date());
        
        // Save Campaign
        campaignService.save(campaign);
        
        campaign = campaignService.findOne(campaign.getCampaignId());
        createSystemLog(action, campaign, campaign.getModifyUser(), campaign.getModifyTime(), campaign.getCampaignId());
        return campaign;
    }

    @Transactional(rollbackFor=Exception.class, timeout = 30)
	public Campaign switchIsActive(String campaignId, String account) throws BcsNoticeException{
        logger.info("switchIsActive:" + campaignId);
        
        Campaign campaign = campaignService.findOne(campaignId);
        if (campaign != null) {
            boolean switchValue = (campaign.getIsActive() == Boolean.TRUE) ? false : true;
            campaign.setIsActive(switchValue);
            
            // Set Modify Admin User
            campaign.setModifyUser(account);
            campaign.setModifyTime(new Date());
            // Save Campaign
            campaignService.save(campaign);
        }
        
        return campaign;
	}
}
