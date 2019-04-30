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
@Table(name = "BCS_SEND_GROUP_DETAIL",
indexes = {
	       @Index(name = "INDEX_0", columnList = "GROUP_ID"),
	})
public class SendGroupDetail extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "GROUP_DETAIL_ID")
	private Long groupDetailId;
	
	@JoinColumn(name = "GROUP_ID", nullable = false)
	@ManyToOne(targetEntity = SendGroup.class, fetch = FetchType.EAGER)
	@JsonIgnore
	private SendGroup sendGroup;

	@Column(name = "QUERY_FIELD", columnDefinition="nvarchar(50)")
	private String queryField;

	@Column(name = "QUERY_OP", columnDefinition="nvarchar(50)")
	private String queryOp;

	@Column(name = "QUERY_VALUE", columnDefinition="nvarchar(50)")
	private String queryValue;

	public Long getGroupDetailId() {
		return groupDetailId;
	}

	public void setGroupDetailId(Long groupDetailId) {
		this.groupDetailId = groupDetailId;
	}

	public SendGroup getSendGroup() {
		return sendGroup;
	}

	public void setSendGroup(SendGroup sendGroup) {
		this.sendGroup = sendGroup;
	}

	public String getQueryField() {
		return queryField;
	}

	public void setQueryField(String queryField) {
		this.queryField = queryField;
	}

	public String getQueryOp() {
		return queryOp;
	}

	public void setQueryOp(String queryOp) {
		this.queryOp = queryOp;
	}

	public String getQueryValue() {
		return queryValue;
	}

	public void setQueryValue(String queryValue) {
		this.queryValue = queryValue;
	}
	
}
