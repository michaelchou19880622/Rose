package com.bcs.core.db.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentEsnDetail;
import com.bcs.core.db.repository.ContentEsnDetailRepository;

@Service
public class ContentEsnDetailService {

	@Autowired
	private ContentEsnDetailRepository contentEsnDetailRepository;

	public ContentEsnDetail findOne(Long esnDetailId) {
	    return contentEsnDetailRepository.findOne(esnDetailId);
	}
	
	public void save(ContentEsnDetail detail) {
		contentEsnDetailRepository.save(detail);
	}

	public void save(List<ContentEsnDetail> detailList){
		contentEsnDetailRepository.save(detailList);
	}

	
	public List<ContentEsnDetail> findByEsnId(String esnId){
	    return contentEsnDetailRepository.findByEsnId(esnId);
	}
	
	public int countNotUsedByEsnId(String esnId) {
	    return contentEsnDetailRepository.countNotUsedByEsnId(esnId);
	}
	
	public List<ContentEsnDetail> findNotUsedByEsnId(String esnId, int size){
	    Pageable pageable = new PageRequest(0, size);	    
	    return contentEsnDetailRepository.findNotUsedByEsnId(esnId, pageable).getContent();
	}
	
	public List<ContentEsnDetail> findByEsnIdAndStatusAndUidNotNull(String esnId, String status){
	    return contentEsnDetailRepository.findByEsnIdAndStatusAndUidNotNull(esnId, status);
	}
	
    @Transactional(rollbackFor=Exception.class, timeout = 30)
	public int updateStatusAndSendTimeByDetailIds(String status, Date sendTime, List<Long> detailIds) {
	    return contentEsnDetailRepository.updateStatusAndSendTimeByDetailIds(status, sendTime, detailIds);
	}
}
