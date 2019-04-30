package com.bcs.core.receive.model;

import java.util.Date;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;

public class ReceivedModelOriginal extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String receivingMsg;
	private String ChannelId;
	private String ChannelName;
	private String channelSignature;
	private API_TYPE apiType;
	private Date start;
	
	public ReceivedModelOriginal(String receivingMsg, String ChannelId,String ChannelName, String channelSignature, API_TYPE apiType){
		this.receivingMsg = receivingMsg;
		this.ChannelId = ChannelId;
		this.channelSignature = channelSignature;
		this.apiType = apiType;
		this.ChannelName = ChannelName;
		this.setStart(new Date());
	}

	public String getReceivingMsg() {
		return receivingMsg;
	}

	public String getChannelId() {
		return ChannelId;
	}

	public String getChannelSignature() {
		return channelSignature;
	}

	public API_TYPE getApiType() {
		return apiType;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public String getChannelName() {
		return ChannelName;
	}

	public void setChannelName(String channelName) {
		ChannelName = channelName;
	}
	
}
