package com.bcs.core.db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.MsgSendRecord;
import com.bcs.core.db.repository.MsgSendRecordRepository;

@Service
public class MsgSendRecordService {
	@Autowired
	private MsgSendRecordRepository msgSendRecordRepository;

	public void save(MsgSendRecord msgSend){
		msgSendRecordRepository.save(msgSend);
	}
	
	public List<MsgSendRecord> findByMsgSendId(Long msgSendId){
		return msgSendRecordRepository.findByMsgSendId(msgSendId);
	}
	
	public void bulkPersist(MsgSendRecord msgSendRecord){
		msgSendRecordRepository.bulkPersist(msgSendRecord);
	}
}
