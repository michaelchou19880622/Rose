package com.bcs.core.record.akke.model;

import com.bcs.core.json.AbstractBcsEntity;

public class MsgSendRecordModel extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private Long iMsgId;
	private int count;
	
	public MsgSendRecordModel(Long iMsgId){
		this.iMsgId = iMsgId;
	}
	
	public MsgSendRecordModel(Long iMsgId, int count){
		this.iMsgId = iMsgId;
		this.count = count;
	}

	public Long getiMsgId() {
		return iMsgId;
	}

	public int getCount() {
		return count;
	}

}
