package com.bcs.core.bot.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;

@Entity
@Table(name = "BCS_LIVE_CHAT_REPLY_RECORD")
public class LiveChatReplyRecord extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "UID", columnDefinition="nvarchar(50)")
	private String UID;
	
	@Column(name = "MSG_TYPE", columnDefinition="nvarchar(50)")
	private String msgType;
	
	@Column(name = "MSG_TEXT", columnDefinition="nvarchar(255)")
	private String msgText;
	
	@Column(name = "USER_LIVE_CHAT_ID")
	private Long userLiveChatId;
	
	@Column(name = "CREATE_TIME")
	private Date createTime;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public String getUID() {
		return UID;
	}
	public void setUID(String UID) {
		this.UID = UID;
	}

	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getMsgText() {
		return msgText;
	}
	public void setMsgText(String msgText) {
		this.msgText = msgText;
	}

	public Long getUserLiveChatId() {
		return userLiveChatId;
	}
	public void setUserLiveChatId(Long userLiveChatId) {
		this.userLiveChatId = userLiveChatId;
	}
	
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}