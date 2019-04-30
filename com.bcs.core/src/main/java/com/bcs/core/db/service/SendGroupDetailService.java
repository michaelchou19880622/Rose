package com.bcs.core.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.SendGroupDetail;
import com.bcs.core.db.repository.SendGroupDetailRepository;

@Service
public class SendGroupDetailService {
	@Autowired
	private SendGroupDetailRepository sendGroupDetailRepository;

	public void save(SendGroupDetail sendGroupDetail){
		sendGroupDetailRepository.save(sendGroupDetail);
	}
}
