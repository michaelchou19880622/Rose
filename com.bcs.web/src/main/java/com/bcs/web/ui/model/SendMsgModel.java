package com.bcs.web.ui.model;

import java.util.ArrayList;
import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class SendMsgModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	/**
	 * ACTION_TYPE
	 */
	public enum ACTION_TYPE {
		SaveDraft("SaveDraft"),
		SendMsg("SendMsg"),
		SendToMe("SendToMe"),
		SendToTestGroup("SendToTestGroup"),
		RedesignMsg("RedesignMsg"),
		;

	    private final String str;
	    
	    ACTION_TYPE(String str) {
	        this.str = str;
	    }
		/**
		 * @return the str
		 */
		public String toString() {
			return str;
		}
	}
	/**
	 * SaveDraft
	 * SendMsg
	 * SendToMe
	 * SendToTest
	 */
	private String actionType;
	
	private Long msgId;
	
	/**
	 * Target Send Group
	 */
	private Long sendGroupId;
	
	/**
	 * Sending Msg Type
	 * IMMEDIATE
	 * DELAY
	 * SCHEDULE
	 */
	private String sendingMsgType;
	/**
	 * If SENDING_MSG_TYPE == DELAY, SCHEDULE
	 */
	private String sendingMsgTime;
	
	private List<String> msgTagList = new ArrayList<>();
	
	private List<SendMsgDetailModel> sendMsgDetails;

	private String serialId;
	
	public String getActionType() {
		return actionType;
	}
	
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public Long getSendGroupId() {
		return sendGroupId;
	}

	public void setSendGroupId(Long sendGroupId) {
		this.sendGroupId = sendGroupId;
	}

	public String getSendingMsgType() {
		return sendingMsgType;
	}

	public void setSendingMsgType(String sendingMsgType) {
		this.sendingMsgType = sendingMsgType;
	}

	public String getSendingMsgTime() {
		return sendingMsgTime;
	}

	public void setSendingMsgTime(String sendingMsgTime) {
		this.sendingMsgTime = sendingMsgTime;
	}

	public List<SendMsgDetailModel> getSendMsgDetails() {
		return sendMsgDetails;
	}

	public void setSendMsgDetails(List<SendMsgDetailModel> sendMsgDetails) {
		this.sendMsgDetails = sendMsgDetails;
	}

	public Long getMsgId() {
		return msgId;
	}

	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	public List<String> getMsgTagList() {
		return msgTagList;
	}

	public void setMsgTagList(List<String> msgTagList) {
		this.msgTagList = msgTagList;
	}

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}
	
}
