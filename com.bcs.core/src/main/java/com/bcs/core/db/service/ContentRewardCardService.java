package com.bcs.core.db.service;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentRewardCard;
import com.bcs.core.db.repository.ContentCouponRepository;
import com.bcs.core.db.repository.ContentRewardCardRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ContentRewardCardService {
	public static final String REWARDCARD_SYNC = "REWARDCARD_SYNC";
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentRewardCardService.class);

	@Autowired
	private ContentCouponRepository contentCouponRepository;
	@Autowired
	private ContentRewardCardRepository contentRewardCardRepository;

	protected LoadingCache<String, ContentRewardCard> dataCache;
	
	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask {

		@Override
		public void run() {

			try {
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(REWARDCARD_SYNC);
				if (isReSyncData) {
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(REWARDCARD_SYNC);
				}
			} catch (Throwable e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	public ContentRewardCardService(){
		
		flushTimer.schedule(new CustomTask(), 120000, 30000);
		
		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, ContentRewardCard>() {
					@Override
					public ContentRewardCard load(String key) throws Exception {
						return new ContentRewardCard();
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentRewardCardService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Throwable e){
		}
		
		System.gc();
		logger.info("[DESTROY] ContentRewardCardService destroyed.");
	}
	
	private boolean notNull(ContentRewardCard result){
		if(result != null && result.getRewardCardId() != null){
			return true;
		}
		return false;
	}

	public List<ContentCoupon> findAll() {
		return contentCouponRepository.findAll(new Sort(Sort.Direction.DESC, "modifyTime"));
	}
	
	public List<ContentRewardCard> findByStatus(String status){
		return contentRewardCardRepository.findByStatus(status);
	}
	
	public List<ContentCoupon> findByEventReferenceAndStatus(String eventReference, String status){
		return contentCouponRepository.findByEventReferenceAndStatus(eventReference, status);
	}

	public ContentRewardCard findOne(String rewardCardId) {
		try {
			ContentRewardCard result = dataCache.get(rewardCardId);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		ContentRewardCard result = contentRewardCardRepository.findOne(rewardCardId);
		if(result != null){
			dataCache.put(rewardCardId, result);
		}
		return result;
	}
	
	public String findRewardCardTitleByRewardCardId(String rewardCardId) {
		return contentRewardCardRepository.findRewardCardTitleByRewardCardId(rewardCardId);
	}
	
	public void save(ContentRewardCard contentRewardCard) {
		contentRewardCardRepository.save(contentRewardCard);
		
		if(contentRewardCard != null){
			dataCache.put(contentRewardCard.getRewardCardId(), contentRewardCard);
			DataSyncUtil.settingReSync(REWARDCARD_SYNC);
		}
	}
	
	@Transactional(rollbackFor=Exception.class)
	public void increaseRewardCardGetNumberByRewardCardId(String rewardCardId){
		contentRewardCardRepository.increaseRewardCardGetNumberByRewardCardId(rewardCardId);
		
		ContentRewardCard result = contentRewardCardRepository.findOne(rewardCardId);
		if(result != null){
			dataCache.put(rewardCardId, result);
		}
	}
	
	public String checkContentRewardCard(ContentRewardCard contentRewardCard)  {
		 	Date now = new Date();
	        
		 	//驗證點數卡是否為Active
	        if(ContentRewardCard.REWARD_CARD_STATUS_DELETE.equals(contentRewardCard.getStatus()) || 
	                ContentRewardCard.REWARD_CARD_STATUS_DISABLE.equals(contentRewardCard.getStatus())){
//	            throw new BcsNoticeException("集點卡錯誤:找不到集點卡");
	            return "noCard";
	        }
	        
	        // 驗證是否符合領用期間
	        Date rewardCardStartGetTime = contentRewardCard.getRewardCardStartGetTime();
	        Date rewardCardEndGetTime = contentRewardCard.getRewardCardEndGetTime();
	        if (now.compareTo(rewardCardStartGetTime) < 0 || now.compareTo(rewardCardEndGetTime) > 0) {
//	            throw new BcsNoticeException("集點卡錯誤:不在領取期間");
	            return "notInTime";
	        }
	        
	        // 驗證是否符合使用期間
	        Date rewardCardStartUseTime = contentRewardCard.getRewardCardStartUsingTime();
	        Date rewardCardEndUseTime = contentRewardCard.getRewardCardEndUsingTime();
	        if (now.compareTo(rewardCardStartUseTime) < 0 || now.compareTo(rewardCardEndUseTime) > 0) {
//	            throw new BcsNoticeException("集點卡錯誤:不在使用期間");
	            return "notInTime";
	        }
	        
	        return null;
	    }
}
