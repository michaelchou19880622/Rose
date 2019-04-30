package com.bcs.core.db.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentCoupon;
import com.bcs.core.db.entity.ContentCouponCode;
import com.bcs.core.db.repository.ContentCouponCodeRepository;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ContentCouponCodeService {
	public static final String COUPON_SYNC = "COUPON_SYNC";
	public static final Integer pageSize = 1000;
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentCouponCodeService.class);

	@Autowired
	private ContentCouponCodeRepository contentCouponCodeRepository;

	public ContentCouponCodeService(){
	}
	
	public ContentCouponCode findOneByCouponIdAndStatus(String couponId,String status) {
		return contentCouponCodeRepository.findOneByCouponIdAndStatus(couponId,status);
	}
	
	public ContentCouponCode findOne(Long couponCodeId){
		return contentCouponCodeRepository.findOne(couponCodeId);
	}
	
	public ContentCouponCode findOneAndLock(Long couponCodeId){
		return contentCouponCodeRepository.findOneAndLock(couponCodeId);
	}

	public void save(ContentCouponCode contentCouponCode) {
		contentCouponCodeRepository.save(contentCouponCode);
	}
	
	public Integer findCouponCodeListNumber(String couponId){
		return contentCouponCodeRepository.findCouponCodeListNumber(couponId);
	}
	
	public List<ContentCouponCode> findByCouponId(String couponId, Pageable pageable){
		Page<ContentCouponCode> pageList = contentCouponCodeRepository.findByCouponId(couponId,pageable);
		return pageList.getContent();
	}
	
	public ContentCouponCode findByCouponCodeIdAndStatus(Long couponCodeId, String status){
		return contentCouponCodeRepository.findByCouponCodeIdAndStatus(couponCodeId, status);
	}
	
	
	@Transactional(rollbackFor = Exception.class)
	public ContentCouponCode findNotUsedCouponCodeAndLock(String couponId,String status,int retryNum) throws Exception{
		int maxRetryNum = 5;
		try {
			ContentCouponCode notUsedContentCouponCode = this.findOneByCouponIdAndStatus(couponId,ContentCouponCode.COUPON_CODE_IS_NOT_USE);
			if(notUsedContentCouponCode == null){
				throw new BcsNoticeException("電子序號已全數領完");
			}
			Long couponCodeId = notUsedContentCouponCode.getCouponCodeId();
			ContentCouponCode lockedContentCouponCode = this.findOneAndLock(couponCodeId);
			ContentCouponCode checkedlockedContentCouponCode = this.findByCouponCodeIdAndStatus(couponCodeId, ContentCouponCode.COUPON_CODE_IS_NOT_USE);
			if (checkedlockedContentCouponCode == null) {
				throw new BcsNoticeException("電子序號已被使用");
			}else
				return lockedContentCouponCode;
		} catch (Exception e) {
			String error = ErrorRecord.recordError(e, false);
			logger.error(error);
			if(e instanceof BcsNoticeException && retryNum<maxRetryNum){
				return this.findNotUsedCouponCodeAndLock(couponId,status,retryNum+1);
			}else
				throw e;
		}
	}
	
    public Integer getCouponCodeMaxPageByCouponId(String couponId){
    	Integer couponCodeNum =  this.findCouponCodeListNumber(couponId);
    	Integer page = couponCodeNum / pageSize;
		if(couponCodeNum % 1000 != 0){
			page++;
		}
		return page;
    } 
}
