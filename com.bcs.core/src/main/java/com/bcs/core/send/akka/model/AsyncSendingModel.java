package com.bcs.core.send.akka.model;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;

public class AsyncSendingModel extends AbstractBcsEntity{
	
	private static final long serialVersionUID = 1L;
	
	private String ChannelId;
	private List<MsgGenerator> msgGenerators;
	private List<String> mids;
	private API_TYPE apiType;
	private Long updateMsgId;
	
	public AsyncSendingModel(String ChannelId, List<MsgGenerator> msgGenerators, List<String> mids, API_TYPE apiType){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		this.mids = mids;
		this.apiType = apiType;
		this.updateMsgId = null;
	}
	
	public AsyncSendingModel(String ChannelId, List<MsgGenerator> msgGenerators, List<String> mids, API_TYPE apiType, Long updateMsgId){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		this.mids = mids;
		this.apiType = apiType;
		this.updateMsgId = updateMsgId;
	}
	
	public AsyncSendingModel(String ChannelId, List<MsgGenerator> msgGenerators, String mid, API_TYPE apiType){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		
		List<String> mids = new ArrayList<String>();
		mids.add(mid);
		this.mids = mids;
		
		this.apiType = apiType;
	}
	
	public AsyncSendingModel(String ChannelId, List<MsgGenerator> msgGenerators, String mid, API_TYPE apiType, Long updateMsgId){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		
		List<String> mids = new ArrayList<String>();
		mids.add(mid);
		this.mids = mids;
		
		this.apiType = apiType;
		this.updateMsgId = updateMsgId;
	}
	
	public AsyncSendingModel(String mid, String ChannelId, List<MsgGenerator> msgGenerators, API_TYPE apiType, Long updateMsgId){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;
		
		List<String> mids = new ArrayList<String>();
		mids.add(mid);
		this.mids = mids;
		
		this.apiType = apiType;
		this.updateMsgId = updateMsgId;
	}
	
	public AsyncSendingModel(List<String> mids, String ChannelId, List<MsgGenerator> msgGenerators, API_TYPE apiType, Long updateMsgId){
		this.ChannelId = ChannelId;
		this.msgGenerators = msgGenerators;

		this.mids = mids;
		
		this.apiType = apiType;
		this.updateMsgId = updateMsgId;
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

	public Long getUpdateMsgId() {
		return updateMsgId;
	}
}
