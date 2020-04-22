package com.bcs.core.db.service;

import com.bcs.core.db.entity.SendGroupDetail;
import com.bcs.core.db.repository.GroupGenerateRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class GroupGenerateService {

    @Autowired
    private GroupGenerateRepository groupGenerateRepository;

    public BigInteger findMIDCountBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception {
        log.debug("findMIDCountBySendGroupDetail");
        return groupGenerateRepository.findMIDCountBySendGroupDetail(sendGroupDetails);
    }

    public List<String> findMIDBySendGroupDetailGroupId(Long groupId) throws Exception {
        log.debug("findMIDBySendGroupDetailGroupId");
        return groupGenerateRepository.findMIDBySendGroupDetailGroupId(groupId);
    }

    public Boolean checkMIDBySendGroupDetailGroupId(Long groupId, String mid) throws Exception {
        String result = groupGenerateRepository.checkMIDBySendGroupDetailGroupId(groupId, mid);
        log.info("checkMIDBySendGroupDetailGroupId:" + result);
		return !StringUtils.isBlank(result);
    }

    public Boolean checkMIDBySendGroupDetailGroupIdForKeywordResponse(Long groupId, String mid) throws Exception {
        String result = groupGenerateRepository.checkMIDBySendGroupDetailGroupIdForKeywordResponse(groupId, mid);
        log.info("checkMIDBySendGroupDetailGroupId:" + result);
		return !StringUtils.isBlank(result);
    }

    public List<String> findMIDBySendGroupDetail(List<SendGroupDetail> sendGroupDetails) throws Exception {
        log.debug("findMIDBySendGroupDetail");
        return groupGenerateRepository.findMIDBySendGroupDetail(sendGroupDetails);
    }
}
