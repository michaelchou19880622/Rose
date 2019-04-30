package com.bcs.core.db.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.repository.LineUserRepository;
import com.bcs.core.enums.LOG_TARGET_ACTION_TYPE;

@Service
public class LineUserService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(LineUserService.class);

//	protected LoadingCache<String, LineUser> dataCache;
	
	public LineUserService(){

//		dataCache = CacheBuilder.newBuilder()
//				.concurrencyLevel(1)
//				.expireAfterAccess(30, TimeUnit.MINUTES)
//				.build(new CacheLoader<String, LineUser>() {
//					@Override
//					public LineUser load(String key) throws Exception {
//						return new LineUser("-");
//					}
//				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] LineUserService cleaning up...");
		try{
//			if(dataCache != null){
//				dataCache.invalidateAll();
//				dataCache = null;
//			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] LineUserService destroyed.");
	}
	
	@Autowired
	private LineUserRepository lineUserRepository;
	@Autowired
	private UserTraceLogService userTraceLogService;
	
	public List<String> findMidByMidIn(List<String> mids) {
		return lineUserRepository.findMidByMidIn(mids);
	}
	
	public List<String> findMidByMidInAndActive(List<String> mids) {
		return lineUserRepository.findMidByMidInAndActive(mids);
	}
	
//	private boolean notNull(LineUser result){
//		if(result != null && StringUtils.isNotBlank(result.getMid()) && !"-".equals(result.getMid())){
//			return true;
//		}
//		return false;
//	}
	
	public LineUser findByMid(String mid) {
//		try {
//			LineUser result = dataCache.get(mid);
//			if(notNull(result)){
//				return result;
//			}
//		} catch (Exception e) {}
		
		LineUser result = lineUserRepository.findByMid(mid);
		if(result != null){
//			dataCache.put(mid, result);
		}
		return result;
	}
	
	public LineUser findByMidAndCreateUnbind(String mid) {
		LineUser lineUser = findByMid(mid);
		if(lineUser == null){
			
			Date time = new Date();

			lineUser = new LineUser();
			lineUser.setMid(mid);
			lineUser.setStatus(LineUser.STATUS_UNBIND);
			lineUser.setIsBinded(LineUser.STATUS_UNBIND);
			lineUser.setModifyTime(time);
			lineUser.setCreateTime(time);
			lineUser.setSoureType("user");
			
			save(lineUser);
		}
		
		return lineUser;
	}

	public List<LineUser> findAll() {
		return lineUserRepository.findAll();
	}

	public Page<String> findMIDAllActive(int page , int pageSize) {
		Pageable pageable = new PageRequest(page, pageSize);
		
		return lineUserRepository.findMIDAllActive(pageable);
	}
	
	public Boolean checkMIDAllActive(String mid) {
		
		String result =  lineUserRepository.checkMIDAllActive(mid);
		logger.debug("checkMIDAllActive:" + result);
		if(StringUtils.isBlank(result)){
			return false;
		}
		else{
			return true;
		}
	}

	public Long countAll() {
		return lineUserRepository.count();
	}

	public Long countByStatus(String status) {
		return lineUserRepository.countByStatus(status);
	}

	public Long countByStatus(String status, String start, String end) {
		return lineUserRepository.countByStatus(status, start, end);
	}
	
	public List<LineUser> findByStatus(String status) {
		
		return lineUserRepository.findByStatus(status);
	}

	public Page<String> findMIDByStatus(String status, int page, int pageSize) {
		Pageable pageable = new PageRequest(page, pageSize);
		
		return lineUserRepository.findMIDByStatus(status, pageable);
	}
	
	public Boolean checkMIDByStatus(String status, String mid) {
		
		String result =  lineUserRepository.checkMIDByStatus(status, mid);
		logger.debug("checkMIDByStatus:" + result);
		if(StringUtils.isBlank(result)){
			return false;
		}
		else{
			return true;
		}
	}
	
	public void saveLog(LineUser lineUser, String mid, LOG_TARGET_ACTION_TYPE action, String referenceId){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		
		UserTraceLog log = new UserTraceLog();
		log.setTarget(LOG_TARGET_ACTION_TYPE.TARGET_LineUser);
		log.setAction(action);
		log.setModifyUser(mid);
		log.setModifyTime(now);
		log.setLevel(UserTraceLog.USER_TRACE_LOG_LEVEL_TRACE);
		log.setModifyDay(sdf.format(now));

		log.setContent(lineUser);
		log.setReferenceId(referenceId);
		userTraceLogService.bulkPersist(log);
//		akkaCoreService.recordMsgs(log);
	}
	
	public void save(LineUser lineUser) {
		lineUserRepository.save(lineUser);

		if(lineUser != null){
//			dataCache.put(lineUser.getMid(), lineUser);
		}
	}
	
	public void bulkPersist(List<LineUser> lineUsers){
		lineUserRepository.bulkPersist(lineUsers);
	}
	
	public List<LineUser> findByMobileAndBirthday(String mobile, String birthday){
		return lineUserRepository.findByMobileAndBirthday(mobile, birthday);
	}
	
	public List<LineUser> findByCreateTime(String start, String end){
	    return lineUserRepository.findByCreateTime(start, end);
	}
}
