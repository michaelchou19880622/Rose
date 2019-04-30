package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.api.msg.model.RichMsgAction;
import com.bcs.core.db.entity.ContentFlag;
import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentRichMsg;
import com.bcs.core.db.entity.ContentRichMsgDetail;
import com.bcs.core.db.repository.ContentResourceRepository;
import com.bcs.core.db.repository.ContentRichMsgDetailRepository;
import com.bcs.core.db.repository.ContentRichMsgRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ContentRichMsgService {
	public static final String RICHMSG_SYNC = "RICHMSG_SYNC";
	
	@Autowired
	private ContentRichMsgRepository contentRichMsgRepository;
	@Autowired
	private ContentRichMsgDetailRepository contentRichMsgDetailRepository;
	@Autowired
	private ContentLinkService contentLinkService;
	@Autowired
	private ContentResourceRepository contentResourceRepository;
	@Autowired
	private ContentFlagService contentFlagService;
	
	@PersistenceContext
    EntityManager entityManager;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentRichMsgService.class);

	protected LoadingCache<String, Map<String, List<String>>> dataCache;

	private Timer flushTimer = new Timer();

	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(RICHMSG_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(RICHMSG_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
	
	public ContentRichMsgService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);

		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, Map<String, List<String>>>() {
					@Override
					public Map<String, List<String>> load(String key) throws Exception {
						return new HashMap<String, List<String>>();
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentRichMsgService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] ContentRichMsgService destroyed.");
	}
    
	public ContentRichMsg getSelectedContentRichMsg(String richId) {
    	return contentRichMsgRepository.findOne(richId);
    }
	
	/**
	 * 取得圖文訊息更新前的DetailId與LinkId
     */
	@Transactional(rollbackFor=Exception.class, timeout = 30)
	public List<Map<String, String>> getPreDetailIdAndLinkId(String richId) {
		List<Map<String, String>> list = new ArrayList<>();
		List<ContentRichMsgDetail> contentRichMsgDetails = contentRichMsgDetailRepository.findByRichId(richId);
		for (ContentRichMsgDetail contentRichMsgDetail : contentRichMsgDetails) {
			Map<String, String> map = new LinkedHashMap<>();
			String linkId = contentRichMsgDetail.getLinkId();
			
			map.put("richDetailId", contentRichMsgDetail.getRichDetailId());
			map.put("linkId", linkId);
			list.add(map);

			contentRichMsgDetail.setStatus(ContentRichMsgDetail.STATUS_DELETE);
			contentRichMsgDetailRepository.save(contentRichMsgDetail);
		}
		
    	return list;
    }
	
	/**
	 * 取得圖文訊息
     */
    @SuppressWarnings("unchecked")
	public Map<String, List<String>> getContentRichMsg(String richId) {
		try {
			Map<String, List<String>> result = dataCache.get(richId);
			if(result != null && result.get(richId) != null){
				return result;
			}
		} catch (Exception e) {}
		
    	String queryString = 
    			"SELECT BCS_CONTENT_RICH.RICH_ID, "
    				+ "BCS_CONTENT_RICH.RICH_TYPE, "
    				+ "BCS_CONTENT_RICH.RICH_TITLE, "
					+ "BCS_CONTENT_RICH.RICH_IMAGE_ID, "
					+ "BCS_CONTENT_LINK.LINK_URL, "
					+ "BCS_CONTENT_LINK.LINK_TITLE, "
					+ "BCS_CONTENT_LINK.LINK_TAG, "
					+ "BCS_CONTENT_RICH_DETAIL.START_POINT_X, "
					+ "BCS_CONTENT_RICH_DETAIL.START_POINT_Y, "
					+ "BCS_CONTENT_RICH_DETAIL.END_POINT_X, "
					+ "BCS_CONTENT_RICH_DETAIL.END_POINT_Y, "
					+ "BCS_CONTENT_RESOURCE.RESOURCE_HEIGHT, "
					+ "BCS_CONTENT_RESOURCE.RESOURCE_WIDTH, "
					+ "BCS_CONTENT_RICH_DETAIL.LINK_ID, "
					+ "BCS_CONTENT_RICH.STATUS, "
					+ "BCS_CONTENT_RICH_DETAIL.ACTION_TYPE "
    			+ "FROM BCS_CONTENT_RICH "
	    			+ "LEFT JOIN BCS_CONTENT_RICH_DETAIL ON BCS_CONTENT_RICH.RICH_ID = BCS_CONTENT_RICH_DETAIL.RICH_ID "
	    			+ "LEFT JOIN BCS_CONTENT_LINK ON BCS_CONTENT_RICH_DETAIL.LINK_ID = BCS_CONTENT_LINK.LINK_ID "
	    			+ "LEFT JOIN BCS_CONTENT_RESOURCE ON BCS_CONTENT_RICH.RICH_IMAGE_ID = BCS_CONTENT_RESOURCE.RESOURCE_ID "
    			+ "WHERE BCS_CONTENT_RICH.RICH_ID = ?1 AND (BCS_CONTENT_RICH_DETAIL.STATUS <> 'DELETE' OR BCS_CONTENT_RICH_DETAIL.STATUS IS NULL) "
    			+ "ORDER BY BCS_CONTENT_RICH_DETAIL.RICH_DETAIL_LETTER";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, richId);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
    	
		Map<String, List<String>> map = new LinkedHashMap<>();
		for (Object[] o : list) {
			for (int i=0, max=o.length; i<max; i++) {
				if (i == 0) {
					List<String> dataList = map.get(o[0]);
					if (dataList == null) {
						map.put(o[0].toString(), new ArrayList<String>());
						continue;
					} else { //重覆的richId，因為有多個連結
						for (int j=3; j<=9; j++) {
							String appendValue = (o[j+1] == null ? "null" : o[j+1].toString());
							dataList.set(j, dataList.get(j) + "," + appendValue);
						}
						for (int j=12; j<=12; j++) {
							dataList.set(j, dataList.get(j) + "," + o[j+1].toString());
						}
						for (int j=14; j<=14; j++) {
							String last = dataList.get(j);
							if(StringUtils.isBlank(last)){
								last = RichMsgAction.ACTION_TYPE_WEB;
							}
							String newStr = (String) o[j+1];
							if(StringUtils.isBlank(newStr)){
								newStr = RichMsgAction.ACTION_TYPE_WEB;
							}
							dataList.set(j, last + "," + newStr);
						}
						break;
					}
				}
				
				List<String> dataList = map.get(o[0]);
				if (o[i] == null) {
					dataList.add(null);
				} else {
					dataList.add(o[i].toString());
				}
			}
		}
		
    	logger.debug(map);
		if(map != null){
			dataCache.put(richId, map);
		}
		return map;
    }
	
    /**
	 * 取得圖文訊息所有清單
     */
    @SuppressWarnings("unchecked")
	public Map<String, List<String>> getAllContentRichMsg(){
    	
    	String queryString = 
    			"SELECT BCS_CONTENT_RICH.RICH_ID, "
    					+ "BCS_CONTENT_RICH.RICH_TITLE, "
    					+ "BCS_CONTENT_LINK.LINK_URL, "
    					+ "BCS_CONTENT_RICH.MODIFY_TIME, "
    					+ "BCS_ADMIN_USER.USER_NAME, "
    					+ "BCS_CONTENT_RICH.RICH_IMAGE_ID, "
    					+ "BCS_CONTENT_LINK.LINK_TITLE, "
    					+ "BCS_CONTENT_RICH.STATUS, "
    					+ "BCS_CONTENT_RICH_DETAIL.ACTION_TYPE, "
    					+ "BCS_CONTENT_RICH_DETAIL.LINK_ID "
    			+ "FROM BCS_CONTENT_RICH "
	    			+ "LEFT JOIN BCS_CONTENT_RICH_DETAIL ON BCS_CONTENT_RICH.RICH_ID = BCS_CONTENT_RICH_DETAIL.RICH_ID "
	    			+ "LEFT JOIN BCS_ADMIN_USER ON BCS_CONTENT_RICH.MODIFY_USER = BCS_ADMIN_USER.ACCOUNT "
	    			+ "LEFT JOIN BCS_CONTENT_LINK ON BCS_CONTENT_RICH_DETAIL.LINK_ID = BCS_CONTENT_LINK.LINK_ID "
	    		+ "WHERE BCS_CONTENT_RICH.STATUS <> 'DELETE' AND (BCS_CONTENT_RICH_DETAIL.STATUS <> 'DELETE' OR BCS_CONTENT_RICH_DETAIL.STATUS IS NULL)"
    			+ "ORDER BY BCS_CONTENT_RICH.MODIFY_TIME DESC, BCS_CONTENT_RICH_DETAIL.RICH_DETAIL_LETTER";
    	
    	Query query = entityManager.createNativeQuery(queryString);
		query.setHint("javax.persistence.query.timeout", 30000);
		List<Object[]> list = query.getResultList();
		
		Map<String, List<String>> map = new LinkedHashMap<>();
		for (Object[] o : list) {
			for (int i=0, max=o.length; i<max; i++) {
				if (i == 0) {
					List<String> dataList = map.get(o[0]);
					if (dataList == null) {
						map.put(o[0].toString(), new ArrayList<String>());
						continue;
					} else { //重覆的richId，因為有多個連結
						// Link Url
						if(o[2] != null){
							dataList.set(1, dataList.get(1) + "," + o[2].toString());
						}
						else{
							dataList.set(1, dataList.get(1) + ",null");
						}
						// Link Title
						if(o[6] != null){
							dataList.set(5, dataList.get(5) + "," + o[6].toString());
						}
						else{
							dataList.set(5, dataList.get(5) + ",null");
						}
						
						// ACTION_TYPE
						String last = dataList.get(7);
						if(StringUtils.isBlank(last)){
							last = RichMsgAction.ACTION_TYPE_WEB;
						}
						String newStr = (String) o[8];
						if(StringUtils.isBlank(newStr)){
							newStr = RichMsgAction.ACTION_TYPE_WEB;
						}
						dataList.set(7, last + "," + newStr);

						// Link ID
						dataList.set(8, dataList.get(8) + "," + o[9].toString());
						break;
					}
				}
				
				List<String> dataList = map.get(o[0]);
				if (o[i] == null) {
					dataList.add(null);
				} else {
					dataList.add(o[i].toString());
				}
			}
		}
		
    	logger.debug(map);
    	
		return map;
	}
    
    /**
	 *  檢查有無重覆使用到UUID
     */
    public Boolean checkDuplicateUUID(String queryType, String uuid) {
    	if (queryType == "1") {
    		ContentRichMsg contentRichMsg = contentRichMsgRepository.findOne(uuid);
    		if (contentRichMsg == null) return false;
    	} else if (queryType == "2") {
    		ContentRichMsgDetail contentRichMsgDetail = contentRichMsgDetailRepository.findOne(uuid);
    		if (contentRichMsgDetail == null) return false;
    	} else {
    		ContentLink contentLink = contentLinkService.findOne(uuid);
    		if (contentLink == null) return false;
    	}
    	
		return true;
    }
    
    /**
	 * 新增圖文訊息
     */
    @Transactional(rollbackFor=Exception.class, timeout = 30)
	public void createRichMsg(
			ContentRichMsg contentRichMsg,  
			List<ContentRichMsgDetail> contentRichMsgDetails, 
			List<ContentLink> contentLinks, 
			Map<String, List<String>> contentFlagMap){
		contentRichMsgRepository.save(contentRichMsg);
    	contentRichMsgDetailRepository.save(contentRichMsgDetails);
    	contentLinkService.save(contentLinks);

    	// Save ContentFlag
    	for (Map.Entry<String, List<String>> entry : contentFlagMap.entrySet()) {
    		contentFlagService.save(
    				entry.getKey(), 
    				ContentFlag.CONTENT_TYPE_LINK, 
    				entry.getValue());
		}
    	
		dataCache.refresh(contentRichMsg.getRichId());
		DataSyncUtil.settingReSync(RICHMSG_SYNC);
	}
    
    /**
	 * 刪除圖文訊息
     */
    @Transactional(rollbackFor=Exception.class, timeout = 30)
	public void deleteRichMsg(String richId, String account){
		// 只改變狀態
		ContentRichMsg contentRichMsg = contentRichMsgRepository.findOne(richId);
		contentRichMsg.setStatus(ContentRichMsg.STATUS_DELETE);
		contentRichMsg.setModifyUser(account);
		contentRichMsg.setModifyTime(new Date());
		
		contentRichMsgRepository.save(contentRichMsg);

		dataCache.refresh(contentRichMsg.getRichId());
		DataSyncUtil.settingReSync(RICHMSG_SYNC);
	}
}
