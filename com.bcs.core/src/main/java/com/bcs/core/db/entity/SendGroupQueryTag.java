package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "BCS_SEND_GROUP_QUERY_TAG",
indexes = {
	       @Index(name = "INDEX_0", columnList = "QUERY_FIELD_ID"),
	})
public class SendGroupQueryTag extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "QUERY_FIELD_TAG_ID")
	private Long queryFieldTagId;
	
	@JoinColumn(name = "QUERY_FIELD_ID", nullable = false)
	@ManyToOne(targetEntity = SendGroupQuery.class, fetch = FetchType.EAGER)
	@JsonIgnore
	private SendGroupQuery sendGroupQuery;

	@Column(name = "QUERY_FIELD_TAG_Value", columnDefinition="nvarchar(50)")
	private String queryFieldTagValue;

	@Column(name = "QUERY_FIELD_TAG_DISPLAY", columnDefinition="nvarchar(50)")
	private String queryFieldTagDisplay;

	@Column(name = "TAG_INDEX")
	private Integer tagIndex;

	public Long getQueryFieldTagId() {
		return queryFieldTagId;
	}

	public void setQueryFieldTagId(Long queryFieldTagId) {
		this.queryFieldTagId = queryFieldTagId;
	}

	public SendGroupQuery getSendGroupQuery() {
		return sendGroupQuery;
	}

	public void setSendGroupQuery(SendGroupQuery sendGroupQuery) {
		this.sendGroupQuery = sendGroupQuery;
	}

	public String getQueryFieldTagValue() {
		return queryFieldTagValue;
	}

	public void setQueryFieldTagValue(String queryFieldTagValue) {
		this.queryFieldTagValue = queryFieldTagValue;
	}

	public String getQueryFieldTagDisplay() {
		return queryFieldTagDisplay;
	}

	public void setQueryFieldTagDisplay(String queryFieldTagDisplay) {
		this.queryFieldTagDisplay = queryFieldTagDisplay;
	}

	public Integer getTagIndex() {
		return tagIndex;
	}

	public void setTagIndex(Integer tagIndex) {
		this.tagIndex = tagIndex;
	}
}
