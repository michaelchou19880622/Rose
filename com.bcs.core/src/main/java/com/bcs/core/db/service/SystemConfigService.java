package com.bcs.core.db.service;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.SystemConfig;
import com.bcs.core.db.repository.SystemConfigRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class SystemConfigService {
	public static final String CONFIG_SYNC = "CONFIG_SYNC";
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentCouponService.class);
	
	@Autowired
	private SystemConfigRepository systemConfigRepository;

	protected LoadingCache<String, String> data;

	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(CONFIG_SYNC);
				if(isReSyncData){
					data.invalidateAll();
					DataSyncUtil.syncDataFinish(CONFIG_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	public SystemConfigService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);

		data = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, String>() {
					@Override
					public String load(String str) throws Exception {
						return "";
					}
				});
	}
	
	public String findOne(String configId, boolean fromCatch){
		if(fromCatch){
			try {
				if(StringUtils.isNotBlank(data.get(configId))){
					return data.get(configId);
				}
			} catch (Exception e) {}
		}
		
		try{
			SystemConfig systemConfig = systemConfigRepository.findOne(configId);
	
			if(systemConfig != null && StringUtils.isNotBlank(systemConfig.getValue())){
				data.put(configId, systemConfig.getValue());
				return systemConfig.getValue();
			}
		}
		catch(Throwable e){}// Skip
		
		return null;
	}
	
	public void clearData(){
		data.invalidateAll();
		DataSyncUtil.settingReSync(CONFIG_SYNC);
	}
	
	public Map<String, String> getSettingData(){
		return data.asMap();
	}
	
	public List<SystemConfig> findAll(){
		return systemConfigRepository.findAll();
	}
	
	public void delete(String configId){
		systemConfigRepository.delete(configId);
		
		data.refresh(configId);
		DataSyncUtil.settingReSync(CONFIG_SYNC);
	}
	
	public SystemConfig save(SystemConfig systemConfig){
		SystemConfig result = systemConfigRepository.save(systemConfig);
		
		data.put(systemConfig.getConfigId(), systemConfig.getValue());
		
		return result;
	}
	
	public SystemConfig findSystemConfig(String configId){
		
		try{
			SystemConfig systemConfig = systemConfigRepository.findOne(configId);
	
			if(systemConfig != null && StringUtils.isNotBlank(systemConfig.getValue())){
				data.put(configId, systemConfig.getValue());
				return systemConfig;
			}
		}
		catch(Throwable e){}// Skip
		
		return null;
	}

	public List<Object[]> findLikeConfigId(String configId){
		return systemConfigRepository.findLikeConfigId(configId);
	}
}
