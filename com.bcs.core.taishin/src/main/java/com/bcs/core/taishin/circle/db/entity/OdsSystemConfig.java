package com.bcs.core.taishin.circle.db.entity;

import java.util.Date;

import org.springframework.stereotype.Component;

@Component
public class OdsSystemConfig{
	private static final long serialVersionUID = 1L;
	
	private String configId;

	private String value;

	private String description;

	private Date modifyTime;

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
}
