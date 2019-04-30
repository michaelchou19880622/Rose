package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.repository.MsgDetailRepository;
import com.bcs.core.db.repository.MsgMainRepository;

@Service
public class MsgMainService {
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgMainService.class);
	@Autowired
	private MsgMainRepository msgMainRepository;
	@Autowired
	private MsgDetailRepository msgDetailRepository;

    @PersistenceContext
    EntityManager entityManager;
    
	public void delete(Long msgId){
		MsgMain main = msgMainRepository.findOne(msgId);
		main.setModifyTime(new Date());
		main.setStatus(MsgMain.MESSAGE_STATUS_DELETE);
		this.save(main);
	}
    
	public void save(MsgMain msgMain){
		msgMainRepository.save(msgMain);
	}
    
	public MsgMain findOne(Long msgId){
		return msgMainRepository.findOne(msgId);
	}
    
	public List<MsgMain> findByStatus(String status){
		return msgMainRepository.findByStatus(status);
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgMain, List<MsgDetail>> queryGetMsgMainDetailByMsgId(Long msgId){
		Query query = entityManager.createNamedQuery("queryGetMsgMainDetailByMsgId").setParameter(1, msgId);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgMain, List<MsgDetail>> queryGetMsgMainDetailByStatus(String status){
		Query query = entityManager.createNamedQuery("queryGetMsgMainDetailByStatus").setParameter(1, status);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgMain, List<MsgDetail>> queryGetMsgMainDetailByStatusAndSendType(String status, String sendType){
		Query query = entityManager.createNamedQuery("queryGetMsgMainDetailByStatusAndSendType").setParameter(1, status).setParameter(2, sendType);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	private Map<MsgMain, List<MsgDetail>> parseListToMap(List<Object[]> list){

		Map<MsgMain, List<MsgDetail>> map = new LinkedHashMap<MsgMain, List<MsgDetail>>();

	    for(Object[] o : list){
	    	logger.debug("length:" + o.length);
	    	logger.debug(o[0]);
	    	if(o[0] !=null){
	    		List<MsgDetail> details = map.get(o[0]);
	    		if(details == null){
	    			map.put((MsgMain) o[0], new ArrayList<MsgDetail>());
	    		}
	    	}
	    	logger.debug(o[1]);
	    	if(o[1] != null){
	    		List<MsgDetail> details = map.get(o[0]);
	    		details.add((MsgDetail) o[1]);
	    	}
	    }
	    
	    return map;
	}
}
