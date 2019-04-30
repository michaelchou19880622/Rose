package com.bcs.web.ui.service;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;

@Service
public class ShareCampaignUIService {

	private static Logger logger = Logger.getLogger(ShareCampaignUIService.class);
	
	@Autowired
	private ShareCampaignService shareCampaignService;

	/**
	 * 新增或修改優惠劵
	 * 
	 * @param contentCouponModel
	 * @param adminUserAccount
	 * @return ContentCoupon
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class)
	public ShareCampaign saveFromUI(ShareCampaign shareCampaign, String adminUserAccount) throws Exception {
		return this.saveFromUI(shareCampaign, adminUserAccount, true);
	}

	/**
	 * 新增或修改優惠劵
	 * 
	 * @param contentCouponModel
	 * @param adminUserAccount
	 * @param doSave
	 * @return ContentCoupon
	 * @throws Exception
	 */
	@Transactional(rollbackFor=Exception.class)
	public ShareCampaign saveFromUI(ShareCampaign shareCampaign, String adminUserAccount, boolean doSave) throws Exception {
		logger.info("saveFromUI:" + shareCampaign);
		
		String action = (shareCampaign.getCampaignId() == null ? "Create" : "Edit");

		// 若是修改
		if (shareCampaign.getCampaignId() != null && doSave) {
		    shareCampaign = mergeOldData(shareCampaign);
		}
		
		String status = shareCampaign.getStatus();
		if(StringUtils.isBlank(status)){
		    shareCampaign.setStatus(ShareCampaign.STATUS_DISABLE);
		}
		
		if(shareCampaign.getCampaignId() == null) {
		    shareCampaign.setCampaignId(shareCampaignService.generateCampaignId());
		}
		
		// Set Modify Admin User
		shareCampaign.setModifyUser(adminUserAccount);
		shareCampaign.setModifyTime(new Date());
		if(doSave){
		    shareCampaignService.save(shareCampaign);
			shareCampaign = shareCampaignService.findOne(shareCampaign.getCampaignId());
			
			createSystemLog(action, shareCampaign, shareCampaign.getModifyUser(), shareCampaign.getModifyTime(), shareCampaign.getCampaignId());
		}
		return shareCampaign;
	}
	
	public void checkShareCampaign(ShareCampaign shareCampaign) {
		Validate.notBlank(shareCampaign.getCampaignName(), "Campaign Name Null");
		Validate.notNull(shareCampaign.getShareTimes(), "Campaign ShareTimes Null");
		Validate.notNull(shareCampaign.getStartTime(), "Campaign Start Time Null");
		Validate.notNull(shareCampaign.getEndTime(), "Campaign End Time Null");

		Validate.notBlank(shareCampaign.getActionImgReferenceId(), "Action Image Null");
		Validate.notBlank(shareCampaign.getShareImgReferenceId(), "Share Image Null");
		Validate.notBlank(shareCampaign.getDescriptionImgReferenceId(), "Description Image Null");
	}
	
	/**
	 * 刪除shareCampaign
	 * 
	 * @param couponId
	 * @param adminUserAccount
	 */
	@Transactional(rollbackFor=Exception.class)
	public void deleteFromUI(String campaignId, String adminUserAccount) {
		logger.info("deleteFromUI:" + campaignId);
		ShareCampaign shareCampaign = shareCampaignService.findOne(campaignId);
		
		shareCampaign.setStatus(ShareCampaign.STATUS_DELETE);
		shareCampaign.setModifyTime(new Date());
		shareCampaign.setModifyUser(adminUserAccount);
		shareCampaignService.save(shareCampaign);

		createSystemLog("Delete", shareCampaign.getCampaignName(), adminUserAccount, new Date(), campaignId);
	}

	@Transactional(rollbackFor=Exception.class)
	public void switchShareCampaignStatus(String campaignId, String adminUserAccount) throws BcsNoticeException{
		// 切換狀態
	    ShareCampaign shareCampaign = shareCampaignService.findOne(campaignId);
		
		String status = shareCampaign.getStatus();
		if(ShareCampaign.STATUS_ACTIVE.equals(status)){
			status = ShareCampaign.STATUS_DISABLE;
		}
		else if(ShareCampaign.STATUS_DISABLE.equals(status)){
			status = ShareCampaign.STATUS_ACTIVE;
		}
		else{
			throw new BcsNoticeException("請選擇正確的MGM活動");
		}
		
		shareCampaign.setStatus(status);
		shareCampaign.setModifyTime(new Date());
		shareCampaign.setModifyUser(adminUserAccount);
		shareCampaignService.save(shareCampaign);

		createSystemLog("SwitchStatus", shareCampaign.getCampaignName(), adminUserAccount, new Date(), campaignId);
	}
	
	/**
	 * 新增系統日誌
	 * 
	 * @param action
	 * @param content
	 * @param modifyUser
	 * @param modifyTime
	 */
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
		SystemLogUtil.saveLogDebug("ShareCampaign", action, modifyUser, content, referenceId);
	}
	
	/**
	 * 將前端修改的欄位合併到原 entity，避免有些不在頁面上的欄位被清空
	 * 
	 * @param newContentCoupon
	 * @return
	 * @throws Exception 
	 */
	private ShareCampaign mergeOldData(ShareCampaign newShareCampaign) throws Exception {
		
		// 原資料
	    ShareCampaign oldShareCampaign = shareCampaignService.findOne(newShareCampaign.getCampaignId());
				
	    oldShareCampaign.setCampaignName(newShareCampaign.getCampaignName());
	    oldShareCampaign.setShareMsg(newShareCampaign.getShareMsg());
	    oldShareCampaign.setStartTime(newShareCampaign.getStartTime());
	    oldShareCampaign.setEndTime(newShareCampaign.getEndTime());
	    oldShareCampaign.setActionImgReferenceId(newShareCampaign.getActionImgReferenceId());
	    oldShareCampaign.setShareImgReferenceId(newShareCampaign.getShareImgReferenceId());
	    oldShareCampaign.setDescriptionImgReferenceId(newShareCampaign.getDescriptionImgReferenceId());
	    oldShareCampaign.setShareTimes(newShareCampaign.getShareTimes());
	    
		return oldShareCampaign;
	}
}
