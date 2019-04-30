package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CAMPAIGN_FLOW", indexes = {})
public class CampaignFlow extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "MID", columnDefinition="nvarchar(50)")
	private String mid;

	@Column(name = "STEP")
    private Integer step;

	@Column(name = "MSG_INTERACTIVE_ID")
	private Long iMsgId;

	@Column(name = "LAST_MODIFY_TIME")
    private Date lastModifiedTime;

	@Column(name = "ERROR_COUNT")
    private Integer errorCount;

	@Column(name = "ERROR_LIMIT")
    private Integer errorLimit;

	@Column(name = "INV_NUM", columnDefinition="nvarchar(10)")
    private String invNum;

	@Column(name = "INV_TERM", columnDefinition="nvarchar(7)")
    private String invTerm;

	@Column(name = "RANDOM_NUMBER", columnDefinition="nvarchar(4)")
    private String randomNumber;

	@Column(name = "TIMEOUT")
    private Integer timeout;
    
    public CampaignFlow() {
        lastModifiedTime = new Date();
        errorCount = new Integer(0);
    }

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public Integer getStep() {
		return step;
	}

	public void setStep(Integer step) {
		this.step = step;
	}

	public Long getiMsgId() {
		return iMsgId;
	}

	public void setiMsgId(Long iMsgId) {
		this.iMsgId = iMsgId;
	}

	public Date getLastModifiedTime() {
		return lastModifiedTime;
	}

	public void setLastModifiedTime(Date lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}

	public Integer getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(Integer errorCount) {
		this.errorCount = errorCount;
	}

	public Integer getErrorLimit() {
		return errorLimit;
	}

	public void setErrorLimit(Integer errorLimit) {
		this.errorLimit = errorLimit;
	}

	public String getInvNum() {
		return invNum;
	}

	public void setInvNum(String invNum) {
		this.invNum = invNum;
	}

	public String getInvTerm() {
		return invTerm;
	}

	public void setInvTerm(String invTerm) {
		this.invTerm = invTerm;
	}

	public String getRandomNumber() {
		return randomNumber;
	}

	public void setRandomNumber(String randomNumber) {
		this.randomNumber = randomNumber;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
    
}
