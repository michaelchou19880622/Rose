package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_TEMPLATE_ACTION",
indexes = {
	       @Index(name = "INDEX_0", columnList = "TEMPLATE_ID"),
	       @Index(name = "INDEX_1", columnList = "LINK_ID"),
	})
public class ContentTemplateMsgAction extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETE = "DELETE";
	public static final String STATUS_AUTOREPLY = "AUTOREPLY";

	public static final String ACTION_TYPE_POSTBACK = "postback";
	public static final String ACTION_TYPE_MESSAGE = "message";
	public static final String ACTION_TYPE_URI = "uri";
	
	@Id
	@Column(name = "TEMPLATE_ID_ACTION", columnDefinition="nvarchar(50)")
	private String templateIdAction;

	@Column(name = "TEMPLATE_ID", columnDefinition="nvarchar(50)")
	private String templateId;
	
	@Column(name = "ACTION_LETTER", columnDefinition="nvarchar(5)")
	private String actionLetter;
	
	@Column(name = "ACTION_TYPE", columnDefinition="nvarchar(50)")
	private String actionType;

	@Column(name = "ACTION_LABEL", columnDefinition="nvarchar(50)")
	private String actionLabel;

	@Column(name = "ACTION_DATA", columnDefinition="nvarchar(500)")
	private String actionData;

	@Column(name = "ACTION_TEXT", columnDefinition="nvarchar(500)")
	private String actionText;

	@Column(name = "LINK_ID", columnDefinition="nvarchar(50)")
	private String linkId;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

	public String getTemplateIdAction() {
		return templateIdAction;
	}

	public void setTemplateIdAction(String templateIdAction) {
		this.templateIdAction = templateIdAction;
	}
	
	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

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

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}
