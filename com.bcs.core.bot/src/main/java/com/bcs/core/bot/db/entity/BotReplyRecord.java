package com.bcs.core.bot.db.entity;

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
@Table(name = "BCS_BOT_REPLY_RECORD")
@NamedNativeQuery(name = "getChatLog", query =
	"SELECT " + 
		"chatLog.* " + 
	"FROM " +
		"(" + 
			"(" + 
				"SELECT " + 
					"'BOT' 'from', " + 
					"MSG_TYPE messageType, " + 
					"MSG_TEXT text, " + 
					"MODIFY_TIME 'timestamp' " + 
				"FROM " + 
					"BCS_BOT_REPLY_RECORD " + 
				"WHERE " + 
					"MODIFY_TIME BETWEEN ?1 AND ?2 " + 
				"AND UID = ?3" + 
			")" +  
			"UNION ALL" +
			"(" + 
				"SELECT " + 
					"SOURCE_ID 'from', " + 
					"MSG_TYPE messageType, " + 
					"TEXT text, " + 
					"RECEIVE_TIME 'timestamp' " + 
				"FROM " + 
					"BCS_MSG_BOT_RECEIVE " + 
				"WHERE " + 
					"RECEIVE_TIME BETWEEN ?1 AND ?2 " + 
				"AND SOURCE_ID = ?3 " + 
				"AND MSG_TYPE IS NOT NULL" +
			")" + 
		") AS chatLog " + 
	"ORDER BY " + 
		"'timestamp' DESC")
public class BotReplyRecord extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	public static final String MESSAGE_TYPE_TEXT = "text";
	public static final String MESSAGE_TYPE_STICKER = "sticker";
	public static final String MESSAGE_TYPE_IMAGE = "image";
	public static final String MESSAGE_TYPE_VIDEO = "video";
	public static final String MESSAGE_TYPE_AUDIO = "audio";
	public static final String MESSAGE_TYPE_LOCATION = "location";
	public static final String MESSAGE_TYPE_TEMPLATE = "template";
	public static final String MESSAGE_TYPE_IMAGEMAP = "imagemap";
	
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

	@Column(name = "REFERENCE_LINE_URL", columnDefinition="nvarchar(255)")
	private String referenceLinkUrl;
	
	@Column(name = "LOCATION", columnDefinition="nvarchar(50)")
	private String location;
	
	@Column(name = "REPLY_TOKEN", columnDefinition="nvarchar(50)")
	private String replyToken;
	
	@Column(name = "CREATE_TIME")
	private Date createTime;
	
	@Column(name = "MODIFY_TIME")
	private Date modifyTime;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	
	public String getReferenceLinkUrl() {
		return referenceLinkUrl;
	}
	public void setReferenceLinkUrl(String referenceLinkUrl) {
		this.referenceLinkUrl = referenceLinkUrl;
	}
	
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getUID() {
		return UID;
	}
	public void setUID(String uID) {
		UID = uID;
	}
	
	public String getReplyToken() {
		return replyToken;
	}
	public void setReplyToken(String replyToken) {
		this.replyToken = replyToken;
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