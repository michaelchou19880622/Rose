package com.bcs.core.db.service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bcs.core.db.entity.BusinessGroup;
import com.bcs.core.db.repository.BusinessGroupRepository;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class BusinessGroupService {
    
public static final String BUSINESS_GROUP_SYNC = "BUSINESS_GROUP_SYNC";
    
    @Autowired
    private BusinessGroupRepository businessGroupRepository;
    
    @Autowired
    private BusinessService businessService;

    protected LoadingCache<String, BusinessGroup> dataCache;
    
    /** Logger */
    private static Logger logger = Logger.getLogger(BusinessGroupRepository.class);

    private Timer flushTimer = new Timer();
    
    private class CustomTask extends TimerTask{
        
        @Override
        public void run() {

            try{
                // Check Data Sync
                Boolean isReSyncData = DataSyncUtil.isReSyncData(BUSINESS_GROUP_SYNC);
                if(isReSyncData){
                    dataCache.invalidateAll();
                    DataSyncUtil.syncDataFinish(BUSINESS_GROUP_SYNC);
                }
            }
            catch(Throwable e){
                logger.error(ErrorRecord.recordError(e));
            }
        }
    }
    
    public BusinessGroupService(){

        flushTimer.schedule(new CustomTask(), 120000, 30000);

        dataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, BusinessGroup>() {
                    @Override
                    public BusinessGroup load(String key) throws Exception {
                        return new BusinessGroup();
                    }
                });
    }
    
    @PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] BusinessGroupService cleaning up...");
        try{
            if(dataCache != null){
                dataCache.invalidateAll();
                dataCache = null;
            }
        }
        catch(Throwable e){}
        
        System.gc();
        logger.info("[DESTROY] BusinessGroupService destroyed.");
    }
    
    private boolean notNull(BusinessGroup result){
        if(result != null && StringUtils.isNotBlank(result.getGroupId())){
            return true;
        }
        return false;
    }
    
    public BusinessGroup save(BusinessGroup businessGroup){
        if(businessGroup.getGroupId() == null) {
            businessGroup.setGroupId(generateGroupId());
        }
        businessGroupRepository.save(businessGroup);
        if(businessGroup != null){
            dataCache.put(businessGroup.getGroupId(), businessGroup);
            DataSyncUtil.settingReSync(BUSINESS_GROUP_SYNC);
        }
        return businessGroup;
    }
    
    public List<BusinessGroup> getAllList(){
        return businessGroupRepository.findAll();
    }
    
    public void delete(String groupId) throws BcsNoticeException {
        logger.debug("delete:" + groupId);
        
        businessGroupRepository.delete(groupId);
        dataCache.invalidate(groupId);
        
        businessService.deleteByGroupId(groupId);
        DataSyncUtil.settingReSync(BUSINESS_GROUP_SYNC);
    }
    
    public BusinessGroup findOne(String groupId){
        try {
            BusinessGroup result = dataCache.get(groupId);
            if(notNull(result)){
                return result;
            }
        } catch (Exception e) {}
        
        BusinessGroup result = businessGroupRepository.findOne(groupId);
        if(result != null){
            dataCache.put(result.getGroupId(), result);
        }
        return result;
    }
    
    public BusinessGroup findByName(String name) {
        BusinessGroup result = businessGroupRepository.findByGroupName(name);
        if(result != null){
            dataCache.put(result.getGroupId(), result);
        }
        return result;
    }
    
    public String generateGroupId() {
        String groupId = UUID.randomUUID().toString().toLowerCase();
        
        while (businessGroupRepository.findOne(groupId) != null) {
            groupId = UUID.randomUUID().toString().toLowerCase();
        }
        return groupId;
    }
}
