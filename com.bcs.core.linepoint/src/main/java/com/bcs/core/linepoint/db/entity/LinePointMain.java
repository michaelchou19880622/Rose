package com.bcs.core.linepoint.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import com.bcs.core.json.AbstractBcsEntity;


//@SqlResultSetMapping(name = "LinePointSendMainDetails", entities = {
//		@EntityResult(entityClass = LinePointSendMain.class),
//		@EntityResult(entityClass = LinePointSend.class)
//})
//
//@NamedNativeQueries({
//		@NamedNativeQuery(name = "queryLinePointSendMainDetailAll",   query = "SELECT BCS_LINE_POINT_SEND_MAIN.MSG_ID, BCS_LINE_POINT_SEND_MAIN.MSG_LP_ID, SERIAL_ID , MODIFY_TIME, MODIFY_USER, SCHEDULE_TIME, SEND_TYPE, BCS_LINE_POINT_SEND_MAIN.STATUS, STATUS_NOTICE, BCS_LINE_POINT_SEND_MAIN.SEND_TIME, SEND_TOTAL,SEND_SUCCESS,SEND_BLOCKED, SEND_ID, BCS_LINE_POINT_SEND.MSG_LP_ID, UID, MAIN_ID, BCS_LINE_POINT_SEND.STATUS, SOURCE, DESCRIPTION, RESPONSE_CODE FROM BCS_LINE_POINT_SEND_MAIN LEFT OUTER JOIN BCS_LINE_POINT_SEND ON BCS_LINE_POINT_SEND_MAIN.MSG_ID = BCS_LINE_POINT_SEND.MAIN_ID WHERE BCS_LINE_POINT_SEND_MAIN.STATUS <>'DELETE' ORDER BY BCS_LINE_POINT_SEND_MAIN.MSG_ID desc ", resultSetMapping = "LinePointSendMainDetails"),
//})

@Entity
@Table(name = "BCS_LINE_POINT_MAIN")
public class LinePointMain extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	public static final String SEND_TYPE_BCS = "BCS"; // FOR BCS
	public static final String SEND_TYPE_API = "API"; // FOR API
	
	public static final String SEND_TIMING_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SEND_TIMING_TYPE_SCHEDULE = "SCHEDULE";

	public static final String SEND_AMOUNT_TYPE_UNIVERSAL = "UNIVERSAL";
	public static final String SEND_AMOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
	
	public static final String STATUS_DELETE = "DELETE";
	public static final String STATUS_IDLE = "IDLE"; 		   // weather immediate or schedule
	public static final String STATUS_SCHEDULED = "SCHEDULED"; // begin to start
	public static final String STATUS_COMPLETE = "COMPLETE";
	public static final String STATUS_ROTTEN = "ROTTEN";
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;
	@Column(name = "SEND_TYPE", columnDefinition="varchar(50)") // BCS/API
	private String sendType;
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	
	@Column(name = "TITLE", columnDefinition="nvarchar(50)")
	private String title;
	@Column(name = "PCC_CODE", columnDefinition="nvarchar(50)")
	private String pccCode;
	@Column(name = "SERIAL_ID", columnDefinition="nvarchar(50)")
	private String serialId;
	@Column(name = "SEND_TIMING_TYPE", columnDefinition="varchar(50)") // IMMEDIATE/SCHEDULE
	private String sendTimingType;
	@Column(name = "SEND_TIMING_TIME")  // only for sendTimingType = SCHEDULE
	private Date sendTimingTime;
	@Column(name = "SEND_AMOUNT_TYPE", columnDefinition="varchar(50)") // INDIVIDUAL/UNIVERSAL
	private String sendAmountType;	
	@Column(name = "AMOUNT")     // only for sendAmountType = UNIVERSAL
	private Long amount;
	@Column(name = "DO_CHECK_FOLLOWAGE")
	private Boolean doCheckFollowage;
	@Column(name = "DO_APPEND_MESSAGE")
	private Boolean doAppendMessage;
	@Column(name = "APPEND_MESSAGE_ID")
	private Long appendMessageId;
	@Column(name = "LINE_POINT_SEND_GROUP_ID")
	private Long linePointSendGroupId;
	
	@Column(name = "TOTAL_COUNT")
	private Long totalCount;
	@Column(name = "TOTAL_AMOUNT")
	private Long totalAmount;
	@Column(name = "SUCCESSFUL_COUNT")
	private Long successfulCount;
	@Column(name = "SUCCESSFUL_AMOUNT")
	private Long successfulAmount;
	@Column(name = "FAILED_COUNT")
	private Long failedCount;
	@Column(name = "SEND_START_TIME")  // press [Start] at LinePointList
	private Date sendStartTime;
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	
	public Long getId() {
		return id;
	}
	public String getSerialId() {
		return serialId;
	}
	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Long getAmount() {
		return amount;
	}
	public void setAmount(Long amount) {
		this.amount = amount;
	}
	public Long getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(Long totalCount) {
		this.totalCount = totalCount;
	}
	public Long getSuccessfulCount() {
		return successfulCount;
	}
	public void setSuccessfulCount(Long successfulCount) {
		this.successfulCount = successfulCount;
	}
	public Long getFailedCount() {
		return failedCount;
	}
	public void setFailedCount(Long failedCount) {
		this.failedCount = failedCount;
	}
	public String getModifyUser() {
		return modifyUser;
	}
	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSendType() {
		return sendType;
	}
	public void setSendType(String sendType) {
		this.sendType = sendType;
	}
	public String getPccCode() {
		return pccCode;
	}
	public void setPccCode(String pccCode) {
		this.pccCode = pccCode;
	}
	public String getSendTimingType() {
		return sendTimingType;
	}
	public void setSendTimingType(String sendTimingType) {
		this.sendTimingType = sendTimingType;
	}
	public Date getSendTimingTime() {
		return sendTimingTime;
	}
	public void setSendTimingTime(Date sendTimingTime) {
		this.sendTimingTime = sendTimingTime;
	}
	public String getSendAmountType() {
		return sendAmountType;
	}
	public void setSendAmountType(String sendAmountType) {
		this.sendAmountType = sendAmountType;
	}
	public Boolean getDoCheckFollowage() {
		return doCheckFollowage;
	}
	public void setDoCheckFollowage(Boolean doCheckFollowage) {
		this.doCheckFollowage = doCheckFollowage;
	}
	public Boolean getDoAppendMessage() {
		return doAppendMessage;
	}
	public void setDoAppendMessage(Boolean doAppendMessage) {
		this.doAppendMessage = doAppendMessage;
	}
	public Long getAppendMessageId() {
		return appendMessageId;
	}
	public void setAppendMessageId(Long appendMessageId) {
		this.appendMessageId = appendMessageId;
	}
	public Long getLinePointSendGroupId() {
		return linePointSendGroupId;
	}
	public void setLinePointSendGroupId(Long linePointSendGroupId) {
		this.linePointSendGroupId = linePointSendGroupId;
	}
	public Long getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Long totalAmount) {
		this.totalAmount = totalAmount;
	}
	public Long getSuccessfulAmount() {
		return successfulAmount;
	}
	public void setSuccessfulAmount(Long successfulAmount) {
		this.successfulAmount = successfulAmount;
	}
	public Date getSendStartTime() {
		return sendStartTime;
	}
	public void setSendStartTime(Date sendStartTime) {
		this.sendStartTime = sendStartTime;
	}
}
