package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgMain;
import com.bcs.core.db.entity.MsgSendMain;
import com.bcs.core.db.repository.MsgDetailRepository;
import com.bcs.core.db.repository.MsgMainRepository;
import com.bcs.core.db.repository.MsgSendMainRepository;
import com.bcs.core.utils.ErrorRecord;

@Service
public class MsgSendMainService {
	private static final String INIT_FLAG = "INIT_FLAG";
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgSendMainService.class);
	@Autowired
	private MsgSendMainRepository msgSendMainRepository;
	@Autowired
	private MsgMainRepository msgMainRepository;
	@Autowired
	private MsgDetailRepository msgDetailRepository;

	private Timer flushTimer = new Timer();
	
	private ConcurrentMap<Long, AtomicLong> increaseMap = new ConcurrentHashMap<Long, AtomicLong>();

    @PersistenceContext
    EntityManager entityManager;
    
    public MsgSendMainService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);
    }

	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				flushIncrease();
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
	
	@PreDestroy
	public void preDestroy(){
		flushTimer.cancel();
		logger.info("[DESTROY] MsgSendMainService flushTimer destroyed");
	}
	
	public MsgSendMain findOne(Long msgSendId){
		return msgSendMainRepository.findOne(msgSendId);
	}
    
	public void delete(Long msgSendId){
		MsgSendMain main = msgSendMainRepository.findOne(msgSendId);
		main.setStatus(MsgSendMain.MESSAGE_STATUS_DELETE);
		this.save(main);
	}
    
	public void save(MsgSendMain msgSendMain){
		msgSendMainRepository.save(msgSendMain);
	}

	public void increaseSendCountByMsgSendId(Long msgSendId){
		synchronized (INIT_FLAG) {
			if(increaseMap.get(msgSendId) == null){
				increaseMap.put(msgSendId, new AtomicLong(1L));
			}
			else{
				increaseMap.get(msgSendId).addAndGet(1);
			}
		}
	}
	
	private void increaseSendCountByMsgSendIdAndCheck(Long msgSendId, Long increase ){
		msgSendMainRepository.increaseSendCountByMsgSendIdAndCheck(msgSendId, increase);
	}

	public void increaseSendCountByMsgSendId(Long msgSendId, Long increase){
		synchronized (INIT_FLAG) {
			if(increaseMap.get(msgSendId) == null){
				increaseMap.put(msgSendId, new AtomicLong(increase));
			}
			else{
				increaseMap.get(msgSendId).addAndGet(increase);
			}
		}
	}

	public void increaseSendCountByMsgSendId(Long msgSendId, int increase){
		synchronized (INIT_FLAG) {
			if(increaseMap.get(msgSendId) == null){
				increaseMap.put(msgSendId, new AtomicLong(increase));
			}
			else{
				increaseMap.get(msgSendId).addAndGet(increase);
			}
		}
	}

	public void flushIncrease(){
		synchronized (INIT_FLAG) {
			logger.debug("MsgSendMainService flushTimer execute");
			for(Map.Entry<Long, AtomicLong> map : increaseMap.entrySet()){
				if(map.getValue().longValue() != 0){
					logger.debug("MsgSendMainService flushTimer execute:" + map.getKey() + "," + map.getValue().longValue());
					this.increaseSendCountByMsgSendIdAndCheck(map.getKey(), map.getValue().longValue());
					map.getValue().set(0);
				}
			}
			logger.debug("MsgSendMainService flushTimer end");
		}
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public MsgSendMain copyFromMsgMain(Long msgId, Long sendTotalCount, String groupTitle){
		return this.copyFromMsgMain(msgId, sendTotalCount, groupTitle, MsgSendMain.MESSAGE_STATUS_PROCESS, null);
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public MsgSendMain copyFromMsgMain(Long msgId, Long sendTotalCount, String groupTitle, String status){
		return this.copyFromMsgMain(msgId, sendTotalCount, groupTitle, status, null);
	}

	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public MsgSendMain copyFromMsgMain(Long msgId, Long sendTotalCount, String groupTitle, String status, String statusNotice){
		logger.debug("copyFromMsgMain:" + msgId);
		
		MsgMain msgMain = msgMainRepository.findOne(msgId);
		
		MsgSendMain msgSendMain = new MsgSendMain();
		
		msgSendMain.setMsgId(msgId);
		msgSendMain.setGroupId(msgMain.getGroupId());
		msgSendMain.setSerialId(msgMain.getSerialId());
		msgSendMain.setGroupTitle(groupTitle);
		msgSendMain.setSendType(msgMain.getSendType());
		msgSendMain.setStatus(status);
		if(StringUtils.isBlank(statusNotice)){
			msgSendMain.setStatusNotice(msgMain.getStatusNotice());
		}
		else{
			msgSendMain.setStatusNotice(statusNotice);
		}
		msgSendMain.setSendTime(new Date());
		msgSendMain.setModifyUser(msgMain.getModifyUser());
		msgSendMain.setSendCount(0L);
		msgSendMain.setSendTotalCount(sendTotalCount);
		msgSendMain.setMsgTag(msgMain.getMsgTag());
		msgSendMain.setScheduleTime(msgMain.getScheduleTime());
		
		save(msgSendMain);
		
		List<MsgDetail> mainDetails = msgDetailRepository.findByMsgIdAndMsgParentType(msgId, MsgMain.THIS_PARENT_TYPE);
		
		for(MsgDetail mainDetail : mainDetails){
			MsgDetail detail = new MsgDetail();

			detail.setMsgId(msgSendMain.getMsgSendId());
			detail.setMsgType(mainDetail.getMsgType());
			detail.setText(mainDetail.getText());
			detail.setMsgParentType(MsgSendMain.THIS_PARENT_TYPE);
			detail.setReferenceId(mainDetail.getReferenceId());
			
			msgDetailRepository.save(detail);
		}
		
		return msgSendMain;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgSendMain, List<MsgDetail>> queryGetMsgSendMainDetailByMsgId(Long msgId){
		Query query = entityManager.createNamedQuery("queryGetMsgSendMainDetailByMsgId").setParameter(1, msgId);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgSendMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgSendMain, List<MsgDetail>> queryGetMsgSendMainDetailByStatus(String Status){
		Query query = entityManager.createNamedQuery("queryGetMsgSendMainDetailByStatus").setParameter(1, Status);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgSendMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgSendMain, List<MsgDetail>> queryGetMsgSendMainDetailAll(){
		Query query = entityManager.createNamedQuery("queryGetMsgSendMainDetailAll");
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgSendMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	private Map<MsgSendMain, List<MsgDetail>> parseListToMap(List<Object[]> list){

		Map<MsgSendMain, List<MsgDetail>> map = new LinkedHashMap<MsgSendMain, List<MsgDetail>>();

	    for(Object[] o : list){
	    	logger.debug("length:" + o.length);
	    	logger.debug(o[0]);
	    	if(o[0] !=null){
	    		List<MsgDetail> details = map.get(o[0]);
	    		if(details == null){
	    			map.put((MsgSendMain) o[0], new ArrayList<MsgDetail>());
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
