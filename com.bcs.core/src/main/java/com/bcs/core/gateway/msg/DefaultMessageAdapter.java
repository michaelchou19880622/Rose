package com.bcs.core.gateway.msg;

import java.util.ArrayList;

import com.bcs.core.db.entity.MsgDetail;

public class DefaultMessageAdapter extends MessageAdapter {
	private static final long serialVersionUID = 1L;
	
	private MsgDetail message = null;
	
	public DefaultMessageAdapter() {
		messageList = new ArrayList<MsgDetail>();
		
		message = noAnswerReply();
		
		messageList.add(this.message);
	}
}
