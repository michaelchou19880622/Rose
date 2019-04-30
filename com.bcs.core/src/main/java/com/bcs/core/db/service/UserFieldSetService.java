package com.bcs.core.db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.UserFieldSet;
import com.bcs.core.db.repository.UserFieldSetRepository;

@Service
public class UserFieldSetService {
	
	@Autowired
	private UserFieldSetRepository userFieldSetRepository;
	
	public void save(UserFieldSet userFieldSet){
		userFieldSetRepository.save(userFieldSet);
	}
	
	public void save(List<UserFieldSet> userFieldSets){
		userFieldSetRepository.save(userFieldSets);
	}

	public List<UserFieldSet> findByMid(String mid){
		return userFieldSetRepository.findByMid(mid);
	}
	
	public List<UserFieldSet> findByMidAndKeyData(String mid, String keyData){
		return userFieldSetRepository.findByMidAndKeyData(mid, keyData);
	}

	public Long deleteByMid(String mid){
		return userFieldSetRepository.deleteByMid(mid);
	}
	
	public Long deleteByMidAndKeyData(String mid, String keyData){
		return userFieldSetRepository.deleteByMidAndKeyData(mid, keyData);
	}
	
	public List<Object[]> getFieldKeyAndNameAndType(){
		return userFieldSetRepository.getFieldKeyAndNameAndType();
	}
}
