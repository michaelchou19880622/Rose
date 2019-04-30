package com.bcs.core.gateway.msg;

import java.util.List;

import com.bcs.core.api.msg.MsgGenerator;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.json.AbstractBcsEntity;

public abstract class MessageAdapter extends AbstractBcsEntity implements MessageAdapterInterface {
	private static final long serialVersionUID = 1L;
	
	protected List<MsgDetail> messageList;
	
	public List<MsgDetail> getMessageList() {
		return this.messageList;
	}
	
	protected MsgDetail noAnswerReply() {
		MsgDetail sendMsg = new MsgDetail();
		
		sendMsg.setMsgType(MsgGenerator.MSG_TYPE_TEXT);
		sendMsg.setText("這個問題沒有合適的答案，建議您換個不同的問法，或許就能為您解答");
		
		return sendMsg;
	}
	
	public void appendMessage(MsgDetail message) {
		messageList.add(message);
	}
}