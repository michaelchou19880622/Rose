package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_LINK")
public class ContentLink extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "LINK_ID", columnDefinition="nvarchar(50)")
	private String linkId;

	@Column(name = "LINK_TITLE", columnDefinition="nvarchar(1000)")
	private String linkTitle;

	@Column(name = "LINK_URL", columnDefinition="nvarchar(1000)")
	private String linkUrl;

	@Column(name = "LINK_PREVIEW_IMAGE", columnDefinition="nvarchar(50)")
	private String linkPreviewImage;

	@Column(name = "LINK_TAG", columnDefinition="nvarchar(50)")
	private String linkTag;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	public ContentLink(){
		
	}
	
	public ContentLink(String linkId){
		this.linkId = linkId;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public String getLinkTitle() {
		return linkTitle;
	}

	public void setLinkTitle(String linkTitle) {
		this.linkTitle = linkTitle;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}

	public String getLinkPreviewImage() {
		return linkPreviewImage;
	}

	public void setLinkPreviewImage(String linkPreviewImage) {
		this.linkPreviewImage = linkPreviewImage;
	}

	public String getLinkTag() {
		return linkTag;
	}

	public void setLinkTag(String linkTag) {
		this.linkTag = linkTag;
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
	
}
