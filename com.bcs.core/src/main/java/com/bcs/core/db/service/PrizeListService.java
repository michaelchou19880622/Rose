package com.bcs.core.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.PrizeList;
import com.bcs.core.db.repository.PrizeListRepository;

@Service
public class PrizeListService {
	@Autowired
	private PrizeListRepository prizeListRepository;
	
	public void insertUIDIntoPrizeList(Integer prizeListId, String UID) {
		PrizeList prizeList = prizeListRepository.findOne(prizeListId);
		
		prizeList.setMid(UID);
		
		prizeListRepository.save(prizeList);
	}
}