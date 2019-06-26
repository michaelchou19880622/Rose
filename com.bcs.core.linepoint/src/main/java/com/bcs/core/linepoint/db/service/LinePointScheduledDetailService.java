package com.bcs.core.linepoint.db.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.linepoint.db.entity.LinePointScheduledDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.repository.LinePointScheduledDetailRepository;
import com.bcs.core.linepoint.db.repository.LinePointMainRepository;

@Service
public class LinePointScheduledDetailService {
	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointScheduledDetailService.class);
	@Autowired
	private LinePointScheduledDetailRepository linePointScheduledDetailRepository;

    @PersistenceContext
    EntityManager entityManager;
    
	public void save(LinePointScheduledDetail linePointScheduledDetail){
		linePointScheduledDetailRepository.save(linePointScheduledDetail);
	}
    
	public LinePointScheduledDetail findOne(Long msgId){
		return linePointScheduledDetailRepository.findOne(msgId);
	}
	
	public List<LinePointScheduledDetail> findByLinePointMainId(Long linePointMainId){
		return linePointScheduledDetailRepository.findByLinePointMainId(linePointMainId);
	}
	
	public void delete(LinePointScheduledDetail linePointScheduledDetail) {
		linePointScheduledDetailRepository.delete(linePointScheduledDetail);
	}
	public List<LinePointScheduledDetail> findAll(Long mainId){
		return linePointScheduledDetailRepository.findByLinePointMainId(mainId);
	}	
	
//	public LinePointDetail findBySerialId(String serialId){
//		return linePointDetailRepository.findBySerialId(serialId);
//	}
//		
//	public List<LinePointDetail> findByStatus(String status){
//		return linePointDetailRepository.findByStatus(status);
//	}	
//
//	public List<LinePointDetail> findByMsgLpId(long msgLpId){
//		return linePointDetailRepository.findByMsgLpId(msgLpId);
//	}	
//	
//	public List<LinePointDetail> findByMsgLpIdAndEmptyUid(long msgLpId){
//		return linePointDetailRepository.findByMsgLpIdAndEmptyUid(msgLpId);
//	}
//	
//	public List<LinePointDetail> findBySerialIdAndEmptyUid(String serialId)
//	{
//	   return this.linePointDetailRepository.findBySerialIdAndEmptyUid(serialId);
//	}
//	  
//	public void updateUID(Long msgLpId,String serialID, String uid){
//		linePointDetailRepository.updateUidByByMsgLpIdAndSerialId(msgLpId, serialID, uid );
//	}
}
