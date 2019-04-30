package com.bcs.core.api.service.model;

import com.bcs.core.json.AbstractBcsEntity;

public class LiveChatResponse extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private Boolean error;
	private String errorMessage;
	private Long feedbackId;
	private String textMessage;
	
	public Boolean getError() {
		return error;
	}
	public void setError(Boolean error) {
		this.error = error;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public Long getFeedbackId() {
		return feedbackId;
	}
	public void setFeedbackId(Long feedbackId) {
		this.feedbackId = feedbackId;
	}
	
	public String getTextMessage() {
		return textMessage;
	}
	public void setTextMessage(String textMessage) {
		this.textMessage = textMessage;
	}
}