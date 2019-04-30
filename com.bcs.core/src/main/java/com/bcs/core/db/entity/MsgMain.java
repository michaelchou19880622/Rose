package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@SqlResultSetMapping(name = "MsgMainDetails", entities = {
		@EntityResult(entityClass = MsgMain.class),
		@EntityResult(entityClass = MsgDetail.class)
})

@NamedNativeQueries({
		@NamedNativeQuery(name = "queryGetMsgMainDetailByStatus",                             query = "SELECT BCS_MSG_MAIN.MSG_ID , SERIAL_ID , GROUP_ID, MODIFY_TIME, MODIFY_USER, MSG_TAG, SCHEDULE_TIME, SEND_TYPE, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_MAIN.MSG_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_MAIN' AND STATUS = ?1 ORDER BY MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping = "MsgMainDetails"),
		@NamedNativeQuery(name = "queryGetMsgMainDetailByMsgId",                             query = "SELECT BCS_MSG_MAIN.MSG_ID , SERIAL_ID , GROUP_ID, MODIFY_TIME, MODIFY_USER, MSG_TAG, SCHEDULE_TIME, SEND_TYPE, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_MAIN.MSG_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_MAIN' AND BCS_MSG_MAIN.MSG_ID = ?1 ORDER BY MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping="MsgMainDetails"),
		@NamedNativeQuery(name = "queryGetMsgMainDetailByStatusAndSendType", query = "SELECT BCS_MSG_MAIN.MSG_ID , SERIAL_ID , GROUP_ID, MODIFY_TIME, MODIFY_USER, MSG_TAG, SCHEDULE_TIME, SEND_TYPE, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_MAIN.MSG_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_MAIN' AND STATUS = ?1 AND SEND_TYPE = ?2 ORDER BY MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping = "MsgMainDetails"),
})

@Entity
@Table(name = "BCS_MSG_MAIN",
indexes = {
	       @Index(name = "INDEX_0", columnList = "SEND_TYPE"),
	       @Index(name = "INDEX_1", columnList = "STATUS"),
	})
public class MsgMain extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	public static final String THIS_PARENT_TYPE = "BCS_MSG_MAIN";

	public static final String SENDING_MSG_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SENDING_MSG_TYPE_DELAY = "DELAY";
	public static final String SENDING_MSG_TYPE_SCHEDULE  = "SCHEDULE";

	public static final String MESSAGE_STATUS_DELETE = "DELETE";
	public static final String MESSAGE_STATUS_DRAFT = "DRAFT";
	public static final String MESSAGE_STATUS_COMPLETE = "COMPLETE";
	public static final String MESSAGE_STATUS_SCHEDULED = "SCHEDULED";
	public static final String MESSAGE_STATUS_FAIL = "FAIL";

	public static final String SENDING_MSG_SCHEDULED_TYPE_EVERY_MONTH = "EveryMonth";
	public static final String SENDING_MSG_SCHEDULED_TYPE_EVERY_WEEK = "EveryWeek";
	public static final String SENDING_MSG_SCHEDULED_TYPE_EVERY_DAY = "EveryDay";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "MSG_ID")
	private Long msgId;

	@Column(name = "GROUP_ID")
	private Long groupId;

	@Column(name = "SEND_TYPE", columnDefinition="nvarchar(50)")
	private String sendType;

	@Column(name = "SCHEDULE_TIME", columnDefinition="nvarchar(50)")
	private String scheduleTime;

	@Column(name = "MSG_TAG", columnDefinition="nvarchar(50)")
	private String msgTag;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

	@Column(name = "STATUS_NOTICE", columnDefinition="nvarchar(200)")
	private String statusNotice;

	@Column(name = "SERIAL_ID", columnDefinition="nvarchar(50)")
	private String serialId;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	public Long getMsgId() {
		return msgId;
	}

	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public String getMsgTag() {
		return msgTag;
	}

	public void setMsgTag(String msgTag) {
		this.msgTag = msgTag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusNotice() {
		return statusNotice;
	}

	public void setStatusNotice(String statusNotice) {
		this.statusNotice = statusNotice;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}
	
}
