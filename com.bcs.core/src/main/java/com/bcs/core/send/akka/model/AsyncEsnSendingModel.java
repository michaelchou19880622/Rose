package com.bcs.core.send.akka.model;

import java.util.List;

import com.bcs.core.db.entity.ContentEsnDetail;
import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.linecorp.bot.model.message.Message;

public class AsyncEsnSendingModel extends AbstractBcsEntity{
	
	private static final long serialVersionUID = 1L;
	
	private String ChannelId;
	private List<Message> messageList;
	private List<ContentEsnDetail> esnDetails;
	private API_TYPE apiType;
	
	public AsyncEsnSendingModel(String ChannelId, List<Message> messageList, List<ContentEsnDetail> esnDetails, API_TYPE apiType){
		this.ChannelId = ChannelId;
		this.messageList = messageList;
		this.esnDetails = esnDetails;
		this.apiType = apiType;
	}

	public String getChannelId() {
		return ChannelId;
	}

	public List<Message> getMessageList() {
		return messageList;
	}

	public List<ContentEsnDetail> getEsnDetails() {
		return esnDetails;
	}

	public API_TYPE getApiType() {
		return apiType;
	}
}
