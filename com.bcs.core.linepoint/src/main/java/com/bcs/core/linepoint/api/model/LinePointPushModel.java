package com.bcs.core.linepoint.api.model;

import java.util.Date;

import org.json.JSONArray;

import com.bcs.core.json.AbstractBcsEntity;

public class LinePointPushModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	public static final String SEND_TIMING_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SEND_TIMING_TYPE_SCHEDULE = "SCHEDULE";

	public static final String SOURCE_TYPE_BCS = "BCS";
	public static final String SEND_TYPE_API = "API";


	// input data
	private Long eventId;
	private String clientId;
	private JSONArray uid;
	private Long amount;
	
	// input information
	private String sendTimeType;
	private String source;

	// output information
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
	
}