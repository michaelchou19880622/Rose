package com.bcs.core.db.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_SEND_GROUP_QUERY")
public class SendGroupQuery extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "QUERY_FIELD_ID", columnDefinition="nvarchar(50)")
	private String queryFieldId;

	@Column(name = "QUERY_FIELD_NAME", columnDefinition="nvarchar(50)")
	private String queryFieldName;

	@Column(name = "QUERY_FIELD_OP", columnDefinition="nvarchar(50)")
	private String queryFieldOp;

	@Column(name = "QUERY_FIELD_FORMAT", columnDefinition="nvarchar(50)")
	private String queryFieldFormat;

	@Column(name = "QUERY_FIELD_SET", columnDefinition="nvarchar(50)")
	private String queryFieldSet;

	@OneToMany(mappedBy="sendGroupQuery",cascade=CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval=true)
	@OrderBy("TAG_INDEX ASC")
	private List<SendGroupQueryTag> sendGroupQueryTag;

	public String getQueryFieldId() {
		return queryFieldId;
	}

	public void setQueryFieldId(String queryFieldId) {
		this.queryFieldId = queryFieldId;
	}

	public String getQueryFieldName() {
		return queryFieldName;
	}

	public void setQueryFieldName(String queryFieldName) {
		this.queryFieldName = queryFieldName;
	}

	public String getQueryFieldOp() {
		return queryFieldOp;
	}

	public void setQueryFieldOp(String queryFieldOp) {
		this.queryFieldOp = queryFieldOp;
	}

	public String getQueryFieldFormat() {
		return queryFieldFormat;
	}

	public void setQueryFieldFormat(String queryFieldFormat) {
		this.queryFieldFormat = queryFieldFormat;
	}

	public String getQueryFieldSet() {
		return queryFieldSet;
	}

	public void setQueryFieldSet(String queryFieldSet) {
		this.queryFieldSet = queryFieldSet;
	}

	public List<SendGroupQueryTag> getSendGroupQueryTag() {
		return sendGroupQueryTag;
	}

	public void setSendGroupQueryTag(List<SendGroupQueryTag> sendGroupQueryTag) {
		this.sendGroupQueryTag = sendGroupQueryTag;
	}
}
