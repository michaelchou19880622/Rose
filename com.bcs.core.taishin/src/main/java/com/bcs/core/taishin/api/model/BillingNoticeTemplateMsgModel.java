package com.bcs.core.taishin.api.model;

import java.util.Date;
import java.util.List;

import com.bcs.core.json.AbstractBcsEntity;

public class BillingNoticeTemplateMsgModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private boolean templateSwitch;
	private String altText;
	private String templateType;
	private String templateImageId;
	private String templateTitle;
	private String curfewStartTime;
	private String curfewEndTime;
	private String templateText;
	private String templateLevel;
	private String templateParentId;
	private String templateLetter;
	private List<TemplateActionModel> templateActions;
	
	
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
	
	public String getTemplateLetter() {
		return templateLetter;
	}
	public void setTemplateLetter(String templateLetter) {
		this.templateLetter = templateLetter;
	}
	
	public List<TemplateActionModel> getTemplateActions() {
		return templateActions;
	}
	public void setTemplateActions(List<TemplateActionModel> templateActions) {
		this.templateActions = templateActions;
	}
	public boolean getTemplateSwitch() {
		return templateSwitch;
	}
	public void setTemplateSwitch(boolean templateSwitch) {
		this.templateSwitch = templateSwitch;
	}
	public String getCurfewStartTime() {
		return curfewStartTime;
	}
	public void setCurfewStartTime(String curfewStartTime) {
		this.curfewStartTime = curfewStartTime;
	}
	public String getCurfewEndTime() {
		return curfewEndTime;
	}
	public void setCurfewEndTime(String curfewEndTime) {
		this.curfewEndTime = curfewEndTime;
	}

}
