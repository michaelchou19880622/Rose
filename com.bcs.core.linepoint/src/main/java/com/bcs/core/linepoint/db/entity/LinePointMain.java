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
	
	public static final String STATUS_DELETE = "DELETE";
	public static final String STATUS_IDLE = "IDLE";
	public static final String STATUS_SCHEDULED = "SCHEDULED";
	public static final String STATUS_COMPLETE = "COMPLETE";
	
	public static final String SEND_TYPE_MANUAL = "MANUAL"; // BY BCS 
	public static final String SEND_TYPE_AUTO = "AUTO"; // BY MGM
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "SERIAL_ID", columnDefinition="nvarchar(50)")
	private String serialId;
	
	@Column(name = "TITLE", columnDefinition="nvarchar(50)")
	private String title;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@Column(name = "AMOUNT")
	private Long amount;
	
	@Column(name = "TOTAL_COUNT")
	private Long totalCount;
	
	@Column(name = "SEND_TYPE", columnDefinition="varchar(50)")
	private String sendType;

	@Column(name = "SUCCESSFUL_COUNT")
	private Long successfulCount;
	
	@Column(name = "FAILED_COUNT")
	private Long failedCount;
	
	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

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

	public Long getId() {
		return id;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}
}
