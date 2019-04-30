package com.bcs.core.bot.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.json.CustomDateDeserializer;
import com.bcs.core.json.CustomDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "BCS_MSG_BOT_RECEIVE",
indexes = {
	       @Index(name = "INDX_0", columnList = "REFERENCE_ID"),
	       @Index(name = "INDX_1", columnList = "CHANNEL"),
	       @Index(name = "INDX_2", columnList = "USER_STATUS"),
	       @Index(name = "INDX_3", columnList = "SOURCE_ID"),
	       @Index(name = "INDX_4", columnList = "MSG_ID"),
	       @Index(name = "INDX_5", columnList = "EVENT_TYPE"),
	       @Index(name = "INDX_6", columnList = "RECEIVE_DAY"),
	})
public class MsgBotReceive extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	public static String EVENT_TYPE_MESSAGE = "message";

	public static String MESSAGE_TYPE_TEXT = "text";
	public static String MESSAGE_TYPE_IMAGE = "image";
	public static String MESSAGE_TYPE_VIDEO = "video";
	public static String MESSAGE_TYPE_AUDIO = "audio";
	public static String MESSAGE_TYPE_LOCATION = "location";
	public static String MESSAGE_TYPE_STICKER = "sticker";
	public static String MESSAGE_TYPE_FILE = "file";
	
	public static String EVENT_TYPE_FOLLOW = "follow";
	public static String EVENT_TYPE_UNFOLLOW = "unfollow";
	public static String EVENT_TYPE_JOIN = "join";
	public static String EVENT_TYPE_LEAVE = "leave";
	public static String EVENT_TYPE_POSTBACK = "postback";
	public static String EVENT_TYPE_BEACON = "beacon";

	public static String EVENT_TYPE_BCSEVENT = "bcsevent";

	public static String SOURCE_TYPE_USER = "user";
	public static String SOURCE_TYPE_GROUP = "group";
	public static String SOURCE_TYPE_ROOM = "room";
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "CHANNEL", columnDefinition="nvarchar(50)")
	private String channel;

	@Column(name = "EVENT_TYPE", columnDefinition="nvarchar(50)")
	private String eventType;

	@Column(name = "REPLY_TOKEN", columnDefinition="nvarchar(200)")
	private String replyToken;

	@Column(name = "TIMESTAMP")
	private Long timestamp;

	@JsonDeserialize(using = CustomDateDeserializer.class)
	@JsonSerialize(using=CustomDateSerializer.class)
	@Column(name = "RECEIVE_TIME")
	private Date receiveTime;

	@Column(name = "RECEIVE_DAY", columnDefinition="nvarchar(10)")
	private String receiveDay;

	@Column(name = "SOURCE_TYPE", columnDefinition="nvarchar(50)")
	private String sourceType;

	@Column(name = "SOURCE_ID", columnDefinition="nvarchar(50)")
	private String sourceId;

	@Column(name = "MSG_ID", columnDefinition="nvarchar(50)")
	private String msgId;

	@Column(name = "MSG_TYPE", columnDefinition="nvarchar(50)")
	private String msgType;

	@Column(name = "TEXT", columnDefinition="nvarchar(1000)")
	private String text;

	/**
	 * LOCATION
	 */
	@Column(name = "LOCATION_TITLE", columnDefinition="nvarchar(50)")
	private String locationTitle;
	
	@Column(name = "LOCATION_ADDRESS", columnDefinition="nvarchar(50)")
	private String locationAddress;
	
	@Column(name = "LOCATION_LATITUDE", columnDefinition="nvarchar(50)")
	private String locationLatitude;
	
	@Column(name = "LOCATION_LONGITUDE", columnDefinition="nvarchar(50)")
	private String locationLongitude;
	
	/**
	 * STICKER
	 */
	@Column(name = "STICKER_PACKAGE_ID", columnDefinition="nvarchar(50)")
	private String stickerPackageId;
	
	@Column(name = "STICKER_ID", columnDefinition="nvarchar(50)")
	private String stickerId;
	
	/**
	 * POSTBACK
	 */
	@Column(name = "POSTBACK_DATA", columnDefinition="nvarchar(1000)")
	private String postbackData;
	
	/**
	 * BEACON
	 */
	@Column(name = "BEACON_HWID", columnDefinition="nvarchar(500)")
	private String beaconHwid;
	
	@Column(name = "BEACON_TYPE", columnDefinition="nvarchar(500)")
	private String beaconType;

	@Column(name = "OTHERS", columnDefinition="nvarchar(1000)")
	private String others;
	
	@Column(name = "REFERENCE_ID", columnDefinition="nvarchar(200)")
	private String referenceId;

	@Column(name = "USER_STATUS", columnDefinition="nvarchar(50)")
	private String userStatus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getReplyToken() {
		return replyToken;
	}

	public void setReplyToken(String replyToken) {
		this.replyToken = replyToken;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
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

	public String getLocationTitle() {
		return locationTitle;
	}

	public void setLocationTitle(String locationTitle) {
		this.locationTitle = locationTitle;
	}

	public String getLocationAddress() {
		return locationAddress;
	}

	public void setLocationAddress(String locationAddress) {
		this.locationAddress = locationAddress;
	}

	public String getLocationLatitude() {
		return locationLatitude;
	}

	public void setLocationLatitude(String locationLatitude) {
		this.locationLatitude = locationLatitude;
	}

	public String getLocationLongitude() {
		return locationLongitude;
	}

	public void setLocationLongitude(String locationLongitude) {
		this.locationLongitude = locationLongitude;
	}

	public String getStickerPackageId() {
		return stickerPackageId;
	}

	public void setStickerPackageId(String stickerPackageId) {
		this.stickerPackageId = stickerPackageId;
	}

	public String getStickerId() {
		return stickerId;
	}

	public void setStickerId(String stickerId) {
		this.stickerId = stickerId;
	}

	public String getPostbackData() {
		return postbackData;
	}

	public void setPostbackData(String postbackData) {
		this.postbackData = postbackData;
	}

	public String getBeaconHwid() {
		return beaconHwid;
	}

	public void setBeaconHwid(String beaconHwid) {
		this.beaconHwid = beaconHwid;
	}

	public String getBeaconType() {
		return beaconType;
	}

	public void setBeaconType(String beaconType) {
		this.beaconType = beaconType;
	}

	public String getOthers() {
		return others;
	}

	public void setOthers(String others) {
		this.others = others;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}

	public String getReceiveDay() {
		return receiveDay;
	}

	public void setReceiveDay(String receiveDay) {
		this.receiveDay = receiveDay;
	}
	
}
