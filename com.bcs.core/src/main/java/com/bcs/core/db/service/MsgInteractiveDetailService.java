package com.bcs.core.db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.MsgInteractiveDetail;
import com.bcs.core.db.repository.MsgInteractiveDetailRepository;

@Service
public class MsgInteractiveDetailService {
	@Autowired
	private MsgInteractiveDetailRepository msgInteractiveDetailRepository;

	public void save(MsgInteractiveDetail msgInteractiveDetail){
		msgInteractiveDetailRepository.save(msgInteractiveDetail);
	}

	public List<MsgInteractiveDetail> findByiMsgId(Long iMsgId){
		return msgInteractiveDetailRepository.findByiMsgId(iMsgId);
	}
	
	public void deleteByiMsgId(Long iMsgId){
		 List<MsgInteractiveDetail> details = msgInteractiveDetailRepository.findByiMsgId(iMsgId);
		 if(details != null && details.size() > 0){
			 msgInteractiveDetailRepository.delete(details);
		 }
	}
}
