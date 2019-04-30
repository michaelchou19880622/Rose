package com.bcs.core.db.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ProductGroup;
import com.bcs.core.db.repository.ProductGroupRepository;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ProductGroupService {
	public static final String PRODUCT_GROUP_SYNC = "PRODUCT_GROUP_SYNC";
	
	@Autowired
	private ProductGroupRepository productGroupRepository;
	
	@Autowired
	private ProductService productService;

    protected LoadingCache<String, ProductGroup> dataCache;
    
    /** Logger */
    private static Logger logger = Logger.getLogger(ProductGroupService.class);

	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(PRODUCT_GROUP_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(PRODUCT_GROUP_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
    
    public ProductGroupService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);

        dataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, ProductGroup>() {
                    @Override
                    public ProductGroup load(String key) throws Exception {
                        return new ProductGroup();
                    }
                });
    }
    
    @PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] ProductGroupService cleaning up...");
        try{
            if(dataCache != null){
                dataCache.invalidateAll();
                dataCache = null;
            }
        }
        catch(Throwable e){}
        
        System.gc();
        logger.info("[DESTROY] ProductGroupService destroyed.");
    }
    
    private boolean notNull(ProductGroup result){
        if(result != null && StringUtils.isNotBlank(result.getGroupId())){
            return true;
        }
        return false;
    }
    
    public ProductGroup findByName(String name) {
        ProductGroup result = productGroupRepository.findByGroupName(name);
        if(result != null){
            dataCache.put(result.getGroupId(), result);
        }
        return result;
    }
    

    public List<ProductGroup> findAll() {
        return productGroupRepository.findAll();
    }


    public Long countAll() {
        return productGroupRepository.count();
    }

    public void save(ProductGroup productGroup) {
        productGroupRepository.save(productGroup);

        if(productGroup != null){
            dataCache.put(productGroup.getGroupId(), productGroup);
			DataSyncUtil.settingReSync(PRODUCT_GROUP_SYNC);
        }
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public void delete(String groupId) throws BcsNoticeException{
        logger.debug("delete:" + groupId);
        
        ProductGroup productGroup = productGroupRepository.findOne(groupId);
        
        productGroupRepository.delete(productGroup);
        dataCache.invalidate(groupId);
        
        productService.deleteByGroupId(groupId);
		DataSyncUtil.settingReSync(PRODUCT_GROUP_SYNC);
    }
    
    public String findGroupNameByGroupId(String groupId) throws BcsNoticeException{
        try {
            ProductGroup result = dataCache.get(groupId);
            if(notNull(result)){
                return result.getGroupName();
            }
        } catch (Exception e) {}
        
        return productGroupRepository.findGroupNameByGroupId(groupId);
    }
    
    public ProductGroup findOne(String groupId){
        try {
            ProductGroup result = dataCache.get(groupId);
            if(notNull(result)){
                return result;
            }
        } catch (Exception e) {}
        
        ProductGroup result = productGroupRepository.findOne(groupId);
        if(result != null){
            dataCache.put(result.getGroupId(), result);
        }
        return result;
    }
    
    public Map<String, String> findGroupNameMap(){
        List<Object[]> groups = productGroupRepository.findAllGroupIdAndGroupName();
        logger.debug("findGroupTitleMap:" + ObjectUtil.objectToJsonStr(groups));
        Map<String, String> result = new LinkedHashMap<String, String>();
        
        for(Object[] group : groups){
            String groupId = (String) group[0];
            String groupName = (String) group[1];
            result.put(groupId, groupName);
        }
        
        return result;
    }
    
    public String generateGroupId() {
        String groupId = UUID.randomUUID().toString().toLowerCase();
        
        while (productGroupRepository.findOne(groupId) != null) {
            groupId = UUID.randomUUID().toString().toLowerCase();
        }
        return groupId;
    }
}
