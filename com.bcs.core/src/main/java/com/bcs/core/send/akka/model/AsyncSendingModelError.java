package com.bcs.core.send.akka.model;

import java.util.Date;
import java.util.List;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;

public class AsyncSendingModelError extends AbstractBcsEntity{
	
	private static final long serialVersionUID = 1L;

	private String ChannelId;
	private List<MsgGenerator> msgGenerators;
	private List<String> mids;
	private API_TYPE apiType;
	private String ErrorMsg;
	private Long updateMsgId;
	private int retryTime = 0;
	private Date date;
	
	public AsyncSendingModelError(String ChannelId, List<MsgGenerator> msgGenerators, List<String> mids, API_TYPE apiType, String ErrorMsg, Date date){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		this.mids = mids;
		this.apiType = apiType;
		this.ErrorMsg = ErrorMsg;
		this.date = date;
	}
	
	public AsyncSendingModelError(String ChannelId, List<MsgGenerator> msgGenerators, List<String> mids, API_TYPE apiType, String ErrorMsg, Long updateMsgId, Date date){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		this.mids = mids;
		this.apiType = apiType;
		this.ErrorMsg = ErrorMsg;
		this.updateMsgId = updateMsgId;
		this.date = date;
	}
	
	public AsyncSendingModelError(List<MsgGenerator> msgGenerators, String ChannelId, List<String> mids, API_TYPE apiType, String ErrorMsg, Long updateMsgId, Date date){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		this.mids = mids;
		this.apiType = apiType;
		this.ErrorMsg = ErrorMsg;
		this.updateMsgId = updateMsgId;
		this.date = date;
	}

	public String getChannelId() {
		return ChannelId;
	}

	public List<MsgGenerator> getMsgGenerators() {
		return msgGenerators;
	}

	public List<String> getMids() {
		return mids;
	}

	public API_TYPE getApiType() {
		return apiType;
	}

	public String getErrorMsg() {
		return ErrorMsg;
	}

	public Long getUpdateMsgId() {
		return updateMsgId;
	}

	public int retryTimeAdd() {
		retryTime++;
		return retryTime;
	}

	public int getRetryTime() {
		return retryTime;
	}

	public Date getDate() {
		return date;
	}
	
}
