package com.bcs.core.linepoint.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.linepoint.db.entity.LinePointSendGroupQueryTag;
import com.bcs.core.linepoint.db.repository.LinePointSendGroupQueryTagRepository;

@Service
public class LinePointSendGroupQueryTagService {
	@Autowired
	private LinePointSendGroupQueryTagRepository sendGroupQueryTagRepository;

	public long countBySendGroupQueryQueryFieldIdAndQueryFieldTagValue(String queryFieldId, String queryFieldTagValue) {
		return sendGroupQueryTagRepository.countBySendGroupQueryQueryFieldIdAndQueryFieldTagValue(queryFieldId, queryFieldTagValue);
	}
	
	public void save(LinePointSendGroupQueryTag sendGroupQueryTag){
		sendGroupQueryTagRepository.save(sendGroupQueryTag);
	}
}
