package com.bcs.core.db.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentSticker;
import com.bcs.core.db.repository.ContentStickerRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ContentStickerService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentStickerService.class);
	
	@Autowired
	private ContentStickerRepository contentStickerRepository;

	protected LoadingCache<String, ContentSticker> dataCache; // No Need Sync

	public ContentStickerService(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, ContentSticker>() {
					@Override
					public ContentSticker load(String key) throws Exception {
						return new ContentSticker("-");
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentStickerService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] ContentStickerService destroyed.");
	}

	public void save(ContentSticker contentSticker){
		contentStickerRepository.save(contentSticker);

		if(contentSticker != null){
			dataCache.put(contentSticker.getStickerStkid(), contentSticker);
		}
	}
	
	private boolean notNull(ContentSticker result){
		if(result != null && StringUtils.isNotBlank(result.getStickerStkid()) && !"-".equals(result.getStickerStkid())){
			return true;
		}
		return false;
	}
	
	public ContentSticker findOne(String stickerStkid){
		try {
			ContentSticker result = dataCache.get(stickerStkid);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		ContentSticker result = contentStickerRepository.findOne(stickerStkid);
		if(result != null){
			dataCache.put(stickerStkid, result);
		}
		return result;
	}
}
