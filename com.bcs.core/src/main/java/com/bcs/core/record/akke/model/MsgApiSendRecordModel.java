package com.bcs.core.record.akke.model;

import java.util.List;

import com.bcs.core.db.entity.MsgApiSendRecord;
import com.bcs.core.json.AbstractBcsEntity;
import com.linecorp.bot.model.message.Message;

public class MsgApiSendRecordModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private MsgApiSendRecord msgApiSendRecord;
	private List<Message> message;
	public MsgApiSendRecord getMsgApiSendRecord() {
		return msgApiSendRecord;
	}
	public void setMsgApiSendRecord(MsgApiSendRecord msgApiSendRecord) {
		this.msgApiSendRecord = msgApiSendRecord;
	}
	public List<Message> getMessage() {
		return message;
	}
	public void setMessage(List<Message> message) {
		this.message = message;
	}
}
