package com.bcs.core.db.service;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.akka.service.AkkaCoreService;
import com.bcs.core.db.entity.SendGroupDetail;
import com.bcs.core.db.repository.GroupGenerateRepository;

@Service
public class GroupGenerateService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(GroupGenerateService.class);
	@Autowired
	private AkkaCoreService akkaService;
	@Autowired
	private GroupGenerateRepository groupGenerateRepository;

	public BigInteger findMIDCountBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception {
		logger.debug("findMIDCountBySendGroupDetail");
		return groupGenerateRepository.findMIDCountBySendGroupDetail(sendGroupDetails);
	}

	public List<String> findMIDBySendGroupDetailGroupId(Long groupId) throws Exception{
		logger.debug("findMIDBySendGroupDetailGroupId");
		return groupGenerateRepository.findMIDBySendGroupDetailGroupId(groupId);
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

	public List<String> findMIDBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception{
		logger.debug("findMIDBySendGroupDetail");
		return groupGenerateRepository.findMIDBySendGroupDetail(sendGroupDetails);
	}
}
