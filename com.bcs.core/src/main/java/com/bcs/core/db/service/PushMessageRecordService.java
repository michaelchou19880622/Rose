package com.bcs.core.db.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.PushMessageRecord;
import com.bcs.core.db.repository.PushMessageRecordRepository;

@Service
public class PushMessageRecordService {		
	@PersistenceContext
	EntityManager entityManager;
	
	@Autowired
	private PushMessageRecordRepository pushMessageRecordRepository;
	
	public PushMessageRecord save(PushMessageRecord pushMessageRecord) {
		return pushMessageRecordRepository.save(pushMessageRecord);
	}
	
	public List<PushMessageRecord> getPushMessageRecordByCreateTime(String createTime) {
		return pushMessageRecordRepository.findByCreateTime(createTime);
	}
	
	public List<Map<String, String>> getPushMessageEffects(String startDateString, String endDateString) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		Date startDate = null, endDate = null;
		
		try {
			startDate = sdf.parse(startDateString);
			endDate = sdf.parse(endDateString);
			
			endDate = DateUtils.addDays(endDate, 1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		Query query = entityManager.createNamedQuery("getPushMessageEffects").setParameter(1, startDate).setParameter(2, endDate);
		
		@SuppressWarnings("unchecked")
		List<Object[]> resultList = query.getResultList();
		
		for (Object[] o : resultList) {
			Map<String, String> map = new HashMap<String, String>();
			
			map.put("createTime", (o[0] == null) ? null : o[0].toString());
			map.put("department", (o[1] == null) ? null : o[1].toString());
			map.put("successCount", (o[2] == null) ? null : o[2].toString());
			map.put("failCount", (o[3] == null) ? null : o[3].toString());
			
			result.add(map);
		}
		
		return result;
	}
}
