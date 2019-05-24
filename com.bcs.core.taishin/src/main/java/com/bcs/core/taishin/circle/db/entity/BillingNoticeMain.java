package com.bcs.core.taishin.circle.db.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedNativeQueries;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.bcs.core.json.AbstractBcsEntity;

@SqlResultSetMapping(name = "BillingNoticeMainDetails", entities = {
		@EntityResult(entityClass = BillingNoticeMain.class),
		@EntityResult(entityClass = BillingNoticeDetail.class)
})

@NamedNativeQueries({
//		@NamedNativeQuery(name = "queryGetMsgMainDetailByStatus",                             query = "SELECT BCS_MSG_MAIN.MSG_ID , SERIAL_ID , GROUP_ID, MODIFY_TIME, MODIFY_USER, MSG_TAG, SCHEDULE_TIME, SEND_TYPE, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_MAIN.MSG_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_MAIN' AND STATUS = ?1 ORDER BY MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping = "MsgMainDetails"),
//		@NamedNativeQuery(name = "queryGetMsgMainDetailByMsgId",                             query = "SELECT BCS_MSG_MAIN.MSG_ID , SERIAL_ID , GROUP_ID, MODIFY_TIME, MODIFY_USER, MSG_TAG, SCHEDULE_TIME, SEND_TYPE, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_MAIN.MSG_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_MAIN' AND BCS_MSG_MAIN.MSG_ID = ?1 ORDER BY MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping="MsgMainDetails"),
//		@NamedNativeQuery(name = "queryGetMsgMainDetailByStatusAndSendType", query = "SELECT BCS_MSG_MAIN.MSG_ID , SERIAL_ID , GROUP_ID, MODIFY_TIME, MODIFY_USER, MSG_TAG, SCHEDULE_TIME, SEND_TYPE, STATUS, STATUS_NOTICE, MSG_DETAIL_ID, BCS_MSG_DETAIL.MSG_ID, MSG_TYPE, REFERENCE_ID, TEXT, MSG_PARENT_TYPE, BCS_MSG_DETAIL.EVENT_TYPE FROM BCS_MSG_MAIN LEFT OUTER JOIN BCS_MSG_DETAIL ON BCS_MSG_MAIN.MSG_ID = BCS_MSG_DETAIL.MSG_ID WHERE BCS_MSG_DETAIL.MSG_PARENT_TYPE = 'BCS_MSG_MAIN' AND STATUS = ?1 AND SEND_TYPE = ?2 ORDER BY MODIFY_TIME, MSG_DETAIL_ID ", resultSetMapping = "MsgMainDetails"),
})

@Entity
@Table(name = "BCS_BILLING_NOTICE_MAIN",
indexes = {
	       @Index(name = "INDEX_0", columnList = "SEND_TYPE"),
	       @Index(name = "INDEX_1", columnList = "STATUS"),
	       @Index(name = "INDEX_3", columnList = "NOTICE_MAIN_ID")
	})

