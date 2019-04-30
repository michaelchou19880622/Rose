package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.repository.ContentFlagRepository;
import com.bcs.core.db.repository.EntityManagerControl;

@Service
public class ContentFlagService {
	
	@Autowired
	private ContentFlagRepository contentFlagRepository;
	
	@Autowired
	private EntityManagerControl entityManagerControl;
	
	/**
	 * 組合多個標籤成一串文字
	 * 
	 * @param flagValueList
	 * @return
	 */
	public String concat(List<String> flagValueList, int maxlength) {
		if (CollectionUtils.isEmpty(flagValueList)) {
			return null;
		}
		
		return StringUtils.left(StringUtils.join(flagValueList, "/"), maxlength);
	}
	
	/**
	 * 查詢指定的 referenceId、contentType 的標籤
	 * 
	 * @param referenceId
	 * @param contentType
	 * @return
	 */
	public List<String> findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(String referenceId, String contentType) {
		return contentFlagRepository.findFlagValueByReferenceIdAndContentTypeOrderByFlagValueAsc(referenceId, contentType);
	}
	
	/**
	 * 查詢指定的 contentType 的不重複的相似標籤(flagValue like xxx)
	 * 
	 * @param flagValue
	 * @param contentType
	 * @return
	 */
	public List<String> findDistinctFlagValueByFlagValueLikeAndContentTypeOrderByFlagValueAsc(String flagValue, String contentType) {
		return contentFlagRepository.findDistinctFlagValueByFlagValueLikeAndContentTypeOrderByFlagValueAsc("%" + flagValue + "%", contentType);
	}
	
	/**
	 * 查詢不重複的相似標籤(flagValue like xxx)
	 * 
	 * @param flagValue
	 * @return
	 */
	public List<String> findDistinctFlagValueByFlagValueLikeOrderByFlagValueAsc(String flagValue) {
		return contentFlagRepository.findDistinctFlagValueByFlagValueLikeOrderByFlagValueAsc("%" + flagValue + "%");
	}
	
	/**
	 * 查詢指定的 contentType 的不重複的標籤
	 * 
	 * @param contentType
	 * @return
	 */
	public List<String> findDistinctFlagValueByContentTypeOrderByFlagValueAsc(String contentType) {
		return contentFlagRepository.findDistinctFlagValueByContentTypeOrderByFlagValueAsc(contentType);
	}
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void save(String referenceId, String contentType, List<String> flagValueList) {
		
		// 先刪除舊的標籤再新增
		contentFlagRepository.deleteByReferenceIdAndContentType(referenceId, contentType);
		
		if (CollectionUtils.isEmpty(flagValueList)) {
			return;
		}
		
		List<Object> contentFlagList = new ArrayList<>();
		
		for (String flagValue : flagValueList) {
			ContentFlag contentFlag = new ContentFlag();
			contentFlag.setReferenceId(referenceId);
			contentFlag.setContentType(contentType);
			contentFlag.setFlagValue(flagValue.toLowerCase()); // 全部轉小寫
			contentFlagList.add(contentFlag);
		}
		
		entityManagerControl.persistInsert(contentFlagList);
	}
}
