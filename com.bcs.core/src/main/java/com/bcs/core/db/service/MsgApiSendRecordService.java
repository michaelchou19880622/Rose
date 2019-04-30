package com.bcs.core.db.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.MsgApiSendRecord;
import com.bcs.core.db.repository.MsgApiSendRecordRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class MsgApiSendRecordService {
	@Autowired
	private MsgApiSendRecordRepository msgApiSendRecordRepository;

	protected LoadingCache<String, String> dataCache;

	public MsgApiSendRecordService(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, String>() {
					@Override
					public String load(String key) throws Exception {
						return "-";
					}
				});
	}

	public void save(MsgApiSendRecord msgApiSendRecord){
		dataCache.put(msgApiSendRecord.getSendApiId(), msgApiSendRecord.getSendApiId());
		msgApiSendRecordRepository.save(msgApiSendRecord);
	}
	
	public List<MsgApiSendRecord> findByMsgSource(String msgSource){
		return msgApiSendRecordRepository.findByMsgSource(msgSource);
	}
	
	public MsgApiSendRecord findOne(String sendApiId){
		return msgApiSendRecordRepository.findOne(sendApiId);
	}
	
	public String findSendApiId(String sendApiId){
		try {
			String result = dataCache.get(sendApiId);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		return null;
	}
	
	private boolean notNull(String result){
		if(StringUtils.isNotBlank(result) && !"-".equals(result)){
			return true;
		}
		return false;
	}
	
	public void bulkPersist(MsgApiSendRecord msgApiSendRecord){
		dataCache.put(msgApiSendRecord.getSendApiId(), msgApiSendRecord.getSendApiId());
		msgApiSendRecordRepository.bulkPersist(msgApiSendRecord);
	}
}
