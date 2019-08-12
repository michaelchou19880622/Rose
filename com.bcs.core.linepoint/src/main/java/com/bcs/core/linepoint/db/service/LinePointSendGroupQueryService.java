package com.bcs.core.linepoint.db.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.linepoint.db.entity.LinePointSendGroupQuery;
import com.bcs.core.linepoint.db.repository.LinePointSendGroupQueryRepository;


@Service
public class LinePointSendGroupQueryService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointSendGroupQueryService.class);
	
	@Autowired
	private LinePointSendGroupQueryRepository sendGroupQueryRepository;

	public boolean exists(String queryFieldId) {
		return sendGroupQueryRepository.exists(queryFieldId);
	}
	
	public List<LinePointSendGroupQuery> findAll(){
		return sendGroupQueryRepository.findAll();
	}
	
	public LinePointSendGroupQuery findOne(String queryFieldId){
		return sendGroupQueryRepository.findOne(queryFieldId);
	}
	
	public void save(LinePointSendGroupQuery sendGroupQuery){
		sendGroupQueryRepository.save(sendGroupQuery);
	}
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void delete(String queryFieldId){
		logger.debug("queryFieldId:" + queryFieldId);
		LinePointSendGroupQuery sendGroupQuery = sendGroupQueryRepository.findOne(queryFieldId);
		
		sendGroupQueryRepository.delete(sendGroupQuery);
	}
}
