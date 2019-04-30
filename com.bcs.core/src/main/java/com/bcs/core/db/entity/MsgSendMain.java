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

@SqlResultSetMapping(name = "MsgSendMainDetails", entities = {
		@EntityResult(entityClass = MsgSendMain.class),
		@EntityResult(entityClass = MsgDetail.class)
})

@NamedNativeQueries({
		@NamedNativeQuery(name = "queryGetMsgSendMainDetailByStatus", query = "SELECT BCS_MSG_SEND_MAIN.MSG_SEND_ID, SERIAL_ID, GROUP_ID, GROUP_TITLE, MODIFY_USER, BCS_MSG_SEND_MAIN.MSG_ID, MSG_TAG, SEND_COUNT, SEND_TIME, SEND_TOTAL_COUNT, SEND_TYPE, SCHEDULE_TIME, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_SEND_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_SEND_MAIN.MSG_SEND_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_SEND_MAIN' AND STATUS = ?1 ORDER BY SEND_TIME DESC, MSG_DETAIL_ID ", resultSetMapping = "MsgSendMainDetails"),
		@NamedNativeQuery(name = "queryGetMsgSendMainDetailByMsgId", query = "SELECT BCS_MSG_SEND_MAIN.MSG_SEND_ID, SERIAL_ID, GROUP_ID, GROUP_TITLE, MODIFY_USER, BCS_MSG_SEND_MAIN.MSG_ID, MSG_TAG, SEND_COUNT, SEND_TIME, SEND_TOTAL_COUNT, SEND_TYPE, SCHEDULE_TIME, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_SEND_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_SEND_MAIN.MSG_SEND_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_SEND_MAIN' AND BCS_MSG_SEND_MAIN.MSG_SEND_ID = ?1 ORDER BY SEND_TIME DESC, MSG_DETAIL_ID ", resultSetMapping="MsgSendMainDetails"),
		@NamedNativeQuery(name = "queryGetMsgSendMainDetailAll",              query = "SELECT BCS_MSG_SEND_MAIN.MSG_SEND_ID, SERIAL_ID, GROUP_ID, GROUP_TITLE, MODIFY_USER, BCS_MSG_SEND_MAIN.MSG_ID, MSG_TAG, SEND_COUNT, SEND_TIME, SEND_TOTAL_COUNT, SEND_TYPE, SCHEDULE_TIME, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_SEND_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_SEND_MAIN.MSG_SEND_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_SEND_MAIN' AND STATUS <> 'DELETE' ORDER BY SEND_TIME DESC, MSG_DETAIL_ID ", resultSetMapping="MsgSendMainDetails"),
})

@Entity
@Table(name = "BCS_MSG_SEND_MAIN",
indexes = {
	       @Index(name = "INDEX_0", columnList = "MSG_ID"),
	       @Index(name = "INDEX_1", columnList = "STATUS"),
	})
public class MsgSendMain extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	public static final String THIS_PARENT_TYPE = "BCS_MSG_SEND_MAIN";

	public static final String SENDING_MSG_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SENDING_MSG_TYPE_DELAY = "DELAY";
	public static final String SENDING_MSG_TYPE_SCHEDULE  = "SCHEDULE";

	public static final String MESSAGE_STATUS_FINISH= "FINISH";
	public static final String MESSAGE_STATUS_PROCESS = "PROCESS";
	public static final String MESSAGE_STATUS_FAIL = "FAIL";
	public static final String MESSAGE_STATUS_DELETE = "DELETE";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "MSG_SEND_ID")
	private Long msgSendId;
	
	@Column(name = "MSG_ID")
	private Long msgId;

	@Column(name = "GROUP_ID")
	private Long groupId;

	@Column(name = "GROUP_TITLE", columnDefinition="nvarchar(50)")
	private String groupTitle;

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
	
	@Column(name = "SEND_TOTAL_COUNT")
	private Long sendTotalCount;
	
	@Column(name = "SEND_COUNT")
	private Long sendCount;

	@Column(name = "SEND_TIME")
	private Date sendTime;

	@Column(name = "SERIAL_ID", columnDefinition="nvarchar(50)")
	private String serialId;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;

	public Long getMsgSendId() {
		return msgSendId;
	}

	public void setMsgSendId(Long msgSendId) {
		this.msgSendId = msgSendId;
	}

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

	public String getGroupTitle() {
		return groupTitle;
	}

	public void setGroupTitle(String groupTitle) {
		this.groupTitle = groupTitle;
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

	public Long getSendTotalCount() {
		return sendTotalCount;
	}

	public void setSendTotalCount(Long sendTotalCount) {
		this.sendTotalCount = sendTotalCount;
	}

	public Long getSendCount() {
		return sendCount;
	}

	public void setSendCount(Long sendCount) {
		this.sendCount = sendCount;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}
	
}
