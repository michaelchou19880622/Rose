package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_SHARE_USER_RECORD", 
indexes = {
        @Index(name = "INDEX_0", columnList = "CAMPAIGN_ID"),
        @Index(name = "INDEX_0", columnList = "UID"),
        @Index(name = "INDEX_1", columnList = "CAMPAIGN_ID")
})
public class ShareUserRecord extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "SHARE_USER_RECORD_ID", columnDefinition="nvarchar(50)")
    private String shareUserRecordId;
	
	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String uid;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
    @Column(name = "CAMPAIGN_ID")
    private String campaignId;

    public String getShareUserRecordId() {
        return shareUserRecordId;
    }

    public void setShareUserRecordId(String shareUserRecordId) {
        this.shareUserRecordId = shareUserRecordId;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}
