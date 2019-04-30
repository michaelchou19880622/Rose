package com.bcs.core.db.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.utils.ObjectUtil;

@Entity
@Table(name = "BCS_MSG_API_SEND_RECORD",
indexes = {
	       @Index(name = "INDEX_0", columnList = "MSG_SOURCE"),
	       @Index(name = "INDEX_1", columnList = "MID"),
	       @Index(name = "INDEX_2", columnList = "SEND_TYPE"),
	       @Index(name = "INDEX_3", columnList = "STATUS"),
	})
public class MsgApiSendRecord extends AbstractBcsEntity {
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "SEND_API_ID", columnDefinition="nvarchar(50)")
	private String sendApiId;

	@Column(name = "SEND_TIME")
	private Date sendTime;

	@Column(name = "RECEIVE_TIME")
	private Date receiveTime;

	@Column(name = "MID", columnDefinition="nvarchar(50)")
	private String mid;

	@Column(name = "MSG_SOURCE", columnDefinition="nvarchar(50)")
	private String msgSource;

	@Column(name = "MSG_NAME", columnDefinition="nvarchar(50)")
	private String msgName;

	@Column(name = "SEND_TYPE", columnDefinition="nvarchar(50)")
	private String sendType;

	@Column(name = "DELAY_TIME", columnDefinition="nvarchar(50)")
	private String delayTime;

	@Column(name = "CHANNEL_ID", columnDefinition="nvarchar(50)")
	private String channelId;

	@Column(name = "API_TYPE", columnDefinition="nvarchar(50)")
	private String apiType;

	@Column(name = "SEND_RECORD", columnDefinition="nvarchar(300)")
	private String sendRecord;

	@Column(name = "SEND_BODY", columnDefinition="nvarchar(3000)")
	private String sendBody;

	@Column(name = "STATUS", columnDefinition="nvarchar(50)")
	private String status;

	public String getSendApiId() {
		return sendApiId;
	}

	public void setSendApiId(String sendApiId) {
		this.sendApiId = sendApiId;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public Date getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getMsgSource() {
		return msgSource;
	}

	public void setMsgSource(String msgSource) {
		this.msgSource = msgSource;
	}

	public String getMsgName() {
		return msgName;
	}

	public void setMsgName(String msgName) {
		this.msgName = msgName;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public String getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(String delayTime) {
		this.delayTime = delayTime;
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

	public void setSendRecord(String sendRecord) {
		this.sendRecord = sendRecord;
	}

	public void setSendRecord(Object sendRecord) {
		this.sendRecord = ObjectUtil.objectToJsonStr(sendRecord);
		if(this.sendRecord.length() > 300){
			this.sendRecord = this.sendRecord.substring(0, 299);
		}
	}

	public String getSendBody() {
		return sendBody;
	}

	public void setSendBody(String sendBody) {
		this.sendBody = sendBody;
	}

	public void setSendBody(Object sendBody) {
		this.sendBody = ObjectUtil.objectToJsonStr(sendBody);
		if(this.sendBody.length() > 3000){
			this.sendBody = this.sendBody.substring(0, 2999);
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
