package com.bcs.core.db.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.repository.ContentCouponRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

@Service
public class ContentCouponService {
	public static final String COUPON_SYNC = "COUPON_SYNC";

	/** Logger */
	private static Logger logger = Logger.getLogger(ContentCouponService.class);

	@Autowired
	private ContentCouponRepository contentCouponRepository;

	protected LoadingCache<String, ContentCoupon> dataCache;

	private Timer flushTimer = new Timer();

	private class CustomTask extends TimerTask {

		@Override
		public void run() {

			try {
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(COUPON_SYNC);
				if (isReSyncData) {
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(COUPON_SYNC);
				}
			} catch (Throwable e) {
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	public ContentCouponService() {

		flushTimer.schedule(new CustomTask(), 120000, 30000);

		dataCache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<Object, ContentCoupon>() {
					@Override
					public ContentCoupon load(Object key) throws Exception {
						return new ContentCoupon();
					}
				});
	}

	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentCouponService cleaning up...");
		try {
			if (dataCache != null) {
				dataCache.invalidateAll();
				dataCache = null;
			}
		} catch (Throwable e) {
		}

		System.gc();
		logger.info("[DESTROY] ContentCouponService destroyed.");
	}

	private boolean notNull(ContentCoupon result) {
		if (result != null && result.getCouponId() != null) {
			return true;
		}
		return false;
	}

	public void setCouponEvent(Object coupon, String referenceId, String eventType) {
		if (eventType.equals("ScratchCard")) {
			Gson gson = new Gson();
			JSONObject couponObj = new JSONObject(gson.toJson(coupon));
			String couponId = couponObj.get("id").toString();
			BigDecimal probability = new BigDecimal(couponObj.get("probability").toString());
			String identityLetter = couponObj.get("identityLetter").toString();

			ContentCoupon targetCoupon = contentCouponRepository.findOne(couponId);

			targetCoupon.setEventReference(ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD);
			targetCoupon.setEventReferenceId(referenceId);
			targetCoupon.setProbability(probability);
			targetCoupon.setIdentityLetter(identityLetter);

			this.save(targetCoupon);
		}
	}

	public void resetCouponEvent(List<ContentCoupon> couponList) {
		for (ContentCoupon coupon : couponList) {
			ContentCoupon targetCoupon = null;

			targetCoupon = contentCouponRepository.findOne(coupon.getCouponId());

			targetCoupon.setEventReference(null);
			targetCoupon.setEventReferenceId(null);
			targetCoupon.setProbability(null);
			targetCoupon.setIdentityLetter(null);

			this.save(targetCoupon);
		}
	}

	public List<ContentCoupon> findAll() {
		return contentCouponRepository.findAll(new Sort(Sort.Direction.DESC, "modifyTime"));
	}

	public List<ContentCoupon> findByStatus(String status) {
		return contentCouponRepository.findByStatus(status);
	}

	public ContentCoupon findByCouponGroupId(String couponGroupId) {
		return contentCouponRepository.findByCouponGroupId(couponGroupId);
	}

	public List<ContentCoupon> findByEventReferenceAndStatus(String eventReference, String status) {
		return contentCouponRepository.findByEventReferenceAndStatus(eventReference, status);
	}

	public List<ContentCoupon> findByEventReferenceAndEventReferenceId(String eventReference, String eventReferenceId) {
		return contentCouponRepository.findByEventReferenceAndEventReferenceId(eventReference, eventReferenceId);
	}

	public List<ContentCoupon> findUnusedContentCouponList() {
		return contentCouponRepository.findUnusedContentCouponList();
	}

	public List<ContentCoupon> findUnusedContentCouponList(String gameId) {
		List<ContentCoupon> contentCoupoList = new ArrayList<ContentCoupon>();

		contentCoupoList.addAll(contentCouponRepository.findByEventReferenceAndEventReferenceId(ContentCoupon.EVENT_REFERENCE_SCRATCH_CARD, gameId));
		contentCoupoList.addAll(this.findUnusedContentCouponList());

		return contentCoupoList;
	}

	public ContentCoupon findOne(String couponId) {
		try {
			ContentCoupon result = dataCache.get(couponId);
			if (notNull(result)) {
				return result;
			}
		} catch (Exception e) {
		}

		ContentCoupon result = contentCouponRepository.findOne(couponId);
		if (result != null) {
			dataCache.put(couponId, result);
		}
		return result;
	}

	public String findCouponTitleByCouponId(String couponId) {
		return contentCouponRepository.findCouponTitleByCouponId(couponId);
	}

	public void save(ContentCoupon contentCoupon) {
		String couponId = null;
		
		if(contentCoupon.getCouponId() == null){
			couponId = this.checkDuplicateUUID();
			contentCoupon.setCouponId(couponId);
		}
		contentCouponRepository.save(contentCoupon);

		if (contentCoupon != null) {
			dataCache.put(contentCoupon.getCouponId(), contentCoupon);
			DataSyncUtil.settingReSync(COUPON_SYNC);
		}
	}

	@Transactional(rollbackFor = Exception.class, timeout = 30)
	public void increaseCouponUsingNumberByCouponId(String couponId) {
		contentCouponRepository.increaseCouponUsingNumberByCouponId(couponId);

		ContentCoupon result = contentCouponRepository.findOne(couponId);
		if (result != null) {
			dataCache.put(couponId, result);
		}
	}

	@Transactional(rollbackFor = Exception.class, timeout = 30)
	public void increaseCouponGetNumberByCouponId(String couponId) {
		contentCouponRepository.increaseCouponGetNumberByCouponId(couponId);

		ContentCoupon result = contentCouponRepository.findOne(couponId);
		if (result != null) {
			dataCache.put(couponId, result);
		}
	}	
	
	public List<ContentCoupon> findAllByCouponGroupId (String couponGroupId){
		return contentCouponRepository.findAllByCouponGroupId(couponGroupId);
	}
	
	public Boolean checkIsNotGetSamePointCoupon (String couponGroupId){
		ContentCoupon contentCoupon = contentCouponRepository.findByCouponGroupId(couponGroupId);
		return (contentCoupon == null)?false:true;
	}
	
	/** 
	 * 回傳一個沒有重覆的uuid
	 */
	public String checkDuplicateUUID() {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		Boolean exsistUUID = this.checkExsistUUID(uuid);
		while (exsistUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			exsistUUID = this.checkExsistUUID(uuid);
		}
		return uuid;
	}
	
	private Boolean checkExsistUUID (String couponId){
		ContentCoupon contentCoupon = contentCouponRepository.findOne(couponId);
		return (contentCoupon == null)?false:true;
	}
}