//與FTP的來源檔案為1對1關係
public class BillingNoticeMain extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	public static final String THIS_PARENT_TYPE = "BCS_BILLING_NOTICE_MAIN";

	public static final String SENDING_MSG_TYPE_IMMEDIATE = "IMMEDIATE";
	public static final String SENDING_MSG_TYPE_DELAY = "DELAY";
	public static final String SENDING_MSG_TYPE_SCHEDULE  = "SCHEDULE";

	//未發送
	public static final String NOTICE_STATUS_WAIT = "WAIT";
	//重試
	public static final String NOTICE_STATUS_RETRY = "RETRY";
	//發送失敗、不推送
	public static final String NOTICE_STATUS_FAIL = "FAIL";
	public static final String NOTICE_STATUS_SENDING = "SENDING";
	public static final String NOTICE_STATUS_DELETE = "DELETE";
	
	public static final String NOTICE_STATUS_DRAFT = "DRAFT";
	public static final String NOTICE_STATUS_COMPLETE = "COMPLETE";
	public static final String NOTICE_STATUS_SCHEDULED = "SCHEDULED";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "NOTICE_MAIN_ID")
	//帳務通知主檔ID；unigue
	private Long noticeMainId;
	
	//樣版ID
	@Column(name = "TEMP_ID", columnDefinition="nvarchar(50)")
	private String tempId;
	
	//預留欄位分產品用
	@Column(name = "GROUP_ID")
	private Long groupId;

	//DELAY :預約發送；IMMEDIATE：立即發送
	@Column(name = "SEND_TYPE", columnDefinition="nvarchar(10)")
	private String sendType;

	//DELAY :預約發送時才會有；IMMEDIATE：立即發送 = null
	@Column(name = "SCHEDULE_TIME", columnDefinition="nvarchar(50)")
	private String scheduleTime;
	
	@Column(name = "MSG_TAG", columnDefinition="nvarchar(50)")
	private String msgTag;

	//主檔狀態；select for update wait時更新此欄位，防止重複發送
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

	@Column(name = "CREAT_TIME")
	private Date createTime;
	
	//取檔發送AP的hostName，select for update wait時更新此欄位，防止重複發送
	@Column(name = "PROC_AP_NAME", columnDefinition="nvarchar(50)")
	private String procApName;
	
	//FTP來源檔原名
	@Column(name = "ORIG_FILE_NAME", columnDefinition="nvarchar(200)")
	private String origFileName;
	
	//發送效期：收到檔案後三小時
	@Column(name = "EXPIRY_TIME ")
	private Date expiryTime;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	//FTP header type
	@Column(name = "ORIG_FILE_TYPE", columnDefinition="nvarchar(200)")
	private String origFileType;
	
	// 不會異動ＤＢ 物件傳遞暫存用
	@Transient
	private List<BillingNoticeDetail> details;
	
	// 不會異動ＤＢ 物件傳遞暫存用
	@Transient
	private BillingNoticeContentTemplateMsg template;
	
	// 不會異動ＤＢ 物件傳遞暫存用
	@Transient
	private List<BillingNoticeContentTemplateMsgAction> templateActions;
	
	
	@PrePersist
	public void prePersist() {
		createTime = Calendar.getInstance().getTime();
		modifyTime = createTime;
	}
	
	@PreUpdate
	public void preUpdate() {
		modifyTime = Calendar.getInstance().getTime();
	}
	
	public String getOrigFileType() {
		return origFileType;
	}

	public void setOrigFileType(String origFileType) {
		this.origFileType = origFileType;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	
	public List<BillingNoticeDetail> getDetails() {
		return details;
	}

	public void setDetails(List<BillingNoticeDetail> details) {
		this.details = details;
	}

	public BillingNoticeContentTemplateMsg getTemplate() {
		return template;
	}

	public void setTemplate(BillingNoticeContentTemplateMsg template) {
		this.template = template;
	}

	public List<BillingNoticeContentTemplateMsgAction> getTemplateActions() {
		return templateActions;
	}

	public void setTemplateActions(List<BillingNoticeContentTemplateMsgAction> templateActions) {
		this.templateActions = templateActions;
	}

	public Long getNoticeMainId() {
		return noticeMainId;
	}

	public void setNoticeMainId(Long noticeMainId) {
		this.noticeMainId = noticeMainId;
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

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getProcApName() {
		return procApName;
	}

	public void setProcApName(String procApName) {
		this.procApName = procApName;
	}

	public String getOrigFileName() {
		return origFileName;
	}

	public void setOrigFileName(String origFileName) {
		this.origFileName = origFileName;
	}

	public Date getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(Date expiryTime) {
		this.expiryTime = expiryTime;
	}

	public String getTempId() {
		return tempId;
	}

	public void setTempId(String tempId) {
		this.tempId = tempId;
	}
	
}
