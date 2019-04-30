package com.bcs.core.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.SendGroupQueryTag;
import com.bcs.core.db.repository.SendGroupQueryTagRepository;

@Service
public class SendGroupQueryTagService {
	@Autowired
	private SendGroupQueryTagRepository sendGroupQueryTagRepository;

	public long countBySendGroupQueryQueryFieldIdAndQueryFieldTagValue(String queryFieldId, String queryFieldTagValue) {
		return sendGroupQueryTagRepository.countBySendGroupQueryQueryFieldIdAndQueryFieldTagValue(queryFieldId, queryFieldTagValue);
	}
	
	public void save(SendGroupQueryTag sendGroupQueryTag){
		sendGroupQueryTagRepository.save(sendGroupQueryTag);
	}
}
