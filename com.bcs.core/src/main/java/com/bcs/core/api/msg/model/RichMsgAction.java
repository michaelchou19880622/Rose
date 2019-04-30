package com.bcs.core.api.msg.model;

import com.bcs.core.json.AbstractBcsEntity;

public class RichMsgAction extends AbstractBcsEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String ACTION_TYPE_WEB = "web";
	public static final String ACTION_TYPE_SEND_MESSAGE = "sendMessage";

	private String actionId;
	private String actionType = ACTION_TYPE_WEB;
	private String actionText;
	private String actionContent;
	private int actionX;
	private int actionY;
	private int actionWidth;
	private int actionHeight;
	
	public RichMsgAction(String actionId, String actionText, String actionContent, int actionX, int actionY, int actionWidth, int actionHeight){

		this.actionId = actionId;
		this.setActionType(ACTION_TYPE_WEB);
		this.actionText = actionText;
		this.setActionContent(actionContent);
		this.actionX = actionX;
		this.actionY = actionY;
		this.actionWidth = actionWidth;
		this.actionHeight = actionHeight;
	}
	
	public RichMsgAction(String actionId, String actionType, String actionText, String actionContent, int actionX, int actionY, int actionWidth, int actionHeight){

		this.actionId = actionId;
		this.setActionType(actionType);
		this.actionText = actionText;
		this.setActionContent(actionContent);
		this.actionX = actionX;
		this.actionY = actionY;
		this.actionWidth = actionWidth;
		this.actionHeight = actionHeight;
	}
	
	public RichMsgAction(){
		
	}
	
	public String getActionId() {
		return actionId;
	}
	public void setActionId(String actionId) {
		this.actionId = actionId;
	}
	public String getActionText() {
		return actionText;
	}
	public void setActionText(String actionText) {
		this.actionText = actionText;
	}
	public int getActionX() {
		return actionX;
	}
	public void setActionX(int actionX) {
		this.actionX = actionX;
	}
	public int getActionY() {
		return actionY;
	}
	public void setActionY(int actionY) {
		this.actionY = actionY;
	}
	public int getActionWidth() {
		return actionWidth;
	}
	public void setActionWidth(int actionWidth) {
		this.actionWidth = actionWidth;
	}
	public int getActionHeight() {
		return actionHeight;
	}
	public void setActionHeight(int actionHeight) {
		this.actionHeight = actionHeight;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getActionContent() {
		return actionContent;
	}

	public void setActionContent(String actionContent) {
		this.actionContent = actionContent;
	}
	
}
