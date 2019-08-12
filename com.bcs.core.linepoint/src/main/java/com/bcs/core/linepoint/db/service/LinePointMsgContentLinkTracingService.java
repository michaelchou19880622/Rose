package com.bcs.core.linepoint.db.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

//import com.bcs.core.linepoint.db.entity.RichMenuContentLink;
import com.bcs.core.linepoint.db.entity.LinePointMsgContentLinkTracing;
import com.bcs.core.linepoint.db.repository.LinePointMsgContentLinkTracingRepository;
import com.bcs.core.resource.UriHelper;


@Service
public class LinePointMsgContentLinkTracingService {

	/** Logger */
	private static Logger logger = Logger.getLogger(LinePointMsgContentLinkTracingService.class);

	@Autowired
	private LinePointMsgContentLinkTracingRepository msgContentLinkTracingRepository;

	protected LoadingCache<Long, LinePointMsgContentLinkTracing> dataCache;

	public LinePointMsgContentLinkTracingService() {

		dataCache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<Long, LinePointMsgContentLinkTracing>() {
					@Override
					public LinePointMsgContentLinkTracing load(Long key) throws Exception {
						return new LinePointMsgContentLinkTracing("-");
					}
				});
	}

	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentLinkTracingService cleaning up...");
		try {
			if (dataCache != null) {
				dataCache.invalidateAll();
				dataCache = null;
			}
		} catch (Throwable e) {
		}

		System.gc();
		logger.info("[DESTROY] ContentLinkTracingService destroyed.");
	}

	private boolean notNull(LinePointMsgContentLinkTracing result) {
		if (result != null && StringUtils.isNotBlank(result.getLinkId()) && !"-".equals(result.getLinkId())) {
			return true;
		}
		return false;
	}

	public void save(LinePointMsgContentLinkTracing msgContentLinkTracing) {
		msgContentLinkTracingRepository.save(msgContentLinkTracing);

		if (msgContentLinkTracing != null) {
			dataCache.put(msgContentLinkTracing.getTracingId(), msgContentLinkTracing);
		}
	}

	public LinePointMsgContentLinkTracing findOne(Long tracingId) {
		try {
			LinePointMsgContentLinkTracing result = dataCache.get(tracingId);
			if (notNull(result)) {
				return result;
			}
		} catch (Exception e) {}

		LinePointMsgContentLinkTracing result = msgContentLinkTracingRepository.findOne(tracingId);
		if (result != null) {
			dataCache.put(tracingId, result);
		}
		return result;
	}

	public List<LinePointMsgContentLinkTracing> findAll() {
		return msgContentLinkTracingRepository.findAll(new Sort(Sort.Direction.DESC, "modifyTime"));
	}

	public List<LinePointMsgContentLinkTracing> findAll(Sort sort) {
		return msgContentLinkTracingRepository.findAll(sort);
	}
	

//	public String generateMsgTracingLink(RichMenuContentLink contentLink) throws Exception{
//		
//		if(contentLink != null){
//			String beCheckedLink = contentLink.getLinkUrl();
//			String tracingUrlPre = UriHelper.getTracingUrlPre();
//			// 檢查是否為追蹤連結
//			if(beCheckedLink.startsWith(tracingUrlPre)){
//				return beCheckedLink;
//			}
//			
//			Long tracingId = generateMsgTracingLink(contentLink.getLinkId());
//			return UriHelper.getMsgTracingUrl(tracingId);
//		}
//		
//		throw new Exception("ContentLink is null");
//		
//	}

	public Long generateMsgTracingLink(String contentLinkId) throws Exception {
		logger.debug("generateMsgTracingLink:" + contentLinkId);

		LinePointMsgContentLinkTracing msgTracingLink = null;
		
		try{
			if (StringUtils.isNotBlank(contentLinkId)) {
				List<LinePointMsgContentLinkTracing> msgTracingLinks = msgContentLinkTracingRepository.findByLinkId(contentLinkId);
				if(msgTracingLink == null && msgTracingLinks.size() == 0){
					msgTracingLink = new LinePointMsgContentLinkTracing();
					msgTracingLink.setLinkId(contentLinkId);
					msgTracingLink.setLinkIdBinded(contentLinkId);
					msgTracingLink.setLinkIdUnMobile(contentLinkId);
					msgTracingLink.setModifyTime(new Date());
					msgContentLinkTracingRepository.save(msgTracingLink);
				}else{
					msgTracingLink = msgTracingLinks.get(0); 
				}
				
			} else {
				throw new Exception("ContentLinkId is null");
			}
		}catch(Exception e){
			throw new Exception("查無資料");
		}
		
		return msgTracingLink.getTracingId();
	}

}
