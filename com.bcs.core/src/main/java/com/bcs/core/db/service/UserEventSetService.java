package com.bcs.core.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import com.bcs.core.db.entity.UserEventSet;
import com.bcs.core.db.repository.UserEventSetRepository;

@Service
public class UserEventSetService {
	
	@Autowired
	private UserEventSetRepository userEventSetRepository;
	
	public void save(UserEventSet userEventSet){
		userEventSetRepository.save(userEventSet);
	}
	
	public void save(List<UserEventSet> userEventSetList){
		userEventSetRepository.save(userEventSetList);
	}
	
	public Page<UserEventSet> findAll(Pageable pageable){
		return userEventSetRepository.findAll(pageable);
	}

	public Page<UserEventSet> findByTargetAndAction(String target, String action, Pageable pageable){
		return userEventSetRepository.findByTargetAndAction(target, action, pageable);
	}

	public Page<UserEventSet> findByTargetAndActionAndReferenceId(String target, String action, String referenceId, Pageable pageable){
		return userEventSetRepository.findByTargetAndActionAndReferenceId(target, action, referenceId, pageable);
	}
	
	public Page<UserEventSet> findByReferenceId(String referenceId, Pageable pageable){
		return userEventSetRepository.findByReferenceId(referenceId, pageable);
	}
	
	public Page<UserEventSet> findByMid(String mid, Pageable pageable){
		return userEventSetRepository.findByMid(mid, pageable);
	}
	
	public Page<UserEventSet> findByMidAndTargetAndAction(String mid, String target, String action, Pageable pageable){
		return userEventSetRepository.findByMidAndTargetAndAction(mid, target, action, pageable);
	}
	
	public Page<UserEventSet> findByMidAndReferenceId(String mid, String referenceId, Pageable pageable){
		return userEventSetRepository.findByMidAndReferenceId(mid, referenceId, pageable);
	}
	
	public Page<UserEventSet> findByMidAndTargetAndActionAndReferenceId(String mid, String target, String action, String referenceId, Pageable pageable){
		return userEventSetRepository.findByMidAndTargetAndActionAndReferenceId(mid, target, action, referenceId, pageable);
	}
	
	public Long deleteByTargetAndAction(String target, String action){
		return userEventSetRepository.deleteByTargetAndAction(target, action);
	}
	
	public Long deleteByTargetAndActionAndReferenceId(String target, String action, String referenceId){
		return userEventSetRepository.deleteByTargetAndActionAndReferenceId(target, action, referenceId);
	}
	
	public Long countByTargetAndAction(String target, String action){
		return userEventSetRepository.countByTargetAndAction(target, action);
	}
	
	public Long countByTargetAndActionAndReferenceId(String target, String action, String referenceId){
		return userEventSetRepository.countByTargetAndActionAndReferenceId(target, action, referenceId);
	}
}
