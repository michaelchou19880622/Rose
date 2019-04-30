package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_PUSH_MESSAGE_RECORD")
@NamedNativeQuery(name = "getPushMessageEffects", query =
	"SELECT " + 
		"CASE " + 
			"WHEN A.CREATE_TIME IS NULL THEN B.CREATE_TIME " + 
			"ELSE A.CREATE_TIME " + 
		"END 'CREATE_TIME', " + 
		"CASE " + 
			"WHEN A.DEPARTMENT IS NULL THEN B.DEPARTMENT " + 
			"ELSE A.DEPARTMENT " + 
		"END 'DEPARTMENT', " + 
		"CASE " + 
			"WHEN A.SUCCESS_COUNT IS NULL THEN 0 " + 
			"ELSE A.SUCCESS_COUNT " + 
		"END 'SUCCESS_COUNT', " + 
		"CASE " + 
			"WHEN B.FAIL_COUNT IS NULL THEN 0 " + 
			"ELSE B.FAIL_COUNT " + 
		"END 'FAIL_COUNT' " + 
	"FROM " + 
		"(" + 
			"(" + 
				"SELECT " + 
					"CREATE_TIME, " + 
					"DEPARTMENT, " + 
					"COUNT (*) AS SUCCESS_COUNT " + 
				"FROM " + 
					"BCS_PUSH_MESSAGE_RECORD " + 
				"WHERE " + 
					"MAIN_MESSAGE = 'SUCCESS' " + 
				"GROUP BY " + 
					"CREATE_TIME, " + 
					"DEPARTMENT " + 
			") AS A " + 
			"FULL JOIN " + 
			"(" + 
				"SELECT " + 
					"CREATE_TIME, " + 
					"DEPARTMENT, " + 
					"COUNT (*) AS FAIL_COUNT " + 
				"FROM " + 
					"BCS_PUSH_MESSAGE_RECORD " + 
				"WHERE " + 
					"MAIN_MESSAGE != 'SUCCESS' " + 
				"GROUP BY " + 
					"CREATE_TIME, " + 
					"DEPARTMENT " + 
			") AS B ON A.CREATE_TIME = B.CREATE_TIME " + 
		")" + 
	"WHERE " + 
		"(" + 
			"A.CREATE_TIME >= ?1 " + 
			"OR B.CREATE_TIME >= ?1" + 
		") " + 
		"AND (" + 
			"A.CREATE_TIME < ?2 " + 
			"OR B.CREATE_TIME < ?2" + 
		") " + 
	"ORDER BY " + 
		"CREATE_TIME DESC;"
)
public class PushMessageRecord extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "DEPARTMENT", columnDefinition="nvarchar(20)")
	private String department;

	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String UID;
	
	@Column(name = "SEND_MESSAGE", columnDefinition="nvarchar(255)")
	private String sendMessage;
	
	@Column(name = "SEND_TYPE", columnDefinition="nvarchar(20)")
	private String sendType;
	
	@Column(name = "STATUS_CODE", columnDefinition="nvarchar(3)")
	private String statusCode;
	
	@Column(name = "MAIN_MESSAGE", columnDefinition="nvarchar(255)")
	private String mainMessage;
	
	@Column(name = "DETAIL_MESSAGE", columnDefinition="nvarchar(255)")
	private String detailMessage;
	
	@Column(name = "SEND_TIME")
	private Date sendTime;
	
	@Column(name = "RESERVATION_TIME")
	private Date reservationTime;
	
	@Column(name = "CREATE_TIME")
	private Date createTime;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}

	public String getUID() {
		return UID;
	}
	public void setUID(String uID) {
		UID = uID;
	}

	public String getSendMessage() {
		return sendMessage;
	}
	public void setSendMessage(String sendMessage) {
		this.sendMessage = sendMessage;
	}
	
	public String getSendType() {
		return sendType;
	}
	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getMainMessage() {
		return mainMessage;
	}
	public void setMainMessage(String mainMessage) {
		this.mainMessage = mainMessage;
	}

	public String getDetailMessage() {
		return detailMessage;
	}
	public void setDetailMessage(String detailMessage) {
		this.detailMessage = detailMessage;
	}

	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Date getReservationTime() {
		return reservationTime;
	}
	public void setReservationTime(Date reservationTime) {
		this.reservationTime = reservationTime;
	}

	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}