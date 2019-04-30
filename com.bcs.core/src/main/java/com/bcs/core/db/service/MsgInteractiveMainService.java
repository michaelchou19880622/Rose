package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.repository.MsgDetailRepository;
import com.bcs.core.db.repository.MsgInteractiveMainRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class MsgInteractiveMainService {
	public static final String INTERACTIVE_MAIN_SYNC = "INTERACTIVE_MAIN_SYNC";
	
	private static final String INIT_FLAG = "INIT_FLAG";
	/** Logger */
	private static Logger logger = Logger.getLogger(MsgInteractiveMainService.class);
	@Autowired
	private MsgInteractiveMainRepository msgInteractiveMainRepository;
	@Autowired
	private MsgDetailRepository msgDetailRepository;

	protected LoadingCache<Long, MsgInteractiveMain> dataCache;
	
	private ConcurrentMap<Long, AtomicLong> increaseMap = new ConcurrentHashMap<Long, AtomicLong>();

	private Timer flushTimer = new Timer();
	
    @PersistenceContext
    EntityManager entityManager;

	public MsgInteractiveMainService(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<Long, MsgInteractiveMain>() {
					@Override
					public MsgInteractiveMain load(Long key) throws Exception {
						return new MsgInteractiveMain();
					}
				});
		
		flushTimer.schedule(new CustomTask(), 120000, 30000);
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] MsgInteractiveMainService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Throwable e){}
		
		flushTimer.cancel();
		
		System.gc();
		logger.info("[DESTROY] MsgInteractiveMainService destroyed.");
	}
	
	private boolean notNull(MsgInteractiveMain result){
		if(result != null && result.getiMsgId() != null){
			return true;
		}
		return false;
	}
    
	public MsgInteractiveMain findOne(Long iMsgId){
		try {
			MsgInteractiveMain result = dataCache.get(iMsgId);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		MsgInteractiveMain result = msgInteractiveMainRepository.findOne(iMsgId);
		if(result != null){
			dataCache.put(iMsgId, result);
		}
		return result;
	}
    
	public List<MsgInteractiveMain> findByInteractiveTypeAndInteractiveStatus(String interactiveType, String interactiveStatus){
		return msgInteractiveMainRepository.findByInteractiveTypeAndInteractiveStatus(interactiveType, interactiveStatus);
	}
    
	public void save(MsgInteractiveMain msgInteractiveMain){
		msgInteractiveMainRepository.save(msgInteractiveMain);

		if(msgInteractiveMain != null){
			dataCache.put(msgInteractiveMain.getiMsgId(), msgInteractiveMain);
			DataSyncUtil.settingReSync(INTERACTIVE_MAIN_SYNC);
		}
	}

	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				flushIncrease();
				
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(INTERACTIVE_MAIN_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(INTERACTIVE_MAIN_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
	
	public void increaseSendCountByMsgInteractiveId(Long iMsgId){
		synchronized (INIT_FLAG) {
			if(increaseMap.get(iMsgId) == null){
				increaseMap.put(iMsgId, new AtomicLong(1L));
			}
			else{
				increaseMap.get(iMsgId).addAndGet(1);
			}
		}
	}
	
	public void flushIncrease(){
		synchronized (INIT_FLAG) {
			logger.debug("MsgInteractiveMainService flushTimer execute");
			for(Map.Entry<Long, AtomicLong> map : increaseMap.entrySet()){
				if(map.getValue().longValue() != 0){
					logger.debug("MsgInteractiveMainService flushTimer execute:" + map.getKey() + "," + map.getValue().longValue());
					this.increaseSendCountByMsgInteractiveId(map.getKey(), map.getValue().longValue());
					map.getValue().set(0);
				}
			}
			logger.debug("MsgInteractiveMainService flushTimer end");
		}
	}

	private void increaseSendCountByMsgInteractiveId(Long msgSendId, Long increase ){
		msgInteractiveMainRepository.increaseSendCountByMsgInteractiveId(msgSendId, increase);
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgInteractiveMain, List<MsgDetail>> queryGetMsgInteractiveMainDetailByMsgId(Long iMsgId){
		Query query = entityManager.createNamedQuery("queryGetMsgInteractiveMainDetailByMsgId").setParameter(1, iMsgId);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgInteractiveMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgInteractiveMain, List<MsgDetail>> queryGetMsgInteractiveMainDetailByType(String type){
		Query query = entityManager.createNamedQuery("queryGetMsgInteractiveMainDetailByType").setParameter(1, type);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgInteractiveMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgInteractiveMain, List<MsgDetail>> queryGetMsgInteractiveMainDetailByTypeAndStatus(String type, String status){
		Query query = entityManager.createNamedQuery("queryGetMsgInteractiveMainDetailByTypeAndStatus").setParameter(1, type).setParameter(2, status);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgInteractiveMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public Map<MsgInteractiveMain, List<MsgDetail>> queryGetMsgInteractiveMainDetailAll(){
		Query query = entityManager.createNamedQuery("queryGetMsgInteractiveMainDetailAll");
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<MsgInteractiveMain, List<MsgDetail>> map = parseListToMap(list);
    	logger.debug(map);
		
		return map;
	}
	
	private Map<MsgInteractiveMain, List<MsgDetail>> parseListToMap(List<Object[]> list){

		Map<MsgInteractiveMain, List<MsgDetail>> map = new LinkedHashMap<MsgInteractiveMain, List<MsgDetail>>();

	    for(Object[] o : list){
	    	logger.debug("length:" + o.length);
	    	logger.debug(o[0]);
	    	if(o[0] !=null){
	    		List<MsgDetail> details = map.get(o[0]);
	    		if(details == null){
	    			map.put((MsgInteractiveMain) o[0], new ArrayList<MsgDetail>());
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
	
	public List<String> findIMsgIdByKeyword(String keyword){
	    return msgInteractiveMainRepository.findIMsgIdByKeyword(keyword);
	}
}
