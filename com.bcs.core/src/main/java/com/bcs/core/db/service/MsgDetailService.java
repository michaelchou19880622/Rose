package com.bcs.core.db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.repository.MsgDetailRepository;

@Service
public class MsgDetailService {
	@Autowired
	private MsgDetailRepository msgDetailRepository;

	public void save(MsgDetail msgDetail){
		msgDetailRepository.save(msgDetail);
	}
	
	public MsgDetail findOne(Long detailId){
		return msgDetailRepository.findOne(detailId);
	}

    public List<MsgDetail> findByMsgIdAndMsgParentType(Long msgId, String msgParentType){
        return msgDetailRepository.findByMsgIdAndMsgParentType(msgId, msgParentType);
    }

    public List<MsgDetail> findByMsgIdAndEventType(Long msgId, String eventType){
        return msgDetailRepository.findByMsgIdAndEventType(msgId, eventType);
    }
	
	public void deleteByMsgIdAndMsgParentType(Long msgId, String msgParentType){
		 List<MsgDetail> details = msgDetailRepository.findByMsgIdAndMsgParentType(msgId, msgParentType);
		 if(details != null && details.size() > 0){
			 msgDetailRepository.delete(details);
		 }
	}
}
