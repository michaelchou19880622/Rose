package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_RICH")
public class ContentRichMsg extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String STATUS_ACTIVE = "ACTIVE";
	public static final String STATUS_DELETE = "DELETE";
	
	@Id
	@Column(name = "RICH_ID", columnDefinition="nvarchar(50)")
	private String richId;

	@Column(name = "RICH_TITLE", columnDefinition="nvarchar(100)")
	private String richTitle;

	@Column(name = "RICH_TYPE", columnDefinition="nvarchar(50)")
	private String richType;

	@Column(name = "RICH_IMAGE_ID", columnDefinition="nvarchar(50)")
	private String richImageId;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

	public String getRichId() {
		return richId;
	}

	public void setRichId(String richId) {
		this.richId = richId;
	}

	public String getRichTitle() {
		return richTitle;
	}

	public void setRichTitle(String richTitle) {
		this.richTitle = richTitle;
	}

	public String getRichType() {
		return richType;
	}

	public void setRichType(String richType) {
		this.richType = richType;
	}

	public String getRichImageId() {
		return richImageId;
	}

	public void setRichImageId(String richImageId) {
		this.richImageId = richImageId;
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
