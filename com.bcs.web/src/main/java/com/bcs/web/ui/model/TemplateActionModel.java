package com.bcs.web.ui.model;

import com.bcs.core.json.AbstractBcsEntity;

public class TemplateActionModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private String actionLetter;
	private String actionType;
	private String actionLabel;
	private String actionData;
	private String actionText;
	
	public String getActionLetter() {
		return actionLetter;
	}
	public void setActionLetter(String actionLetter) {
		this.actionLetter = actionLetter;
	}
	
	public String getActionType() {
		return actionType;
	}
	public void setActionType(String actionType) {
		this.actionType = actionType;
	}
	
	public String getActionLabel() {
		return actionLabel;
	}
	public void setActionLabel(String actionLabel) {
		this.actionLabel = actionLabel;
	}
	
	public String getActionData() {
		return actionData;
	}
	public void setActionData(String actionData) {
		this.actionData = actionData;
	}
	
	public String getActionText() {
		return actionText;
	}
	public void setActionText(String actionText) {
		this.actionText = actionText;
	}
}
