package com.bcs.core.linepoint.db.service;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.akka.service.AkkaCoreService;
import com.bcs.core.linepoint.db.entity.LinePointSendGroupDetail;
import com.bcs.core.linepoint.db.repository.LinePointGroupGenerateRepository;

@Service
public class LinePointGroupGenerateService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointGroupGenerateService.class);
	@Autowired
	private AkkaCoreService akkaService;
	@Autowired
	private LinePointGroupGenerateRepository groupGenerateRepository;

	public BigInteger findMIDCountBySendGroupDetail(List<LinePointSendGroupDetail> sendGroupDetails) throws Exception {
		logger.debug("findMIDCountBySendGroupDetail");
		return groupGenerateRepository.findMIDCountBySendGroupDetail(sendGroupDetails);
	}

	public List<String> findMIDBySendGroupDetailGroupId(Long groupId) throws Exception{
		logger.info("findMIDBySendGroupDetailGroupId");
		List<String> list = groupGenerateRepository.findMIDBySendGroupDetailGroupId(groupId);
		logger.info("list:"+list);
		return list;
	}
	
	public Boolean checkMIDBySendGroupDetailGroupId(Long groupId, String mid) throws Exception{
		String result = groupGenerateRepository.checkMIDBySendGroupDetailGroupId(groupId, mid);
		logger.debug("checkMIDBySendGroupDetailGroupId:" + result);
		if(StringUtils.isBlank(result)){
			return false;
		}
		else{
			return true;
		}
	}

	public List<String> findMIDBySendGroupDetail(List<LinePointSendGroupDetail> sendGroupDetails) throws Exception{
		logger.debug("findMIDBySendGroupDetail");
		return groupGenerateRepository.findMIDBySendGroupDetail(sendGroupDetails);
	}
}
