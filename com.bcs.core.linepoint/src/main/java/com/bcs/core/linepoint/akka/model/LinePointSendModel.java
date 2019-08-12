package com.bcs.core.linepoint.akka.model;

import java.util.Date;

import org.json.JSONArray;

import com.bcs.core.json.AbstractBcsEntity;

public class LinePointSendModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	public static final String SEND_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SEND_TYPE_DELAY = "DELAY";

	public static final String SOURCE_TYPE_BCS = "BCS";
	public static final String SOURCE_TYPE_MGM = "MGM";
	
	private JSONArray uid;
	private String richMenuId;
	
	
	private String clientId;
	
	
	private Long eventId;
	
	private Long amount;
	
	private String source;
	
	private String sendTimeType;
	
	private Date triggerTime;
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public JSONArray getUid() {
		return uid;
	}

	public void setUid(JSONArray uid) {
		this.uid = uid;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}

	public String getSendTimeType() {
		return sendTimeType;
	}

	public void setSendTimeType(String sendTimeType) {
		this.sendTimeType = sendTimeType;
	}

	public Date getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getRichMenuId() {
		return richMenuId;
	}

	public void setRichMenuId(String richMenuId) {
		this.richMenuId = richMenuId;
	}
}