package com.bcs.core.db.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.repository.ContentLinkRepository;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ContentLinkService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentLinkService.class);
	
	@Autowired
	private ContentLinkRepository contentLinkRepository;

	protected LoadingCache<String, ContentLink> dataCache; // No Need Sync

	public ContentLinkService(){

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, ContentLink>() {
					@Override
					public ContentLink load(String key) throws Exception {
						return new ContentLink("-");
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
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] ContentLinkService destroyed.");
	}
	
	private boolean notNull(ContentLink result){
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

  	public List<ContentLink> findAll(){
		return contentLinkRepository.findAll();
    }

  	public Page<ContentLink> findAll(Pageable pageable){
		return contentLinkRepository.findAll(pageable);
    }

  	public List<ContentLink> findByLinkUrl(String linkUrl){
		return contentLinkRepository.findByLinkUrl(linkUrl);
  	}
  	
  	public List<ContentLink> findByLinkIdIn(List<String> linkIds){
  		return contentLinkRepository.findByLinkIdIn(linkIds);
  	}
	
	public void save(ContentLink contentLink){
		contentLinkRepository.save(contentLink);

		if(contentLink != null){
			dataCache.put(contentLink.getLinkId(), contentLink);
		}
	}
	
	public void save(List<ContentLink> contentLinks){
		for(ContentLink contentLink : contentLinks){
			this.save(contentLink);
		}
	}
	
	public ContentLink findOne(String linkId){
		try {
			ContentLink result = dataCache.get(linkId);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		ContentLink result = contentLinkRepository.findOne(linkId);
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
