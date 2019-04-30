package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@Table(name = "BCS_RECORD_REPORT",
indexes = {
	       @Index(name = "INDX_1", columnList = "REFERENCE_ID"),
	       @Index(name = "INDX_2", columnList = "CONTENT_TYPE"),
	       @Index(name = "INDX_3", columnList = "DATA_TYPE"),
	})
public class RecordReport extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "RECORD_ID")
	private Long recordId;
	
	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "RECORD_TIME")
	private Date recordTime;

	@Column(name = "REFERENCE_ID", columnDefinition = "nvarchar(50)")
	private String referenceId;

	@Column(name = "INCREASE_ID", columnDefinition = "nvarchar(1000)")
	private String increaseId;

	@Column(name = "CONTENT_TYPE", columnDefinition = "nvarchar(50)")
	private String contentType;

	@Column(name = "DATA_TYPE", columnDefinition = "nvarchar(50)")
	private String dataType;
	
	@Column(name = "RECORD_COUNT")
	private Long recordCount;

	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public Date getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(Date recordTime) {
		this.recordTime = recordTime;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getIncreaseId() {
		return increaseId;
	}

	public void setIncreaseId(String increaseId) {
		this.increaseId = increaseId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Long recordCount) {
		this.recordCount = recordCount;
	}
}
