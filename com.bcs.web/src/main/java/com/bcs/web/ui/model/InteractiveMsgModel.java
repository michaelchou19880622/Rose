package com.bcs.web.ui.model;

import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class InteractiveMsgModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	/**
	 * ACTION_TYPE
	 */
	public enum ACTION_TYPE {
		SaveSetting("SaveSetting"),
		;

	    private final String str;
	    
	    ACTION_TYPE(String str) {
	        this.str = str;
	    }
		/**
		 * @return the str
		 */
		public String toString() {
			return str;
		}
	}
	/**
	 * SaveSetting
	 */
	private String actionType;
	
	private Long iMsgId;

	private String interactiveType;
	
	private String userStatus;
	private String interactiveStatus;
	private String keywordInput;

	private String otherRole;
	private String serialId;
	private Long interactiveIndex;

	private String interactiveTimeType;
	private String interactiveStartTime;
	private String interactiveEndTime;
	
	private List<SendMsgDetailModel> sendMsgDetails;
	
	private List<String> otherKeywords;
	
    private String campaignId;

    private Integer errorLimit;
    private Integer timeout;

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public Long getiMsgId() {
		return iMsgId;
	}

	public void setiMsgId(Long iMsgId) {
		this.iMsgId = iMsgId;
	}

	public String getInteractiveType() {
		return interactiveType;
	}

	public void setInteractiveType(String interactiveType) {
		this.interactiveType = interactiveType;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getInteractiveStatus() {
		return interactiveStatus;
	}

	public void setInteractiveStatus(String interactiveStatus) {
		this.interactiveStatus = interactiveStatus;
	}

	public String getKeywordInput() {
		return keywordInput;
	}

	public void setKeywordInput(String keywordInput) {
		this.keywordInput = keywordInput;
	}

	public String getOtherRole() {
		return otherRole;
	}

	public void setOtherRole(String otherRole) {
		this.otherRole = otherRole;
	}

	public Long getInteractiveIndex() {
		return interactiveIndex;
	}

	public void setInteractiveIndex(Long interactiveIndex) {
		this.interactiveIndex = interactiveIndex;
	}

	public List<SendMsgDetailModel> getSendMsgDetails() {
		return sendMsgDetails;
	}

	public void setSendMsgDetails(List<SendMsgDetailModel> sendMsgDetails) {
		this.sendMsgDetails = sendMsgDetails;
	}

	public List<String> getOtherKeywords() {
		return otherKeywords;
	}

	public void setOtherKeywords(List<String> otherKeywords) {
		this.otherKeywords = otherKeywords;
	}

	public String getInteractiveTimeType() {
		return interactiveTimeType;
	}

	public void setInteractiveTimeType(String interactiveTimeType) {
		this.interactiveTimeType = interactiveTimeType;
	}

	public String getInteractiveStartTime() {
		return interactiveStartTime;
	}

	public void setInteractiveStartTime(String interactiveStartTime) {
		this.interactiveStartTime = interactiveStartTime;
	}

	public String getInteractiveEndTime() {
		return interactiveEndTime;
	}

	public void setInteractiveEndTime(String interactiveEndTime) {
		this.interactiveEndTime = interactiveEndTime;
	}

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
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
