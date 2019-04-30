package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_MSG_INTERACTIVE_CAMPAIGN",
indexes = {
	       @Index(name = "INDEX_0", columnList = "MSG_INTERACTIVE_ID"),
	})
public class MsgInteractiveCampaign extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "MSG_INTERACTIVE_CAMPAIGN_ID")
	private Long iMsgCampaignId;

	@Column(name = "MSG_INTERACTIVE_ID")
	private Long iMsgId;

    @Column(name = "CAMPAIGN_ID", columnDefinition="nvarchar(50)")
    private String campaignId;

    @Column(name = "ERROR_LIMIT")
    private Integer errorLimit;

    @Column(name = "TIMEOUT")
    private Integer timeout;
    

	public Long getiMsgCampaignId() {
        return iMsgCampaignId;
    }

    public void setiMsgCampaignId(Long iMsgCampaignId) {
        this.iMsgCampaignId = iMsgCampaignId;
    }

    public Long getiMsgId() {
		return iMsgId;
	}

	public void setiMsgId(Long iMsgId) {
		this.iMsgId = iMsgId;
	}

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public Integer getErrorLimit() {
        return errorLimit;
    }

    public void setErrorLimit(Integer errorLimit) {
        this.errorLimit = errorLimit;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
