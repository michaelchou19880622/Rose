package com.bcs.core.linepoint.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateSerializer;

@Entity
@Table(name = "BCS_LINE_POINT_MSG_CONTENT_LINK_TRACING")
public class LinePointMsgContentLinkTracing extends AbstractBcsEntity{
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

	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	public LinePointMsgContentLinkTracing(){
		
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

    public Long getTracingId() {
		return tracingId;
	}

	public void setTracingId(Long tracingId) {
		this.tracingId = tracingId;
	}

	public LinePointMsgContentLinkTracing(String linkId){
		this.linkId = linkId;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
	
	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
}
