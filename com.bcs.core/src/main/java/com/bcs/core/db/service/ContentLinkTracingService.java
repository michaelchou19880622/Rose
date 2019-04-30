package com.bcs.core.db.service;

import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.ContentLinkTracing;
import com.bcs.core.db.repository.ContentLinkTracingRepository;

@Service
public class ContentLinkTracingService {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentLinkTracingService.class);
	
	@Autowired
	private ContentLinkTracingRepository contentLinkTracingRepository;

//	protected LoadingCache<Long, ContentLinkTracing> dataCache;

	public ContentLinkTracingService(){

//		dataCache = CacheBuilder.newBuilder()
//				.concurrencyLevel(1)
//				.expireAfterAccess(30, TimeUnit.MINUTES)
//				.build(new CacheLoader<Long, ContentLinkTracing>() {
//					@Override
//					public ContentLinkTracing load(Long key) throws Exception {
//						return new ContentLinkTracing("-");
//					}
//				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentLinkTracingService cleaning up...");
		try{
//			if(dataCache != null){
//				dataCache.invalidateAll();
//				dataCache = null;
//			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] ContentLinkTracingService destroyed.");
	}
	
//	private boolean notNull(ContentLinkTracing result){
//		if(result != null && StringUtils.isNotBlank(result.getLinkId()) && !"-".equals(result.getLinkId())){
//			return true;
//		}
//		return false;
//	}
	
	public void save(ContentLinkTracing contentLinkTracing){
		contentLinkTracingRepository.save(contentLinkTracing);

		if(contentLinkTracing != null){
//			dataCache.put(contentLinkTracing.getTracingId(), contentLinkTracing);
		}
	}
	
	public ContentLinkTracing findOne(Long tracingId){
//		try {
//			ContentLinkTracing result = dataCache.get(tracingId);
//			if(notNull(result)){
//				return result;
//			}
//		} catch (Exception e) {}
		
		ContentLinkTracing result = contentLinkTracingRepository.findOne(tracingId);
		if(result != null){
//			dataCache.put(tracingId, result);
		}
		return result;
	}
	
	public List<ContentLinkTracing> findAll(){
		return contentLinkTracingRepository.findAll(new Sort(Sort.Direction.DESC, "modifyTime"));
	}
	
	public List<ContentLinkTracing> findAll(Sort sort){
		return contentLinkTracingRepository.findAll(sort);
	}
}
