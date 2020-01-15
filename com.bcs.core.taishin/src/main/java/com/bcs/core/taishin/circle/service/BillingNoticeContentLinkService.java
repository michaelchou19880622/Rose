package com.bcs.core.taishin.circle.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentLink;
import com.bcs.core.taishin.circle.db.repository.BillingNoticeContentLinkRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class BillingNoticeContentLinkService {

	/** Logger */
	private static Logger logger = Logger.getLogger(BillingNoticeContentLinkService.class);

	@Autowired
	private BillingNoticeContentLinkRepository contentLinkRepository;

	protected LoadingCache<String, BillingNoticeContentLink> dataCache; // No Need Sync

	public BillingNoticeContentLinkService(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, BillingNoticeContentLink>() {
					@Override
					public BillingNoticeContentLink load(String key) throws Exception {
						return new BillingNoticeContentLink("-");
					}
				});
	}

	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentLinkService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Exception e){}

		System.gc();
		logger.info("[DESTROY] ContentLinkService destroyed.");
	}

	private boolean notNull(BillingNoticeContentLink result){
		if(result != null && StringUtils.isNotBlank(result.getLinkId()) && !"-".equals(result.getLinkId())){
			return true;
		}
		return false;
	}

    /**
  	 * 取得所有連結清單
       */
  	public List<Object[]> getAllContentLinkUrl(){
		return contentLinkRepository.findAllLinkUrl();
    }

  	public List<Object[]> findAllLinkUrlByFlag(String flag){
  		return contentLinkRepository.findAllLinkUrlByFlag(flag);
  	}

  	public List<Object[]> findAllLinkUrlByLikeFlag(String flag){
  		return contentLinkRepository.findAllLinkUrlByLikeFlag(flag);
  	}

  	public List<Object[]> findAllLinkUrlByLikeTitle(String title){
  		return contentLinkRepository.findAllLinkUrlByLikeTitle(title);
  	}

  	public List<BillingNoticeContentLink> findAll(){
		return contentLinkRepository.findAll();
    }

  	public Page<BillingNoticeContentLink> findAll(Pageable pageable){
		return contentLinkRepository.findAll(pageable);
    }

  	public List<BillingNoticeContentLink> findByLinkUrl(String linkUrl){
		return contentLinkRepository.findByLinkUrl(linkUrl);
  	}

  	public List<BillingNoticeContentLink> findByLinkIdIn(List<String> linkIds){
  		return contentLinkRepository.findByLinkIdIn(linkIds);
  	}

	public void save(BillingNoticeContentLink contentLink){
		contentLinkRepository.save(contentLink);

		if(contentLink != null){
			dataCache.put(contentLink.getLinkId(), contentLink);
		}
	}

	public void save(List<BillingNoticeContentLink> contentLinks){
		for(BillingNoticeContentLink contentLink : contentLinks){
			this.save(contentLink);
		}
	}

	public BillingNoticeContentLink findOne(String linkId){
		try {
			BillingNoticeContentLink result = dataCache.get(linkId);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}

		BillingNoticeContentLink result = contentLinkRepository.findOne(linkId);
		if(result != null){
			dataCache.put(linkId, result);
		}
		return result;
	}

	public List<Object[]> countClickCountByLinkUrlAndTime(String linkUrl, String start, String end){
		return contentLinkRepository.countClickCountByLinkUrlAndTime(linkUrl, start, end);
	}

	public List<Object[]> countClickCountByLinkUrl(String linkUrl){
		return contentLinkRepository.countClickCountByLinkUrl(linkUrl);
	}

	public List<Object[]> countClickCountByLinkUrl(String linkUrl, String start){
		return contentLinkRepository.countClickCountByLinkUrl(linkUrl, start);
	}

	public List<Object[]> countClickCountByLinkIdAndTime(String linkUrl, String start, String end){
		return contentLinkRepository.countClickCountByLinkIdAndTime(linkUrl, start, end);
	}

	public List<Object[]> countClickCountByLinkId(String LinkId){
		return contentLinkRepository.countClickCountByLinkId(LinkId);
	}

	public List<Object[]> countClickCountByLinkId(String LinkId, String start){
		return contentLinkRepository.countClickCountByLinkId(LinkId, start);
	}

	public List<String> findClickMidByLinkUrlAndTime(String linkUrl, String start, String end){
		return contentLinkRepository.findClickMidByLinkUrlAndTime(linkUrl, start, end);
	}
}
