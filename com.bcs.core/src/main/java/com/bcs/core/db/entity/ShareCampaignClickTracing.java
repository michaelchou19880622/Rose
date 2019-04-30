package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_SHARE_CAMPAIGN_CLICK_TRACING", indexes = {
        @Index(name = "INDEX_0", columnList = "SHARE_USER_RECORD_ID"),
        @Index(name = "INDEX_0", columnList = "UID")
})
public class ShareCampaignClickTracing extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "CLICK_TRACING_ID")
    private Long clickTracingId;
	
	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String uid;
    
    @Column(name = "SHARE_USER_RECORD_ID", columnDefinition="nvarchar(50)")
    private String shareUserRecordId;
	
    @Column(name = "MODIFY_TIME")
    private Date modifyTime;
    
    public ShareCampaignClickTracing() {
        
    }

    public Long getClickTracingId() {
        return clickTracingId;
    }

    public void setClickTracingId(Long clickTracingId) {
        this.clickTracingId = clickTracingId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShareUserRecordId() {
        return shareUserRecordId;
    }

    public void setShareUserRecordId(String shareUserRecordId) {
        this.shareUserRecordId = shareUserRecordId;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }
}
