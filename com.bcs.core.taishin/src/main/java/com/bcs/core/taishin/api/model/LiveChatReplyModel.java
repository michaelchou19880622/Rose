package com.bcs.core.taishin.api.model;

import com.bcs.core.json.AbstractBcsEntity;

public class LiveChatReplyModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final Integer NORMAL_REPLY = 0;
	public static final Integer NORMAL_WAITING = 1;
	public static final Integer LONG_WAITING = 2;
	
	private String channel;
	private String to;
	private String msgType;
	private String text;
	private Integer isWaiting;
	
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
	public Integer getIsWaiting() {
		return isWaiting;
	}
	public void setIsWaiting(Integer isWaiting) {
		this.isWaiting = isWaiting;
	}
	
	public void validateResponse() throws Exception {
		if(to == null)
			throw new IllegalArgumentException("Parameter \"to\" is required!");
		if(to.equals(""))
			throw new IllegalArgumentException("Parameter \"to\" cannot be empty!");
		
		if(channel == null)
			throw new IllegalArgumentException("Parameter \"channel\" is required!");
		if(channel.equals(""))
			throw new IllegalArgumentException("Parameter \"channel\" cannot be empty!");		
		if(!channel.equals("TaishinCC"))
			throw new IllegalArgumentException("Invalid channel!");
		
		if(isWaiting == null) {
			throw new IllegalArgumentException("Parameter \"isWaiting\" is required!");
		}
		if(isWaiting > 2 || isWaiting < 0)
			throw new IllegalArgumentException("Invalid \"isWaiting\" value!");
		if(isWaiting == 0 || isWaiting == 3) {
			if(msgType == null)
				throw new IllegalArgumentException("Parameter \"msgType\" is required!");
			if(msgType.equals(""))
				throw new IllegalArgumentException("Parameter \"msgType\" cannot be empty!");	
			
			if(text == null)
				throw new IllegalArgumentException("Parameter \"text\" is required!");
			if(text.equals(""))
				throw new IllegalArgumentException("Parameter \"text\" cannot be empty!");
		}
	}
}