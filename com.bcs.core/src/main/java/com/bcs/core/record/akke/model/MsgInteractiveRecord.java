package com.bcs.core.record.akke.model;

import com.bcs.core.json.AbstractBcsEntity;

public class MsgInteractiveRecord extends AbstractBcsEntity{
	private static final long serialVersionUID = 1L;
	
	private Long iMsgId;
	
	public MsgInteractiveRecord(Long iMsgId){
		this.iMsgId = iMsgId;
	}

	public Long getiMsgId() {
		return iMsgId;
	}

}
