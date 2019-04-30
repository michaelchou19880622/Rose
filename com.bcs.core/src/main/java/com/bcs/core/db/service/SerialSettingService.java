package com.bcs.core.db.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.SerialSetting;
import com.bcs.core.db.entity.UserEventSet;
import com.bcs.core.db.repository.SerialSettingRepository;
import com.bcs.core.enums.EVENT_TARGET_ACTION_TYPE;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class SerialSettingService {
	public static final String SERIAL_SYNC = "SERIAL_SYNC";
	
	/** Logger */
	private static Logger logger = Logger.getLogger(SerialSettingService.class);

	@Autowired
	private SerialSettingRepository serialSettingRepository;
	@Autowired
	private UserEventSetService userEventSetService;

	protected LoadingCache<String, SerialSetting> dataCache;

	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(SERIAL_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(SERIAL_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	public SerialSettingService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, SerialSetting>() {
					@Override
					public SerialSetting load(String key) throws Exception {
						return new SerialSetting();
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] SerialSettingService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] SerialSettingService destroyed.");
	}
	
	private boolean notNull(SerialSetting result){
		if(result != null && StringUtils.isNotBlank(result.getSerialId()) && !"-".equals(result.getSerialId())){
			return true;
		}
		return false;
	}
	
	public List<SerialSetting> findByLevel(String serialLevel){
		return serialSettingRepository.findByLevel(serialLevel);
	}

	public SerialSetting findOne(String serialId) {
		try {
			SerialSetting result = dataCache.get(serialId);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		SerialSetting result = serialSettingRepository.findOne(serialId);
		if(result != null){
			dataCache.put(serialId, result);
		}
		return result;
	}
	
	public void save(SerialSetting serialSetting) {
		serialSettingRepository.save(serialSetting);
		
		if(serialSetting != null){
			dataCache.put(serialSetting.getSerialId(), serialSetting);
			DataSyncUtil.settingReSync(SERIAL_SYNC);
		}
	}
	
	public Map<String, String> getSerialSettingReplaceParam(String SerialId, String mid){

		String target = EVENT_TARGET_ACTION_TYPE.EVENT_SERIAL_SETTING.toString();
		String action = EVENT_TARGET_ACTION_TYPE.ACTION_UPLOAD_MID_SERIAL.toString();
		
		if(StringUtils.isNotBlank(SerialId)){
			SerialSetting serialSetting = findOne(SerialId);
			if(serialSetting != null){
				String replaceTarget = serialSetting.getSerialTarget();
	
				Pageable pageable = new PageRequest(0, 1);
				Page<UserEventSet> userEventSetPage = userEventSetService.findByMidAndTargetAndActionAndReferenceId(mid, target, action, SerialId, pageable);
	
				if(userEventSetPage != null){
					List<UserEventSet> list = userEventSetPage.getContent();
					if(list != null && list.size() > 0){
						Map<String, String> result = new HashMap<String, String>();
						
						result.put(replaceTarget, list.get(0).getContent());
						
						return result;
					}
				}
			}
		}
		
		return null;
	}
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public void delete(String serialId) throws BcsNoticeException{
		logger.debug("delete:" + serialId);
		if(StringUtils.isNotBlank(serialId)){
		
			SerialSetting serialSetting = findOne(serialId);
			
			if(serialSetting != null){
				serialSettingRepository.delete(serialSetting);
				dataCache.put(serialId, new SerialSetting());
				DataSyncUtil.settingReSync(SERIAL_SYNC);
			}
		}
	}
}
