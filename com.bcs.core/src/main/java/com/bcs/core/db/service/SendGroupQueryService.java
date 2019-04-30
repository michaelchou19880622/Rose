package com.bcs.core.db.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SendGroupQuery;
import com.bcs.core.db.repository.SendGroupQueryRepository;

@Service
public class SendGroupQueryService {
	/** Logger */
	private static Logger logger = Logger.getLogger(SendGroupQueryService.class);
	
	@Autowired
	private SendGroupQueryRepository sendGroupQueryRepository;

	public boolean exists(String queryFieldId) {
		return sendGroupQueryRepository.exists(queryFieldId);
	}
	
	public List<SendGroupQuery> findAll(){
		return sendGroupQueryRepository.findAll();
	}
	
	public SendGroupQuery findOne(String queryFieldId){
		return sendGroupQueryRepository.findOne(queryFieldId);
	}
	
	public void save(SendGroupQuery sendGroupQuery){
		sendGroupQueryRepository.save(sendGroupQuery);
	}
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void delete(String queryFieldId){
		logger.debug("queryFieldId:" + queryFieldId);
		SendGroupQuery sendGroupQuery = sendGroupQueryRepository.findOne(queryFieldId);
		
		sendGroupQueryRepository.delete(sendGroupQuery);
	}
}
