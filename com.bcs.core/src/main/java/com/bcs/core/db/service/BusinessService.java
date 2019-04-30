package com.bcs.core.db.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.Business;
import com.bcs.core.db.repository.BusinessRepository;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class BusinessService {
    
public static final String BUSINESS_SYNC = "BUSINESS_SYNC";
    
    @Autowired
    private BusinessRepository businessRepository;

    protected LoadingCache<String, Map<String, Business>> dataCache;
    
    /** Logger */
    private static Logger logger = Logger.getLogger(BusinessService.class);

    private Timer flushTimer = new Timer();
    
    private class CustomTask extends TimerTask{
        
        @Override
        public void run() {

            try{
                // Check Data Sync
                Boolean isReSyncData = DataSyncUtil.isReSyncData(BUSINESS_SYNC);
                if(isReSyncData){
                    dataCache.invalidateAll();
                    DataSyncUtil.syncDataFinish(BUSINESS_SYNC);
                }
            }
            catch(Throwable e){
                logger.error(ErrorRecord.recordError(e));
            }
        }
    }
    
    public BusinessService(){

        flushTimer.schedule(new CustomTask(), 120000, 30000);

        dataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, Business>>() {
                    @Override
                    public Map<String, Business> load(String key) throws Exception {
                        return new HashMap<String, Business>();
                    }
                });
    }
    
    @PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] BusinessService cleaning up...");
        try{
            if(dataCache != null){
                dataCache.invalidateAll();
                dataCache = null;
            }
        }
        catch(Throwable e){}
        
        System.gc();
        logger.info("[DESTROY] BusinessService destroyed.");
    }
    
    private boolean notNull(Business result){
        if(result != null 
                && StringUtils.isNotBlank(result.getBizId()) 
                && !"-".equals(result.getBizId())
                && StringUtils.isNotBlank(result.getGroupId())){
            return true;
        }
        return false;
    }
    
    private Map<String, Business> putInCache(Business business) {
        Map<String, Business> result = null;
        
        if (notNull(business)) {
            String groupId = business.getGroupId();
            String bizId = business.getBizId();
            
            result = dataCache.getIfPresent(groupId);
            if (result == null) {
                loadBusinessesInCache(groupId);
            }
            result.put(bizId, business);
        }
        
        return result;
    }
    
    private Map<String, Business> removeFromCache(Business business) {
        Map<String, Business> result = null;
        
        if (notNull(business)) {
            String groupId = business.getGroupId();
            String bizId = business.getBizId();
            
            result = dataCache.getIfPresent(groupId);
            if (result != null) {
                result.remove(bizId);
            }
        }
        
        return result;
    }
    
    private void loadBusinessesInCache(String groupId) {
        findByGroupId(groupId);
    }
    
    public List<Business> findByGroupId(String groupId) {
        try {
            Map<String, Business> businessMap = dataCache.get(groupId);
            if(businessMap != null && businessMap.size() > 0){
                
                return new ArrayList<Business>(businessMap.values());
            }
        } catch (Exception e) {}
        
        List<Business> result = businessRepository.findByGroupId(groupId);
        if(result != null){
            for (Business business : result) {
                putInCache(business);
            }
        }
        return result;
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public void deleteByGroupId(String groupId) throws BcsNoticeException{
        logger.debug("delete:" + groupId);
        
        businessRepository.deleteByGroupId(groupId);
        dataCache.invalidate(groupId);
        DataSyncUtil.settingReSync(BUSINESS_SYNC);
    }
    
    public void save(Business business) {
        if (business != null) {
            Long id = business.getId();
            if (id != null) {
                Business oldBusinessInDb = businessRepository.findOne(id);
                removeFromCache(oldBusinessInDb);
            }
        }
        
        businessRepository.save(business);

        if(business != null){
            putInCache(business);
            DataSyncUtil.settingReSync(BUSINESS_SYNC);
        }
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public void delete(Long id) throws BcsNoticeException{
        logger.debug("delete:" + id);
        
        Business business = businessRepository.findOne(id);
        
        businessRepository.delete(business);
        removeFromCache(business);
        DataSyncUtil.settingReSync(BUSINESS_SYNC);
    }
    
    public Business findOne(Long id){        
        Business result = businessRepository.findOne(id);
        if(result != null){
            putInCache(result);
        }
        return result;
    }
    
    public Business findByBizIdAndGroupId(String bizId, String groupId) {
        try {
            Business result = dataCache.get(groupId).get(bizId);
            if(notNull(result)){
                return result;
            }
        } catch (Exception e) {}
        
        Business result = null;
        
        loadBusinessesInCache(groupId);
        Map<String, Business> businesses = dataCache.getIfPresent(groupId);
        if (businesses != null && businesses.size() > 0) {
            result = businesses.get(bizId);
        }
        
        return result;
    }
}
