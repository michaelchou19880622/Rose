package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "BCS_SERIAL_SETTING")
public class SerialSetting extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String SERIAL_LEVEL_MAIN = "MAIN";
	public static final String SERIAL_LEVEL_COLUMN = "COLUMN";

	public static final String SERIAL_STATUS_DELETE = "DELETE";
	public static final String SERIAL_STATUS_ACTIVE = "ACTIVE";
	
	@Id
	@Column(name = "SERIAL_ID", columnDefinition="nvarchar(50)")
	private String serialId;

	@Column(name = "SERIAL_LEVEL", columnDefinition="nvarchar(50)")
	private String serialLevel;

	@Column(name = "SERIAL_PARENT_ID", columnDefinition="nvarchar(50)")
	private String serialParentId;

	@Column(name = "SERIAL_TITLE", columnDefinition="nvarchar(50)")
	private String serialTitle;

	@Column(name = "SERIAL_DESCRIPTION", columnDefinition="nvarchar(1000)")
	private String serialDescription;

	@Column(name = "SERIAL_COUNT")
	private int serialCount;

	@Column(name = "SERIAL_TARGET", columnDefinition="nvarchar(50)")
	private String serialTarget;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}

	public String getSerialLevel() {
		return serialLevel;
	}

	public void setSerialLevel(String serialLevel) {
		this.serialLevel = serialLevel;
	}

	public String getSerialParentId() {
		return serialParentId;
	}

	public void setSerialParentId(String serialParentId) {
		this.serialParentId = serialParentId;
	}

	public String getSerialTitle() {
		return serialTitle;
	}

	public void setSerialTitle(String serialTitle) {
		this.serialTitle = serialTitle;
	}

	public String getSerialDescription() {
		return serialDescription;
	}

	public void setSerialDescription(String serialDescription) {
		this.serialDescription = serialDescription;
	}

	public int getSerialCount() {
		return serialCount;
	}

	public void setSerialCount(int serialCount) {
		this.serialCount = serialCount;
	}

	public String getSerialTarget() {
		return serialTarget;
	}

	public void setSerialTarget(String serialTarget) {
		this.serialTarget = serialTarget;
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
	
}
