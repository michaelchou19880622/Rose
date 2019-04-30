package com.bcs.core.db.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_CONTENT_FLAG")
public class ContentFlag extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	/**
	 * 內容類型：優惠劵
	 */
	public static final String CONTENT_TYPE_COUPON = "COUPON";
	/**
	 * 內容類型：訊息主檔
	 */
	public static final String CONTENT_TYPE_MSG_SEND = "MSG_SEND";
	/**
	 * 內容類型：連結
	 */
	public static final String CONTENT_TYPE_LINK = "LINK";

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "FLAG_VALUE", columnDefinition = "nvarchar(200)")
	private String flagValue;

	@Column(name = "CONTENT_TYPE", columnDefinition = "nvarchar(50)")
	private String contentType;

	@Column(name = "REFERENCE_ID", columnDefinition = "nvarchar(50)")
	private String referenceId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFlagValue() {
		return flagValue;
	}

	public void setFlagValue(String flagValue) {
		this.flagValue = flagValue;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

}
