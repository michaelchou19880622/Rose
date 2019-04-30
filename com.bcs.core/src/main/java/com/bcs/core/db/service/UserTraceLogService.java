package com.bcs.core.db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.UserTraceLog;
import com.bcs.core.db.repository.UserTraceLogRepository;

@Service
public class UserTraceLogService {
	
	@Autowired
	private UserTraceLogRepository userTraceLogRepository;
	
	public void save(UserTraceLog userTraceLog){
		userTraceLogRepository.save(userTraceLog);
	}
	
	public Page<UserTraceLog> findAll(Pageable pageable){
		return userTraceLogRepository.findAll(pageable);
	}
	
	public Page<UserTraceLog> findByTargetAndAction(String target, String action, Pageable pageable){
		return userTraceLogRepository.findByTargetAndAction(target, action, pageable);
	}
	
	public Page<UserTraceLog> findByReferenceId(String referenceId, Pageable pageable){
		return userTraceLogRepository.findByReferenceId(referenceId, pageable);
	}
	
	public Page<UserTraceLog> findByLevel(String level, Pageable pageable){
		return userTraceLogRepository.findByLevel(level, pageable);
	}
	
	public Page<UserTraceLog> findByModifyUser(String modifyUser, Pageable pageable){
		return userTraceLogRepository.findByModifyUser(modifyUser, pageable);
	}
	
	public void bulkPersist(UserTraceLog userTraceLog){
		userTraceLogRepository.bulkPersist(userTraceLog);
	}
	
	public Long countBinded2Block(){
		return userTraceLogRepository.countBinded2Block();
	}
	
	public Long countBinded2Block(String start, String end){
		return userTraceLogRepository.countBinded2Block(start, end);
	}
	
	public Long countByLevel(String level, String start, String end){
		return userTraceLogRepository.countByLevel(level, start, end);
	}
	
	public Long countByTargetAndAction(String target, String action, String start, String end){
		return userTraceLogRepository.countByTargetAndAction(target, action, start, end);
	}
	
	public Long countAll(String start, String end){
		return userTraceLogRepository.countAll(start, end);
	}
	
	public List<Object[]> countByReferenceIdAndTime(String target, String action, String referenceId, String start, String end){
		return userTraceLogRepository.countByReferenceIdAndTime(target, action, referenceId, start, end);
	}
	
	public List<String> findMidByReferenceId(String referenceId){
		return userTraceLogRepository.findMidByReferenceId(referenceId);
	}
	
	public List<String> findMidByReferenceId(String referenceId, String start){
		return userTraceLogRepository.findMidByReferenceId(referenceId, start);
	}
	
	public List<UserTraceLog> findByModifyUserAndAction(String modifyUser, String action){
	    return userTraceLogRepository.findByModifyUserAndAction(modifyUser, action);
	}
	
	public List<UserTraceLog> findByTargetAndAction(String target, String action, String start, String end){
        return userTraceLogRepository.findByTargetAndAction(target, action, start, end);
    }
	
	public Long countBlock(String start, String end) {
	    return userTraceLogRepository.countBlock(start, end);
	}
	
	public Long countBinded2Block2(String start, String end){
        return userTraceLogRepository.countBinded2Block2(start, end);
    }
}
