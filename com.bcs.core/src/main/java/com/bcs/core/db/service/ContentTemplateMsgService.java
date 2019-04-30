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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ContentLink;
import com.bcs.core.db.entity.ContentTemplateMsg;
import com.bcs.core.db.entity.ContentTemplateMsgAction;
import com.bcs.core.db.repository.ContentTemplateMsgActionRepository;
import com.bcs.core.db.repository.ContentTemplateMsgRepository;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ContentTemplateMsgService {
	public static final String TEMPLATE_SYNC = "TEMPLATE_SYNC";
	@Autowired
	private ContentTemplateMsgRepository contentTemplateMsgRepository;
	@Autowired
	private ContentTemplateMsgActionRepository contentTemplateMsgActionRepository;
	@Autowired
	private ContentLinkService contentLinkService;
	
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
				Boolean isReSyncData = DataSyncUtil.isReSyncData(TEMPLATE_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(TEMPLATE_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	public ContentTemplateMsgService(){

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
    
	public List<String> getPreTemplateIds(String templateId) {
		List<String> list = new ArrayList<>();
		List<ContentTemplateMsg> contentTemplateMsgs = contentTemplateMsgRepository.findMainAndColumnByTemplateId(templateId);
		
		for(ContentTemplateMsg contentTemplateMsg : contentTemplateMsgs){
			list.add(contentTemplateMsg.getTemplateId());
			
			contentTemplateMsg.setStatus(ContentTemplateMsg.STATUS_DELETE);
			contentTemplateMsgRepository.save(contentTemplateMsg);
		}
		
    	return list;
    }
	
	public ContentTemplateMsg getSelectedContentTemplateMsg(String templateId) {
    	return contentTemplateMsgRepository.findOne(templateId);
    }
	
	/**
	 * 取得樣板訊息更新前的actionId與LinkId
     */
	@Transactional(rollbackFor=Exception.class)
	public List<Map<String, String>> getPreActionIdAndLinkId(String templateId) {
		List<Map<String, String>> list = new ArrayList<>();
		List<ContentTemplateMsgAction> contentTemplateMsgActions = contentTemplateMsgActionRepository.findByTemplateId(templateId);
		
		for (ContentTemplateMsgAction contentTemplateMsgAction : contentTemplateMsgActions) {
			Map<String, String> map = new LinkedHashMap<>();
			String linkId = contentTemplateMsgAction.getLinkId();
			
			map.put("actionId", contentTemplateMsgAction.getTemplateIdAction());
			map.put("linkId", linkId);
			list.add(map);

			contentTemplateMsgAction.setStatus(ContentTemplateMsgAction.STATUS_DELETE);
			contentTemplateMsgActionRepository.save(contentTemplateMsgAction);
		}
		
    	return list;
    }
	
    /**
	 * 取得樣板訊息
     */
    @SuppressWarnings("unchecked")
	public Map<String, List<String>> getContentTemplateMsg(String templateId) {
		try {
			Map<String, List<String>> result = dataCache.get(templateId);
			if(result != null && result.get(templateId) != null){
				return result;
			}
		} catch (Exception e) {}
		
    	String queryString = 
    			"SELECT BCS_CONTENT_TEMPLATE.TEMPLATE_ID, "
    				+ "BCS_CONTENT_TEMPLATE.ALT_TEXT,"
    				+ "BCS_CONTENT_TEMPLATE.TEMPLATE_TYPE,"
    				+ "BCS_CONTENT_TEMPLATE.TEMPLATE_IMAGE_ID,"
    				+ "BCS_CONTENT_TEMPLATE.TEMPLATE_TITLE,"
    				+ "BCS_CONTENT_TEMPLATE.TEMPLATE_TEXT,"
    				+ "BCS_CONTENT_TEMPLATE.STATUS,"
    				+ "BCS_CONTENT_TEMPLATE_ACTION.ACTION_TYPE,"
    				+ "BCS_CONTENT_TEMPLATE_ACTION.ACTION_LABEL,"
    				+ "BCS_CONTENT_TEMPLATE_ACTION.ACTION_DATA,"
    				+ "BCS_CONTENT_TEMPLATE_ACTION.ACTION_TEXT,"
    				+ "BCS_CONTENT_LINK.LINK_URL, "    				
    				+ "BCS_CONTENT_LINK.LINK_ID "    				
    			+ "FROM BCS_CONTENT_TEMPLATE "
    				+ "LEFT JOIN BCS_CONTENT_TEMPLATE_ACTION ON BCS_CONTENT_TEMPLATE.TEMPLATE_ID = BCS_CONTENT_TEMPLATE_ACTION.TEMPLATE_ID "
    				+ "LEFT JOIN BCS_CONTENT_LINK ON BCS_CONTENT_TEMPLATE_ACTION.LINK_ID = BCS_CONTENT_LINK.LINK_ID "
    			+ "WHERE (BCS_CONTENT_TEMPLATE.TEMPLATE_ID = ?1 OR BCS_CONTENT_TEMPLATE.TEMPLATE_PARENT_ID = ?1) AND BCS_CONTENT_TEMPLATE.STATUS <> 'DELETE' AND BCS_CONTENT_TEMPLATE_ACTION.STATUS <> 'DELETE' "
    			+ "ORDER BY BCS_CONTENT_TEMPLATE.TEMPLATE_LETTER, BCS_CONTENT_TEMPLATE_ACTION.ACTION_LETTER";
    	
    	Query query = entityManager.createNativeQuery(queryString).setParameter(1, templateId);
		List<Object[]> list = query.getResultList();
		
		Map<String, List<String>> map = new LinkedHashMap<>();
		for (Object[] o : list) {
			for (int i=0, max=o.length; i<max; i++) {
				if(i==0){
					List<String> dataList = map.get(o[0]);
					if (dataList == null) {
						map.put(o[0].toString(), new ArrayList<String>());
						continue;
					}else{ //重覆的templateId，因為有多個action
						for(int j=7;j<13;j++){
							if (o[j] == null) {
								dataList.add(null);
							} else {
								dataList.add(o[j].toString());
							}
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
			dataCache.put(templateId, map);
		}
		return map;
    }
    
    /**
	 * 取得樣板訊息所有清單
     */
    @SuppressWarnings("unchecked")
    public  Map<String, List<String>> getAllContentTemplateMsg(){
    	String queryString = 
    			"SELECT BCS_CONTENT_TEMPLATE.TEMPLATE_ID, "
    					+ "BCS_CONTENT_TEMPLATE.TEMPLATE_TEXT, "
    					+ "BCS_CONTENT_TEMPLATE.TEMPLATE_IMAGE_ID, "
    					+ "BCS_CONTENT_TEMPLATE.TEMPLATE_TITLE, "
    					+ "BCS_CONTENT_TEMPLATE.MODIFY_TIME, "
    					+ "BCS_CONTENT_TEMPLATE.TEMPLATE_TYPE, "
    					+ "BCS_ADMIN_USER.USER_NAME "
    			+ "FROM BCS_CONTENT_TEMPLATE "
	    			+ "LEFT JOIN BCS_ADMIN_USER ON BCS_CONTENT_TEMPLATE.MODIFY_USER = BCS_ADMIN_USER.ACCOUNT "
	    		+ "WHERE BCS_CONTENT_TEMPLATE.STATUS = 'ACTIVE' AND BCS_CONTENT_TEMPLATE.TEMPLATE_LEVEL <> 'COLUMN'"
    			+ "ORDER BY BCS_CONTENT_TEMPLATE.MODIFY_TIME DESC";
    	
    	Query query = entityManager.createNativeQuery(queryString);
		List<Object[]> list = query.getResultList();
    	
    	Map<String, List<String>> map = new LinkedHashMap<>();
		for (Object[] o : list) {
			for (int i=0, max=o.length; i<max; i++) {
				if(i==0){
					map.put(o[0].toString(), new ArrayList<String>());
					continue;
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
    	if(queryType == "1"){
    		ContentTemplateMsg contentTemplateMsg = contentTemplateMsgRepository.findOne(uuid);
    		if (contentTemplateMsg == null) return false;
    	} else if (queryType == "2") {
    		ContentTemplateMsgAction contentTemplateMsgAction = contentTemplateMsgActionRepository.findOne(uuid);
    		if (contentTemplateMsgAction == null) return false;
    	} else {
    		ContentLink contentLink = contentLinkService.findOne(uuid);
    		if (contentLink == null) return false;
    	}
    	
		return true;
    }
    
    /**
	 * 新增樣板訊息
     */
    @Transactional(rollbackFor=Exception.class)
	public void createTemplateMsg(List<ContentTemplateMsg> contentTemplateMsgs, List<ContentTemplateMsgAction> contentTemplateMsgActions, List<ContentLink> contentLinks){    	
    	for(ContentTemplateMsg contentTemplateMsg : contentTemplateMsgs){
    		contentTemplateMsgRepository.save(contentTemplateMsg);
    	}
    	
    	for(ContentTemplateMsgAction contentTemplateMsgAction : contentTemplateMsgActions){
    		contentTemplateMsgActionRepository.save(contentTemplateMsgAction);
    	}

    	contentLinkService.save(contentLinks);
    	
    	for(ContentTemplateMsg contentTemplateMsg : contentTemplateMsgs){
    		dataCache.refresh(contentTemplateMsg.getTemplateId());
			DataSyncUtil.settingReSync(TEMPLATE_SYNC);
    	}
	}
    
    /**
	 * 刪除圖文訊息
     */
    @Transactional(rollbackFor=Exception.class)
	public void deleteTemplateMsg(String templateId, String account){
		// 只改變狀態
    	List<ContentTemplateMsg> contentTemplateMsgs = contentTemplateMsgRepository.findMainAndColumnByTemplateId(templateId);
    	
    	for(ContentTemplateMsg contentTemplateMsg : contentTemplateMsgs){
    		contentTemplateMsg.setStatus(ContentTemplateMsg.STATUS_DELETE);
        	contentTemplateMsg.setModifyUser(account);
        	contentTemplateMsg.setModifyTime(new Date());
    		contentTemplateMsgRepository.save(contentTemplateMsg);
    	}

    	for(ContentTemplateMsg contentTemplateMsg : contentTemplateMsgs){
    		dataCache.refresh(contentTemplateMsg.getTemplateId());
			DataSyncUtil.settingReSync(TEMPLATE_SYNC);
    	}
	}
}
