package com.bcs.core.api.service.model;

import java.util.Date;

import org.json.JSONArray;

import com.bcs.core.json.AbstractBcsEntity;

public class PushApiModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	public static final String SEND_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SEND_TYPE_DELAY = "DELAY";
	
	private String department;
	
	private JSONArray uid;
	
	private String sendTimeType;
	
	private Date sendTimeSet;
	
	private JSONArray messages;
	
	private Date triggerTime;
	
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	
	public JSONArray getUid() {
		return uid;
	}
	public void setUid(JSONArray uid) {
		this.uid = uid;
	}
	
	public String getSendTimeType() {
		return sendTimeType;
	}
	public void setSendTimeType(String sendTimeType) {
		this.sendTimeType = sendTimeType;
	}
	
	public Date getSendTimeSet() {
		return sendTimeSet;
	}
	public void setSendTimeSet(Date sendTimeSet) {
		this.sendTimeSet = sendTimeSet;
	}
	
	public JSONArray getMessages() {
		return messages;
	}
	public void setMessages(JSONArray messages) {
		this.messages = messages;
	}
	
	public Date getTriggerTime() {
		return triggerTime;
	}
	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}
}