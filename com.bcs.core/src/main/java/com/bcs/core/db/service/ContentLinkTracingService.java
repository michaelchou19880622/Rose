package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentLinkTracing;
import com.bcs.core.db.repository.ContentLinkTracingRepository;
import com.bcs.core.utils.ErrorRecord;

@Service
public class ContentLinkTracingService {
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentLinkTracingService.class);
	
	@Autowired
	private ContentLinkTracingRepository contentLinkTracingRepository;
	
	public ContentLinkTracingService(){}
	public void save(ContentLinkTracing contentLinkTracing){
		contentLinkTracingRepository.save(contentLinkTracing);

		if(contentLinkTracing != null){}
	}
	
	public ContentLinkTracing findOne(Long tracingId){
		ContentLinkTracing result = contentLinkTracingRepository.findOne(tracingId);
		if(result != null){}
		return result;
	}
	
	public List<ContentLinkTracing> findAll(){
		return contentLinkTracingRepository.findAll(new Sort(Sort.Direction.DESC, "modifyTime"));
	}
	
	public List<ContentLinkTracing> findAll(Sort sort){
		return contentLinkTracingRepository.findAll(sort);
	}
	
	public List<Object[]> findListByFlag(String flag, int offset, int recordNum){
		try {
			if(StringUtils.isBlank(flag))
		        return contentLinkTracingRepository.findListByPageNo(offset, recordNum);
			else
				return contentLinkTracingRepository.findListByFlag(offset, recordNum, "%" + flag + "%");
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
		}
		return new ArrayList<Object[]>();
	}
}
