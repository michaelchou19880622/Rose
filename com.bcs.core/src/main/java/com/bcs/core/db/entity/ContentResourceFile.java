package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_RESOURCE_FILE")
public class ContentResourceFile extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "RESOURCE_ID", columnDefinition="nvarchar(50)")
	private String resourceId;
	
    @Lob
//    @Column(name="FILE_DATA", columnDefinition="mediumblob") // MYSQL Difference
    @Column(name="FILE_DATA")
    private byte[] fileData;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	public ContentResourceFile(){
		
	}
	
	public ContentResourceFile(String resourceId){
		this.resourceId = resourceId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
