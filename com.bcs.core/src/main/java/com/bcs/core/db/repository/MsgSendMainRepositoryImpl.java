package com.bcs.core.db.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgSendMain;

@Repository
public class MsgSendMainRepositoryImpl  implements MsgSendMainRepositoryCustom {
	@Autowired
	private MsgSendMainRepository msgSendMainRepository;

	@Override
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void increaseSendCountByMsgSendIdAndCheck(Long msgSendId, Long increase ){
		msgSendMainRepository.increaseSendCountByMsgSendId(msgSendId, increase);
		MsgSendMain msg = msgSendMainRepository.findOne(msgSendId);
		if(msg != null && msg.getSendTotalCount() <= msg.getSendCount()){
			msg.setStatus(MsgSendMain.MESSAGE_STATUS_FINISH);
			msgSendMainRepository.save(msg);
		}
	}
}
