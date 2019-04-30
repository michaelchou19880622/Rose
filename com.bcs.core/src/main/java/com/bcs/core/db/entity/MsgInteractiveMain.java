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
import com.bcs.core.json.CustomDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@SqlResultSetMapping(name = "MsgInteractiveMainDetails", entities = {
		@EntityResult(entityClass = MsgInteractiveMain.class),
		@EntityResult(entityClass = MsgDetail.class)
})

@NamedNativeQueries({
		@NamedNativeQuery(name = "queryGetMsgInteractiveMainDetailByType",                      query = "SELECT BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID, INTERACTIVE_STATUS, INTERACTIVE_TYPE, USER_STATUS, MAIN_KEYWORD, STATUS_NOTICE, INTERACTIVE_INDEX, INTERACTIVE_START_TIME, INTERACTIVE_END_TIME, INTERACTIVE_TIME_TYPE, OTHER_ROLE, SERIAL_ID, SEND_COUNT, MODIFY_USER, MODIFY_TIME, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_INTERACTIVE_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_INTERACTIVE_MAIN' AND INTERACTIVE_TYPE = ?1 ORDER BY CASE WHEN INTERACTIVE_INDEX IS NULL THEN 1 ELSE 0 END, INTERACTIVE_INDEX, MAIN_KEYWORD, USER_STATUS, MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping = "MsgInteractiveMainDetails"),
		@NamedNativeQuery(name = "queryGetMsgInteractiveMainDetailByTypeAndStatus", query = "SELECT BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID, INTERACTIVE_STATUS, INTERACTIVE_TYPE, USER_STATUS, MAIN_KEYWORD, STATUS_NOTICE, INTERACTIVE_INDEX, INTERACTIVE_START_TIME, INTERACTIVE_END_TIME, INTERACTIVE_TIME_TYPE, OTHER_ROLE, SERIAL_ID, SEND_COUNT, MODIFY_USER, MODIFY_TIME, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_INTERACTIVE_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_INTERACTIVE_MAIN' AND INTERACTIVE_TYPE = ?1 AND INTERACTIVE_STATUS = ?2 ORDER BY CASE WHEN INTERACTIVE_INDEX IS NULL THEN 1 ELSE 0 END, INTERACTIVE_INDEX, MAIN_KEYWORD, USER_STATUS, MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping = "MsgInteractiveMainDetails"),
		@NamedNativeQuery(name = "queryGetMsgInteractiveMainDetailByMsgId",                   query = "SELECT BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID, INTERACTIVE_STATUS, INTERACTIVE_TYPE, USER_STATUS, MAIN_KEYWORD, STATUS_NOTICE, INTERACTIVE_INDEX, INTERACTIVE_START_TIME, INTERACTIVE_END_TIME, INTERACTIVE_TIME_TYPE, OTHER_ROLE, SERIAL_ID, SEND_COUNT, MODIFY_USER, MODIFY_TIME, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_INTERACTIVE_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_INTERACTIVE_MAIN' AND BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID = ?1 ORDER BY MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping="MsgInteractiveMainDetails"),
		@NamedNativeQuery(name = "queryGetMsgInteractiveMainDetailAll",                                query = "SELECT BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID, INTERACTIVE_STATUS, INTERACTIVE_TYPE, USER_STATUS, MAIN_KEYWORD, STATUS_NOTICE, INTERACTIVE_INDEX, INTERACTIVE_START_TIME, INTERACTIVE_END_TIME, INTERACTIVE_TIME_TYPE, OTHER_ROLE, SERIAL_ID, SEND_COUNT, MODIFY_USER, MODIFY_TIME, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_INTERACTIVE_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_INTERACTIVE_MAIN.MSG_INTERACTIVE_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_INTERACTIVE_MAIN' ORDER BY CASE WHEN INTERACTIVE_INDEX IS NULL THEN 1 ELSE 0 END, INTERACTIVE_INDEX, MAIN_KEYWORD, USER_STATUS, MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping="MsgInteractiveMainDetails"),
})

@Entity
@Table(name = "BCS_MSG_INTERACTIVE_MAIN",
indexes = {
	       @Index(name = "INDEX_0", columnList = "INTERACTIVE_STATUS"),
	       @Index(name = "INDEX_1", columnList = "INTERACTIVE_TYPE"),
	})
public class MsgInteractiveMain extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	public static final String THIS_PARENT_TYPE = "BCS_MSG_INTERACTIVE_MAIN";
	
	public static final String USER_STATUS_ALL = "ALL";
	
	public static final String INTERACTIVE_STATUS_ACTIVE = "ACTIVE";
	public static final String INTERACTIVE_STATUS_DISABLE = "DISABLE";
	public static final String INTERACTIVE_STATUS_DELETE = "DELETE";

	public static final String INTERACTIVE_KEYWORD_LINE_BCS_WELCOME_KEYWORD = "LINE_BCS_WELCOME_KEYWORD";
	public static final String INTERACTIVE_KEYWORD_LINE_BCS_AUTO_KEYWORD = "LINE_BCS_AUTO_KEYWORD";
	public static final String INTERACTIVE_KEYWORD_LINE_BCS_EVENT_KEYWORD = "LINE_BCS_EVENT_KEYWORD";
	
	public static final String INTERACTIVE_TYPE_KEYWORD = "KEYWORD";
	public static final String INTERACTIVE_TYPE_INTERACTIVE = "INTERACTIVE";
    public static final String INTERACTIVE_TYPE_BLACK_KEYWORD = "BLACK_KEYWORD";
    public static final String INTERACTIVE_TYPE_CAMPAIGN = "CAMPAIGN";

	/**
	 * 24 小時內的設定
	 */
	public static final String INTERACTIVE_TIME_TYPE_DAY = "TYPE_DAY";
	/**
	 * 開始時間到結束時間
	 */
	public static final String INTERACTIVE_TIME_TYPE_RANGE= "TYPE_RANGE";
	
	public static final Long indexLimit = 3L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "MSG_INTERACTIVE_ID")
	private Long iMsgId;

	@Column(name = "INTERACTIVE_STATUS", columnDefinition="nvarchar(50)")
	private String interactiveStatus;

	@Column(name = "INTERACTIVE_TYPE", columnDefinition="nvarchar(50)")
	private String interactiveType;

	@Column(name = "USER_STATUS", columnDefinition="nvarchar(50)")
	private String userStatus;

	@Column(name = "MAIN_KEYWORD", columnDefinition="nvarchar(50)")
	private String mainKeyword;

	@Column(name = "STATUS_NOTICE", columnDefinition="nvarchar(200)")
	private String statusNotice;
	
	@Column(name = "INTERACTIVE_INDEX")
	private Long interactiveIndex;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "INTERACTIVE_START_TIME")
	private Date interactiveStartTime;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@Column(name = "INTERACTIVE_END_TIME")
	private Date interactiveEndTime;

	@Column(name = "INTERACTIVE_TIME_TYPE", columnDefinition="nvarchar(50)")
	private String interactiveTimeType;
	
	@Column(name = "SEND_COUNT")
	private Long sendCount;

	@Column(name = "MODIFY_USER", columnDefinition="nvarchar(50)")
	private String modifyUser;	

	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	@Column(name = "OTHER_ROLE", columnDefinition="nvarchar(50)")
	private String otherRole;

	@Column(name = "SERIAL_ID", columnDefinition="nvarchar(50)")
	private String serialId;

	public Long getiMsgId() {
		return iMsgId;
	}

	public void setiMsgId(Long iMsgId) {
		this.iMsgId = iMsgId;
	}

	public String getInteractiveStatus() {
		return interactiveStatus;
	}

	public void setInteractiveStatus(String interactiveStatus) {
		this.interactiveStatus = interactiveStatus;
	}

	public String getInteractiveType() {
		return interactiveType;
	}

	public void setInteractiveType(String interactiveType) {
		this.interactiveType = interactiveType;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getMainKeyword() {
		return mainKeyword;
	}

	public void setMainKeyword(String mainKeyword) {
		this.mainKeyword = mainKeyword;
	}

	public String getStatusNotice() {
		return statusNotice;
	}

	public void setStatusNotice(String statusNotice) {
		this.statusNotice = statusNotice;
	}

	public Long getInteractiveIndex() {
		return interactiveIndex;
	}

	public void setInteractiveIndex(Long interactiveIndex) {
		this.interactiveIndex = interactiveIndex;
	}

	public Date getInteractiveStartTime() {
		return interactiveStartTime;
	}

	public void setInteractiveStartTime(Date interactiveStartTime) {
		this.interactiveStartTime = interactiveStartTime;
	}

	public Date getInteractiveEndTime() {
		return interactiveEndTime;
	}

	public void setInteractiveEndTime(Date interactiveEndTime) {
		this.interactiveEndTime = interactiveEndTime;
	}

	public String getInteractiveTimeType() {
		return interactiveTimeType;
	}

	public void setInteractiveTimeType(String interactiveTimeType) {
		this.interactiveTimeType = interactiveTimeType;
	}

	public Long getSendCount() {
		return sendCount;
	}

	public void setSendCount(Long sendCount) {
		this.sendCount = sendCount;
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

	public String getOtherRole() {
		return otherRole;
	}

	public void setOtherRole(String otherRole) {
		this.otherRole = otherRole;
	}

	public String getSerialId() {
		return serialId;
	}

	public void setSerialId(String serialId) {
		this.serialId = serialId;
	}
	
}
