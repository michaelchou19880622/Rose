package com.bcs.core.bot.db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.bot.db.entity.MsgBotReceive;
import com.bcs.core.bot.db.repository.MsgBotReceiveRepository;

@Service
public class MsgBotReceiveService {
	@Autowired
	private MsgBotReceiveRepository msgBotReceiveRepository;

	public void save(MsgBotReceive msgReceive){
		msgBotReceiveRepository.save(msgReceive);
	}
	
	public Page<MsgBotReceive> findAll(Pageable pageable){
		return msgBotReceiveRepository.findAll(pageable);
	}
	
	public Page<MsgBotReceive> findByUserStatus(String userStatus, Pageable pageable){
		return msgBotReceiveRepository.findByUserStatus(userStatus, pageable);
	}
	
	public void bulkPersist(List<MsgBotReceive> msgReceives){
		msgBotReceiveRepository.bulkPersist(msgReceives);
	}
	
	public void bulkPersist(MsgBotReceive msgReceive){
		msgBotReceiveRepository.bulkPersist(msgReceive);
	}
	
	public Long countReceive(String start, String end){
		return msgBotReceiveRepository.countReceive(start, end);
	}
	
	public List<Object[]> countReceiveByReferenceId(String referenceId, String start, String end){
		return msgBotReceiveRepository.countReceiveByReferenceId(referenceId, start, end);
	}
	
	public List<Object[]> countReceiveByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus){
		return msgBotReceiveRepository.countReceiveByReferenceIdAndStatus(referenceId, start, end, userStatus);
	}
	
	public List<String> findReceiveMidByReferenceIdAndStatus(String referenceId, String start, String end, String userStatus){
		return msgBotReceiveRepository.findReceiveMidByReferenceIdAndStatus(referenceId, start, end, userStatus);
	}
	
	public Long countReceiveByType(String start, String end, String eventType){
		return msgBotReceiveRepository.countReceiveByType(start, end, eventType);
	}
	
	public List<String> findReferenceId(String start, String end){
	    return msgBotReceiveRepository.findReferenceId(start, end);
	}
	
	public List<MsgBotReceive> findByReceiveDay(String start, String end){
	    return msgBotReceiveRepository.findByReceiveDay(start, end);
	}
}
