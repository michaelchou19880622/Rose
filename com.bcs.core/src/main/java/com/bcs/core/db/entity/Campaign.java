package com.bcs.core.db.entity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CAMPAIGN", indexes = {})
public class Campaign extends AbstractBcsEntity{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "CAMPAIGN_ID", columnDefinition="NVARCHAR(50)")
    private String campaignId = null;
    
    @Column(name = "CAMPAIGN_NAME", columnDefinition="NVARCHAR(200)")
    private String campaignName = null;
    
    @Column(name = "START_TIME")
    private Date startTime = null;
    
    @Column(name = "END_TIME")
    private Date endTime = null;
    
    @Column(name = "MODIFY_TIME")
    private Date modifyTime = null;
    
    @Column(name = "MODIFY_USER", columnDefinition="NVARCHAR(50)")
    private String modifyUser = null;
    
    @Column(name = "GROUP_ID", columnDefinition="NVARCHAR(50)")
    private String groupId = null;
    
    @Column(name = "IS_ACTIVE")
    private Boolean isActive = Boolean.TRUE;
    
    @Column(name = "PRICE")
    private Integer price = null;
    
    @Column(name = "GAME_ID")
    private Long gameId;
    
    public Campaign() {
        
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    public void setStartTime(String startTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            this.startTime = df.parse(startTime);
        } catch (ParseException e) {}
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public void setEndTime(String endTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            this.endTime = df.parse(endTime);
        } catch (ParseException e) {}
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
    
    
}
