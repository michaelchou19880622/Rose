package com.bcs.core.db.repository;


public interface MsgSendMainRepositoryCustom{

	public void increaseSendCountByMsgSendIdAndCheck(Long msgSendId, Long increase );
}
