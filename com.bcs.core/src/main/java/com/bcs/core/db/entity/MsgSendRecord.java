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
import com.bcs.core.utils.ObjectUtil;

@Entity
@Table(name = "BCS_MSG_SEND_RECORD",
indexes = {
	       @Index(name = "INDEX_0", columnList = "MSG_SEND_ID"),
	       @Index(name = "INDEX_1", columnList = "MID"),
	})
public class MsgSendRecord extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "SEND_ID")
	private Long sendId;

	@Column(name = "MSG_SEND_ID")
	private Long msgSendId;

	@Column(name = "SEND_TIME")
	private Date sendTime;

	@Column(name = "MID", columnDefinition="nvarchar(50)")
	private String mid;

	@Column(name = "CHANNEL_ID", columnDefinition="nvarchar(50)")
	private String channelId;

	@Column(name = "API_TYPE", columnDefinition="nvarchar(50)")
	private String apiType;

	@Column(name = "SEND_RECORD", columnDefinition="nvarchar(1000)")
	private String sendRecord;

	public Long getSendId() {
		return sendId;
	}

	public void setSendId(Long sendId) {
		this.sendId = sendId;
	}

	public Long getMsgSendId() {
		return msgSendId;
	}

	public void setMsgSendId(Long msgSendId) {
		this.msgSendId = msgSendId;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	public String getSendRecord() {
		return sendRecord;
	}

	public void setSendRecord(Object sendRecord) {
		this.sendRecord = ObjectUtil.objectToJsonStr(sendRecord);
		if(this.sendRecord.length() > 1000){
			this.sendRecord = this.sendRecord.substring(0, 999);
		}
	}
	
}
