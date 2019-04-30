package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_TEMPLATE")
public class ContentTemplateMsg extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String TEMPLATE_LEVEL_MAIN = "MAIN";
	// For Carousel Type
	public static final String TEMPLATE_LEVEL_COLUMN = "COLUMN";
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETE = "DELETE";
	public static final String STATUS_AUTOREPLY = "AUTOREPLY";

	public static final String TEMPLATE_TYPE_BUTTONS = "buttons";
	public static final String TEMPLATE_TYPE_CONFIRM = "confirm";
	public static final String TEMPLATE_TYPE_CAROUSEL = "carousel";
	
	@Id
	@Column(name = "TEMPLATE_ID", columnDefinition="nvarchar(50)")
	private String templateId;

	@Column(name = "ALT_TEXT", columnDefinition="nvarchar(500)")
	private String altText;

	@Column(name = "TEMPLATE_TYPE", columnDefinition="nvarchar(50)")
	private String templateType;

	@Column(name = "TEMPLATE_LEVEL", columnDefinition="nvarchar(50)")
	private String templateLevel;

	@Column(name = "TEMPLATE_PARENT_ID", columnDefinition="nvarchar(50)")
	private String templateParentId;

	@Column(name = "TEMPLATE_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String templateImageId;

	@Column(name = "TEMPLATE_TITLE", columnDefinition="nvarchar(200)")
	private String templateTitle;

	@Column(name = "TEMPLATE_TEXT", columnDefinition="nvarchar(500)")
	private String templateText;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "TEMPLATE_LETTER", columnDefinition="nvarchar(5)")
	private String templateLetter;

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTemplateLevel() {
		return templateLevel;
	}

	public void setTemplateLevel(String templateLevel) {
		this.templateLevel = templateLevel;
	}

	public String getTemplateParentId() {
		return templateParentId;
	}

	public void setTemplateParentId(String templateParentId) {
		this.templateParentId = templateParentId;
	}

	public String getAltText() {
		return altText;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}

	public String getTemplateImageId() {
		return templateImageId;
	}

	public void setTemplateImageId(String templateImageId) {
		this.templateImageId = templateImageId;
	}

	public String getTemplateTitle() {
		return templateTitle;
	}

	public void setTemplateTitle(String templateTitle) {
		this.templateTitle = templateTitle;
	}

	public String getTemplateText() {
		return templateText;
	}

	public void setTemplateText(String templateText) {
		this.templateText = templateText;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTemplateLetter() {
		return templateLetter;
	}

	public void setTemplateLetter(String templateLetter) {
		this.templateLetter = templateLetter;
	}
}
