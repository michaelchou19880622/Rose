package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_RESOURCE")
public class ContentResource extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String RESOURCE_TYPE_IMAGE = "IMAGE";
	public static final String RESOURCE_TYPE_VIDEO = "VIDEO";
	public static final String RESOURCE_TYPE_AUDIO = "AUDIO";

	public static final String RESOURCE_TYPE_RECEIVEIMAGE = "RECEIVEIMAGE";
	public static final String RESOURCE_TYPE_QRIMAGE = "QRIMAGE";
	
	@Id
	@Column(name = "RESOURCE_ID", columnDefinition="nvarchar(50)")
	private String resourceId;

	@Column(name = "RESOURCE_TYPE", columnDefinition="nvarchar(50)")
	private String resourceType;

	@Column(name = "CONTENT_TYPE", columnDefinition="nvarchar(50)")
	private String contentType;

	@Column(name = "RESOURCE_TITLE", columnDefinition="nvarchar(50)")
	private String resourceTitle;

	@Column(name = "RESOURCE_SIZE")
	private Long resourceSize;
	
	@Column(name = "RESOURCE_WIDTH")
	private Long resourceWidth;
	
	@Column(name = "RESOURCE_HEIGHT")
	private Long resourceHeight;

	@Column(name = "RESOURCE_LENGTH")
	private Integer resourceLength;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "USE_FLAG")
	private boolean useFlag = false;

	@Column(name = "RESOURCE_PREVIEW", columnDefinition="nvarchar(50)")
	private String resourcePreview;
	
	public ContentResource(){
		
	}
	
	public ContentResource(String resourceId){
		this.resourceId = resourceId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getResourceTitle() {
		return resourceTitle;
	}

	public void setResourceTitle(String resourceTitle) {
		this.resourceTitle = resourceTitle;
	}

	public Long getResourceSize() {
		return resourceSize;
	}

	public void setResourceSize(Long resourceSize) {
		this.resourceSize = resourceSize;
	}

	public Long getResourceWidth() {
		return resourceWidth;
	}

	public void setResourceWidth(Long resourceWidth) {
		this.resourceWidth = resourceWidth;
	}

	public Long getResourceHeight() {
		return resourceHeight;
	}

	public void setResourceHeight(Long resourceHeight) {
		this.resourceHeight = resourceHeight;
	}

	public Integer getResourceLength() {
		return resourceLength;
	}

	public void setResourceLength(Integer resourceLength) {
		this.resourceLength = resourceLength;
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

	public boolean isUseFlag() {
		return useFlag;
	}

	public void setUseFlag(boolean useFlag) {
		this.useFlag = useFlag;
	}

	public String getResourcePreview() {
		return resourcePreview;
	}

	public void setResourcePreview(String resourcePreview) {
		this.resourcePreview = resourcePreview;
	}
	
}
