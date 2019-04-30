package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "BCS_CONTENT_LINK_TRACING")
public class ContentLinkTracing extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "TRACING_ID")
	private Long tracingId;

	@Column(name = "LINK_ID", columnDefinition="nvarchar(50)")
	private String linkId;

	@Column(name = "LINK_ID_BINDED", columnDefinition="nvarchar(50)")
	private String linkIdBinded;

	@Column(name = "LINK_ID_UNMOBILE", columnDefinition="nvarchar(50)")
	private String linkIdUnMobile;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	public ContentLinkTracing(){
		
	}
	
	public Long getTracingId() {
		return tracingId;
	}

	public void setTracingId(Long tracingId) {
		this.tracingId = tracingId;
	}

	public ContentLinkTracing(String linkId){
		this.linkId = linkId;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
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

	public String getLinkIdBinded() {
		return linkIdBinded;
	}

	public void setLinkIdBinded(String linkIdBinded) {
		this.linkIdBinded = linkIdBinded;
	}

	public String getLinkIdUnMobile() {
		return linkIdUnMobile;
	}

	public void setLinkIdUnMobile(String linkIdUnMobile) {
		this.linkIdUnMobile = linkIdUnMobile;
	}
	
}
