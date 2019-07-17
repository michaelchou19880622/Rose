package com.bcs.core.taishin.circle.PNP.akka.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.bcs.core.enums.API_TYPE;
import com.bcs.core.json.AbstractBcsEntity;
import com.bcs.core.taishin.circle.PNP.db.entity.PnpDetail;

public class PnpDetailReportModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private Long pnpDetailId;
	private Long pnpMainId;
	private String uid;
	private String phone;
	private String msg;
	private String source; // Mitake/Ming/Every8D/Unica
	private String scheduleTime;
	private Date sendTime;
	
	public Long getPnpDetailId() {
		return pnpDetailId;
	}
	public void setPnpDetailId(Long pnpDetailId) {
		this.pnpDetailId = pnpDetailId;
	}
	public Long getPnpMainId() {
		return pnpMainId;
	}
	public void setPnpMainId(Long pnpMainId) {
		this.pnpMainId = pnpMainId;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getScheduleTime() {
		return scheduleTime;
	}
	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
}

