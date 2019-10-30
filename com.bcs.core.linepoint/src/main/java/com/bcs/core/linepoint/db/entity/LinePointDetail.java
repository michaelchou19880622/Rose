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

	public static final String STATUS_WAIT = "WAIT";
 	public static final String STATUS_SUCCESS = "SUCCESS";
 	public static final String STATUS_FAIL = "FAIL";
 	
	public static final String DETAIL_TYPE_ISSUE_API = "ISSUE_API";
	public static final String DETAIL_TYPE_CANCEL_API = "CANCEL_API";
	public static final String DETAIL_TYPE_ISSUE_BCS = "ISSUE_BCS";
	public static final String DETAIL_TYPE_CANCEL_BCS = "CANCEL_BCS";
	
 	public static final String DESCRIPTION_OVERFLOW = "OVERFLOW";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "DETAIL_ID")
	private Long detailId;
	@Column(name = "LINE_POINT_MAIN_ID")
	private Long linePointMainId;
	@Column(name = "DETAIL_TYPE", columnDefinition="varchar(20)")
	private String detailType;
	
	// API Request Data
	@Column(name = "DEPARTMENT", columnDefinition="nvarchar(50)")
	private String department;
	@Column(name = "SERVICE_NAME", columnDefinition="nvarchar(50)")
	private String serviceName;
	@Column(name = "PCC_CODE", columnDefinition="nvarchar(50)")
	private String pccCode;
	@Column(name = "CAMP_NAME", columnDefinition="nvarchar(50)")
	private String campName;
	@Column(name = "CUSTID", columnDefinition="nvarchar(20)")
	private String custid;
	@Column(name = "NOTE", columnDefinition="nvarchar(100)")
	private String note;
	@Column(name = "CANCEL_TRANSCATION_ID", columnDefinition="varchar(20)")
	private String cancelTranscationId;	

	// Push Information
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	@Column(name = "TRIGGER_TIME")
	private Date triggerTime;
	@Column(name = "SEND_TIME")
	private Date sendTime;
	
	// Post Model
	@Column(name = "UID", columnDefinition="varchar(50)")
	private String uid;
	@Column(name = "ORDER_KEY", columnDefinition="varchar(50)")
	private String orderKey;
	@Column(name = "APPLICATION_TIME")
	private Long applicationTime;
	@Column(name = "AMOUNT")
	private Long amount;
	@Column(name = "IS_MEMBER")
	private Long isMember;
	
	// Success Response Model
	@Column(name = "TRANSCATION_ID", columnDefinition="varchar(20)")
	private String transcationId;
	@Column(name = "TRANSCATION_TIME")
	private Long transcationTime;
	@Column(name = "TRANSCATION_TYPE", columnDefinition="varchar(50)")
	private String transcationType;	
	@Column(name = "TRANSCATION_AMOUNT")
	private Integer transactionAmount;
	@Column(name = "CANCELLED_AMOUNT")
	private Integer cancelledAmount;
	@Column(name = "REMAINING_AMOUNT")
	private Integer remainingAmount;
	@Column(name = "BALANCE")
	private Integer balance;
	
	// Fail Response Model
	@Column(name = "ERROR", columnDefinition="nvarchar(50)")
	private String error;
	@Column(name = "MESSAGE", columnDefinition="nvarchar(200)")
	private String message;
	@Column(name = "DETAILS", columnDefinition="nvarchar(200)") // optional response
	private String details;
	
	public Long getDetailId() {
		return detailId;
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
	public String getTranscationId() {
		return transcationId;
	}
	public void setIsMember(Long isMember) {
		this.isMember = isMember;
	}
	public Long getIsMember() {
		return isMember;
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
	public Integer getBalance() {
		return balance;
	}
	public void setBalance(Integer balance) {
		this.balance = balance;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getPccCode() {
		return pccCode;
	}
	public void setPccCode(String pccCode) {
		this.pccCode = pccCode;
	}
	public String getCampName() {
		return campName;
	}
	public void setCampName(String campName) {
		this.campName = campName;
	}
	public String getCustid() {
		return custid;
	}
	public void setCustid(String custid) {
		this.custid = custid;
	}
	public void setDetailId(Long detailId) {
		this.detailId = detailId;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
	public String getDetailType() {
		return detailType;
	}
	public void setDetailType(String detailType) {
		this.detailType = detailType;
	}
	public Long getLinePointMainId() {
		return linePointMainId;
	}
	public void setLinePointMainId(Long linePointMainId) {
		this.linePointMainId = linePointMainId;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Integer getCancelledAmount() {
		return cancelledAmount;
	}
	public void setCancelledAmount(Integer cancelledAmount) {
		this.cancelledAmount = cancelledAmount;
	}
	public Integer getRemainingAmount() {
		return remainingAmount;
	}
	public void setRemainingAmount(Integer remainingAmount) {
		this.remainingAmount = remainingAmount;
	}
	public String getCancelTranscationId() {
		return cancelTranscationId;
	}
	public void setCancelTranscationId(String cancelTranscationId) {
		this.cancelTranscationId = cancelTranscationId;
	}
}
