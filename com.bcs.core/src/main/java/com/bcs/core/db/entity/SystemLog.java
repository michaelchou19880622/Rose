package com.bcs.core.db.entity;

import java.util.Date;
import java.util.List;

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
@Table(name = "BCS_SYSTEM_LOG",
indexes = {
	       @Index(name = "INDEX_0", columnList = "TARGET"),
	       @Index(name = "INDEX_1", columnList = "ACTION"),
	       @Index(name = "INDEX_2", columnList = "LEVEL") ,
	       @Index(name = "INDEX_4", columnList = "MODIFY_USER") ,
	       @Index(name = "INDEX_5", columnList = "REFERENCE_ID") ,
	       @Index(name = "INDEX_5", columnList = "MODIFY_DAY") ,
	})
public class SystemLog extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	public static final String SYSTEM_EVENT = "SYSTEM";
	
	public static final String SYSTEM_LOG_LEVEL_ERROR = "ERROR";
	public static final String SYSTEM_LOG_LEVEL_INFO = "INFO";
	public static final String SYSTEM_LOG_LEVEL_DEBUG = "DEBUG";
	public static final String SYSTEM_LOG_LEVEL_WARN = "WARN";
	public static final String SYSTEM_LOG_LEVEL_TEST = "TEST";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "LOG_ID")
	private Long logId;

	@Column(name = "TARGET", columnDefinition="nvarchar(50)")
	private String target;

	@Column(name = "ACTION", columnDefinition="nvarchar(50)")
	private String action;

	@Column(name = "LEVEL", columnDefinition="nvarchar(50)")
	private String level;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;
	
	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "CONTENT", columnDefinition="nvarchar(1000)")
	private String content;
	
	@Column(name = "REFERENCE_ID", columnDefinition="nvarchar(200)")
	private String referenceId;

	@Column(name = "MODIFY_DAY", columnDefinition="nvarchar(10)")
	private String modifyDay;

	@Column(name = "SYSTEM_IP", columnDefinition="nvarchar(50)")
	private String systemIp;

	public Long getLogId() {
		return logId;
	}

	public void setLogId(Long logId) {
		this.logId = logId;
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

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		if(StringUtils.isNotBlank(this.content) && this.content.length() > 1000){
			this.content = this.content.substring(0, 999);
		}
	}

	@SuppressWarnings("unchecked")
	public void setContent(Object content) {
		if(content instanceof List){
			List<Object> list = (List<Object>) content;
			this.content = ObjectUtil.listObjectToString(list);
		}
		else{
			this.content = ObjectUtil.objectToJsonStr(content);
		}
		
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

	public String getModifyDay() {
		return modifyDay;
	}

	public void setModifyDay(String modifyDay) {
		this.modifyDay = modifyDay;
	}

	public String getSystemIp() {
		return systemIp;
	}

	public void setSystemIp(String systemIp) {
		this.systemIp = systemIp;
	}
}
