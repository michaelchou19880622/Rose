package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateSerializer;
import com.bcs.core.utils.ObjectUtil;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "BCS_USER_EVENT_SET",
	indexes = {
	       @Index(name = "INDEX_0", columnList = "TARGET"),
	       @Index(name = "INDEX_1", columnList = "ACTION"),
	       @Index(name = "INDEX_2", columnList = "MID") ,
	       @Index(name = "INDEX_3", columnList = "REFERENCE_ID") ,
	})
public class UserEventSet extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "EVENT_ID")
	private Long eventId;

	@Column(name = "TARGET", columnDefinition="nvarchar(50)")
	private String target;

	@Column(name = "ACTION", columnDefinition="nvarchar(50)")
	private String action;

	@Column(name = "MID", columnDefinition="nvarchar(50)")
	private String mid;
	
	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "SET_TIME")
	private Date setTime;

	@Column(name = "CONTENT", columnDefinition="nvarchar(1000)")
	private String content;
	
	@Column(name = "REFERENCE_ID", columnDefinition="nvarchar(200)")
	private String referenceId;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		if(StringUtils.isNotBlank(this.content) && this.content.length() > 1000){
			this.content = this.content.substring(0, 999);
		}
	}

	public void setContent(Object content) {
		this.content = ObjectUtil.objectToJsonStr(content);
		if(this.content.length() > 1000){
			this.content = this.content.substring(0, 999);
		}
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
		if(StringUtils.isNotBlank(this.referenceId) && this.referenceId.length() > 200){
			this.referenceId = this.referenceId.substring(0, 199);
		}
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public Date getSetTime() {
		return setTime;
	}

	public void setSetTime(Date setTime) {
		this.setTime = setTime;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}
	
}
