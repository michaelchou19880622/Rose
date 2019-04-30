package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "BCS_SHARE_CAMPAIGN", indexes = {
        @Index(name = "INDEX_0", columnList = "STATUS")
})
public class ShareCampaign extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String STATUS_DELETE = "DELETE";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DISABLE = "DISABLE";

	@Id
    @Column(name = "CAMPAIGN_ID", columnDefinition="nvarchar(50)")
    private String campaignId;
    
    @Column(name = "CAMPAIGN_NAME", columnDefinition="nvarchar(50)")
    private String campaignName;
	
    @Column(name = "SHARE_MSG", columnDefinition="nvarchar(1000)")
    private String shareMsg;
    
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Column(name = "START_TIME")
    private Date startTime;
    
    @JsonDeserialize(using = CustomDateDeserializer.class)
    @Column(name = "END_TIME")
    private Date endTime;
    
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;
    
    @Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
    private String modifyUser;
       
    @Column(name = "STATUS", columnDefinition="nvarchar(50)")
    private String status;
    
    @Column(name = "ACTION_IMG_REFERENCE_ID", columnDefinition="nvarchar(50)")
    private String actionImgReferenceId;
    
    @Column(name = "SHARE_IMG_REFERENCE_ID", columnDefinition="nvarchar(50)")
    private String shareImgReferenceId;
    
    @Column(name = "DESCRIPTION_IMG_REFERENCE_ID", columnDefinition="nvarchar(50)")
    private String descriptionImgReferenceId;

    @Column(name = "SHARE_TIMES")
    private Integer shareTimes;
    
    public ShareCampaign() {
        
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getShareMsg() {
        return shareMsg;
    }

    public void setShareMsg(String shareMsg) {
        this.shareMsg = shareMsg;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getModifyUser() {
        return modifyUser;
    }

    public void setModifyUser(String modifyUser) {
        this.modifyUser = modifyUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionImgReferenceId() {
        return actionImgReferenceId;
    }

    public void setActionImgReferenceId(String actionImgReferenceId) {
        this.actionImgReferenceId = actionImgReferenceId;
    }

    public String getShareImgReferenceId() {
        return shareImgReferenceId;
    }

    public void setShareImgReferenceId(String shareImgReferenceId) {
        this.shareImgReferenceId = shareImgReferenceId;
    }

    public String getDescriptionImgReferenceId() {
        return descriptionImgReferenceId;
    }

    public void setDescriptionImgReferenceId(String descriptionImgReferenceId) {
        this.descriptionImgReferenceId = descriptionImgReferenceId;
    }

    public Integer getShareTimes() {
        return shareTimes;
    }

    public void setShareTimes(Integer shareTimes) {
        this.shareTimes = shareTimes;
    }

}
