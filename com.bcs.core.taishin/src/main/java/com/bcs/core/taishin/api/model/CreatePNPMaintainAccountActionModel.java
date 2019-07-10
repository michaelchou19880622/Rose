package com.bcs.core.taishin.api.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import java.util.List;

public class CreatePNPMaintainAccountActionModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	private String pathway; //通路流
	private String template; //樣板ID
	private String pnpContent; //簡訊內容
	public String getPathway() {
		return pathway;
	}
	public void setPathway(String pathway) {
		this.pathway = pathway;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getPnpContent() {
		return pnpContent;
	}
	public void setPnpContent(String pnpContent) {
		this.pnpContent = pnpContent;
	}
}
