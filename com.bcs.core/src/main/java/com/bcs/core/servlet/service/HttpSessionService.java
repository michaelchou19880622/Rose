package com.bcs.core.servlet.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class HttpSessionService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(HttpSessionService.class);

	protected LoadingCache<String, HttpSession> dataCache; // No Need Sync
	
	public HttpSessionService(){
		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, HttpSession>() {
					@Override
					public HttpSession load(String key) throws Exception {
						return null;
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] HttpSessionService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] HttpSessionService destroyed.");
	}
	
	public void setSession(String sessionId, HttpSession httpSession){
		dataCache.put(sessionId, httpSession);
	}
	
	public HttpSession getSession(String sessionId){
		try {
			return dataCache.get(sessionId);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return null;
		}
	}
}
