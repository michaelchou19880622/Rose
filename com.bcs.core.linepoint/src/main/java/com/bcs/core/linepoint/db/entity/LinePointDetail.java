package com.bcs.core.linepoint.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.json.JSONArray;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.utils.ObjectUtil;

@Entity
@Table(name = "BCS_LINE_POINT_DETAIL")
public class LinePointDetail extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	//public static final String STATUS_WAIT = "WAIT";
	public static final String STATUS_SUCCESS = "SUCCESS";
	public static final String STATUS_FAIL = "FAIL";
	public static final String TRANSCATION_TYPE_ISSUE = "ISSUE";
	public static final String DESCRIPTION_OVERFLOW = "OVERFLOW";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "DETAIL_ID")
	private Long detailId;

	@Column(name = "LINE_POINT_MAIN_ID")
	private Long linePointMainId;

	// Post Model
	@Column(name = "UID", columnDefinition="varchar(50)")
	private String uid;
	@Column(name = "ORDER_KEY", columnDefinition="varchar(50)")
	private String orderKey;
	@Column(name = "APPLICATION_TIME")
	private Long applicationTime;
	@Column(name = "AMOUNT")
	private Long amount;
	
	// Push Information
	@Column(name = "SOURCE", columnDefinition="varchar(10)")
	private String source;
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	@Column(name = "TRIGGER_TIME")
	private Date triggerTime;
	@Column(name = "SEND_TIME")
	private Date sendTime;

	// Response Model
	@Column(name = "DESCRIPTION", columnDefinition="nvarchar(200)")
	private String description;
	@Column(name = "TRANSCATION_ID", columnDefinition="varchar(20)")
	private String transcationId;
	@Column(name = "TRANSCATION_TIME")
	private Long transcationTime;
	@Column(name = "TRANSCATION_TYPE", columnDefinition="varchar(50)")
	private String transcationType;	
	@Column(name = "TRANSCATION_AMOUNT")
	private Integer transactionAmount;
	@Column(name = "TRANSCATION_BALANCE")
	private Integer transactionBalance;
	
	public Long getDetailId() {
		return detailId;
	}
	public Long getLinePointMainId() {
		return linePointMainId;
	}
	public void setLinePointMainId(Long linePointMainId) {
		this.linePointMainId = linePointMainId;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getOrderKey() {
		return orderKey;
	}
	public void setOrderKey(String orderKey) {
		this.orderKey = orderKey;
	}
	public Long getApplicationTime() {
		return applicationTime;
	}
	public void setApplicationTime(Long applicationTime) {
		this.applicationTime = applicationTime;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Date getTriggerTime() {
		return triggerTime;
	}
	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getTranscationId() {
		return transcationId;
	}
	public void setTranscationId(String transcationId) {
		this.transcationId = transcationId;
	}
	public Long getTranscationTime() {
		return transcationTime;
	}
	public void setTranscationTime(Long transcationTime) {
		this.transcationTime = transcationTime;
	}
	public String getTranscationType() {
		return transcationType;
	}
	public void setTranscationType(String transcationType) {
		this.transcationType = transcationType;
	}
	public Integer getTransactionAmount() {
		return transactionAmount;
	}
	public void setTransactionAmount(Integer transactionAmount) {
		this.transactionAmount = transactionAmount;
	}
	public Integer getTransactionBalance() {
		return transactionBalance;
	}
	public void setTransactionBalance(Integer transactionBalance) {
		this.transactionBalance = transactionBalance;
	}
}
