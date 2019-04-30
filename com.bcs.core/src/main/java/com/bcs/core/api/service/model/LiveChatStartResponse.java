package com.bcs.core.api.service.model;

import java.util.Date;

import com.bcs.core.json.AbstractBcsEntity;

public class LiveChatStartResponse extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final int WAITING = 0;
	public static final int IN_PROGRESS = 1;
	public static final int NON_OFFICE_HOUR = 2;
	
	private Long chatId;
	private int status;
	private String category;
	private String hash;
	private String fowardURL;
	private String returnMessage;
	private Date timestamp;
	
	public Long getChatId() {
		return chatId;
	}
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getFowardURL() {
		return fowardURL;
	}
	public void setFowardURL(String fowardURL) {
		this.fowardURL = fowardURL;
	}
	
	public String getReturnMessage() {
		return returnMessage;
	}
	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}