package com.bcs.core.taishin.circle.db.entity;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity

@Table(name = "BCS_BILLING_NOTICE_DETAIL",
indexes = {
	       @Index(name = "INDEX_0", columnList = "NOTICE_DETAIL_ID"),
	       @Index(name = "INDEX_1", columnList = "PARENT_TYPE"),
	       @Index(name = "INDEX_2", columnList = "MSG_TYPE"),
	       @Index(name = "INDEX_3", columnList = "NOTICE_MAIN_ID")
	})
//BCS_BILLING_NOTICE_DETAIL與BCS_BILLING_NOTICE_MAIN為1對多關係
public class BillingNoticeDetail extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	public static final String MSG_TYPE_TEMPLATE = "template";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "NOTICE_DETAIL_ID")
	//帳務通知明細ID
	private Long noticeDetailId;

	//帳務通知主檔ID
	@Column(name = "NOTICE_MAIN_ID")
	private Long noticeMainId;

	@Column(name = "PARENT_TYPE", columnDefinition="nvarchar(50)")
	private String parentType;

	@Column(name = "MSG_TYPE", columnDefinition="nvarchar(50)")
	private String msgType = "template";

	//Line uid
	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String uid;
	
	//訊息title
	@Column(name = "TITLE", columnDefinition="nvarchar(500)")
	private String title;
	
	//訊息內文
	@Column(name = "TEXT", columnDefinition="nvarchar(1000)")
	private String text;

	@Column(name = "CREAT_TIME")
	private Date createTime;
	
	@Column(name = "SEND_TIME")
	private Date sendTime;
	
	//明細檔狀態；select for update wait時更新此欄位，防止重複發送
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	@PrePersist
	public void prePersist() {
		createTime = Calendar.getInstance().getTime();
		modifyTime = createTime;
	}
	
	@PreUpdate
	public void preUpdate() {
		modifyTime = Calendar.getInstance().getTime();
	}
	
	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public Long getNoticeDetailId() {
		return noticeDetailId;
	}

	public void setNoticeDetailId(Long noticeDetailId) {
		this.noticeDetailId = noticeDetailId;
	}

	public Long getNoticeMainId() {
		return noticeMainId;
	}

	public void setNoticeMainId(Long noticeMainId) {
		this.noticeMainId = noticeMainId;
	}

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

}
