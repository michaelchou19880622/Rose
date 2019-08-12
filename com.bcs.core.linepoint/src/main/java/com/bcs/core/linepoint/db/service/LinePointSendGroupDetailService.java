package com.bcs.core.linepoint.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.linepoint.db.entity.LinePointSendGroupDetail;
import com.bcs.core.linepoint.db.repository.LinePointSendGroupDetailRepository;

@Service
public class LinePointSendGroupDetailService {
	@Autowired
	private LinePointSendGroupDetailRepository sendGroupDetailRepository;

	public void save(LinePointSendGroupDetail sendGroupDetail){
		sendGroupDetailRepository.save(sendGroupDetail);
	}
}
