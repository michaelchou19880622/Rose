package com.bcs.core.send.akka.model;

import java.util.Date;
import java.util.List;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;

public class AsyncSendingModelSuccess extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String ChannelId;
	private List<String[]> mids;
	private API_TYPE apiType;
	private Long updateMsgId;
	private Date date;
	
	public AsyncSendingModelSuccess(String ChannelId, List<String[]> mids, API_TYPE apiType, Date date){
		this.ChannelId = ChannelId;
		this.mids = mids;
		this.apiType = apiType;
		this.date = date;
	}
	
	public AsyncSendingModelSuccess(String ChannelId, List<String[]> mids, API_TYPE apiType, Long updateMsgId, Date date){
		this.ChannelId = ChannelId;
		this.mids = mids;
		this.apiType = apiType;
		this.updateMsgId = updateMsgId;
		this.date = date;
	}

	public String getChannelId() {
		return ChannelId;
	}

	public List<String[]> getMids() {
		return mids;
	}

	public API_TYPE getApiType() {
		return apiType;
	}

	public Long getUpdateMsgId() {
		return updateMsgId;
	}

	public Date getDate() {
		return date;
	}
}
