package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(
	name = "BCS_USER_LIVE_CHAT", 
	indexes = {
		@Index(name = "INDEX_0", columnList = "UID"),
		@Index(name = "INDEX_1", columnList = "CHAT_ID")
	}
)
public class UserLiveChat extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	/* 使用者的客服轉接狀態 */
	public static final String BEGINNING = "BEGINNING";
	public static final String WAITING = "WAITING";
	public static final String IN_PROGRESS = "IN_PROGRESS";
	public static final String FINISH = "FINISH";
	public static final String DISCARD = "DISCARD";
	
	/* 使用者的留言狀態 */
	public static final String LEAVE_MESSAGE = "LEAVE_MESSAGE";
	public static final String CONFIRM = "CONFIRM";
	public static final String COMPLETE = "COMPLETE";
	public static final String REPLY = "REPLY";
	
	/* 部門類別 */
	public static final String CATEGORY_CREDIT_CARD = "creditcard";
	public static final String CATEGORY_BANK = "bank";
	public static final String CATEGORY_RICHART = "richart";
	public static final String CATEGORY_GENERAL = "general";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long Id;
	
	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String UID;
	
	@Column(name = "CHAT_ID")
	private Long chatId;
	
	@Column(name = "HASH", columnDefinition="nvarchar(50)")
	private String hash;
	
	@Column(name = "CATEGORY", columnDefinition="nvarchar(50)")
	private String category;
	
	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;
	
	@Column(name = "LEAVE_MESSAGE_STATE", columnDefinition="nvarchar(50)")
	private String leaveMsgState;
	
	@Column(name = "MESSAGE", columnDefinition="nvarchar(255)")
	private String messsage;
	
	@Column(name = "CREATE_TIME")
	private Date createTime;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;
	
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}

	public String getUID() {
		return UID;
	}
	public void setUID(String uID) {
		UID = uID;
	}

	public Long getChatId() {
		return chatId;
	}
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getLeaveMsgState() {
		return leaveMsgState;
	}
	public void setLeaveMsgState(String leaveMsgState) {
		this.leaveMsgState = leaveMsgState;
	}
	
	public String getMesssage() {
		return messsage;
	}
	public void setMesssage(String messsage) {
		this.messsage = messsage;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
}