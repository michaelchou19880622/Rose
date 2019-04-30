package com.bcs.core.bot.api.model;

import com.bcs.core.bot.enums.SEND_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;

public class SendToBotModel extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	private String ChannelId;
	private String ChannelName;
	private SEND_TYPE sendType;
	private ReplyMessage replyMessage;
	private PushMessage pushMessage;
	
	public SendToBotModel(){
		
	}
	
	public SendToBotModel(String ChannelId, SEND_TYPE sendType, ReplyMessage replyMessage){
		this.ChannelId = ChannelId;
		this.sendType = sendType;
		this.replyMessage = replyMessage;
	}
	
	public SendToBotModel(String ChannelId, SEND_TYPE sendType, PushMessage pushMessage){
		this.ChannelId = ChannelId;
		this.sendType = sendType;
		this.setPushMessage(pushMessage);
	}

	public String getChannelId() {
		return ChannelId;
	}

	public void setChannelId(String channelId) {
		ChannelId = channelId;
	}

	public String getChannelName() {
		return ChannelName;
	}

	public void setChannelName(String channelName) {
		ChannelName = channelName;
	}

	public SEND_TYPE getSendType() {
		return sendType;
	}

	public void setSendType(SEND_TYPE sendType) {
		this.sendType = sendType;
	}

	public ReplyMessage getReplyMessage() {
		return replyMessage;
	}

	public void setReplyMessage(ReplyMessage replyMessage) {
		this.replyMessage = replyMessage;
	}

	public PushMessage getPushMessage() {
		return pushMessage;
	}

	public void setPushMessage(PushMessage pushMessage) {
		this.pushMessage = pushMessage;
	}
	
}
