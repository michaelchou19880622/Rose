package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;

public class LiveChatUserModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final Integer ACTIVE_CLOSE = 0;
	public static final Integer PASSIVE_CLOSE = 1;
	public static final Integer DISCARD_WAITING = 2;
	
	private String channel;
	private String uid;
	private Integer closeType;
	private String surveyUrl;
	
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getUID() {
		return uid;
	}
	public void setUID(String UID) {
		this.uid = UID;
	}
	
	public Integer getCloseType() {
		return closeType;
	}
	public void setCloseType(Integer closeType) {
		this.closeType = closeType;
	}
	
	public String getSurveyUrl() {
		return surveyUrl;
	}
	public void setSurveyUrl(String surveyUrl) {
		this.surveyUrl = surveyUrl;
	}
	
	public void validateResponse() throws Exception {
		if(channel == null)
			throw new IllegalArgumentException("Parameter \"channel\" is required!");
		if(channel.equals(""))
			throw new IllegalArgumentException("Parameter \"channel\" cannot be empty!");
		if(!channel.equals("TaishinCC"))
			throw new IllegalArgumentException("Invalid channel!");
		
		if(uid == null) {
			throw new IllegalArgumentException("Parameter \"uid\" is required!");
		}
		if(uid.equals("")) {
			throw new IllegalArgumentException("Parameter \"uid\" cannot be empty!");
		}
	}
}