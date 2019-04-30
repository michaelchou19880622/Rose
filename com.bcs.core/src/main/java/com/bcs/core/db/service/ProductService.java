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

import com.bcs.core.db.entity.Product;
import com.bcs.core.db.repository.ProductRepository;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ProductService {
	public static final String PRODUCT_SYNC = "PRODUCT_SYNC";
	
	@Autowired
	private ProductRepository productRepository;

    protected LoadingCache<String, Map<String, Product>> dataCache;
    
    /** Logger */
    private static Logger logger = Logger.getLogger(ProductService.class);

	private Timer flushTimer = new Timer();
	
	private class CustomTask extends TimerTask{
		
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(PRODUCT_SYNC);
				if(isReSyncData){
					dataCache.invalidateAll();
					DataSyncUtil.syncDataFinish(PRODUCT_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
    
    public ProductService(){

		flushTimer.schedule(new CustomTask(), 120000, 30000);

        dataCache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Map<String, Product>>() {
                    @Override
                    public Map<String, Product> load(String key) throws Exception {
                        return new HashMap<String, Product>();
                    }
                });
    }
    
    @PreDestroy
    public void cleanUp() {
        logger.info("[DESTROY] ProductService cleaning up...");
        try{
            if(dataCache != null){
                dataCache.invalidateAll();
                dataCache = null;
            }
        }
        catch(Throwable e){}
        
        System.gc();
        logger.info("[DESTROY] ProductService destroyed.");
    }
    
    private boolean notNull(Product result){
        if(result != null 
                && StringUtils.isNotBlank(result.getProductName()) 
                && !"-".equals(result.getProductName())
                && StringUtils.isNotBlank(result.getGroupId())){
            return true;
        }
        return false;
    }
    
    public Product findByProductNameAndGroupId(String productName, String groupId) {
        try {
            Product result = dataCache.get(groupId).get(productName);
            if(notNull(result)){
                return result;
            }
        } catch (Exception e) {}
        
        Product result = null;
        
        loadProductsInCache(groupId);
        Map<String, Product> products = dataCache.getIfPresent(groupId);
        if (products != null && products.size() > 0) {
            result = products.get(productName);
        }
        
        return result;
    }
    
    public List<Product> findByGroupId(String groupId) {
        try {
            Map<String, Product> productMap = dataCache.get(groupId);
            if(productMap != null && productMap.size() > 0){
                
                return new ArrayList<Product>(productMap.values());
            }
        } catch (Exception e) {}
        
        List<Product> result = productRepository.findByGroupId(groupId);
        if(result != null){
            for (Product product : result) {
                putInCache(product);
            }
        }
        return result;
    }
    

    public void save(Product product) {
        if (product != null) {
            Long productId = product.getProductId();
            if (productId != null) {
                Product oldProductInDb = productRepository.findOne(productId);
                removeFromCache(oldProductInDb);
            }
        }
        
        productRepository.save(product);

        if(product != null){
            putInCache(product);
    		DataSyncUtil.settingReSync(PRODUCT_SYNC);
        }
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public void delete(Long productId) throws BcsNoticeException{
        logger.debug("delete:" + productId);
        
        Product product = productRepository.findOne(productId);
        
        productRepository.delete(product);
        removeFromCache(product);
		DataSyncUtil.settingReSync(PRODUCT_SYNC);
    }
    
    public String findProductNameByProductId(Long productId) throws BcsNoticeException{
        return productRepository.findProductNameByProductId(productId);
    }
    
    public Product findOne(Long productId){        
        Product result = productRepository.findOne(productId);
        if(result != null){
            putInCache(result);
        }
        return result;
    }
    
    private Map<String, Product> putInCache(Product product) {
        Map<String, Product> result = null;
        
        if (notNull(product)) {
            String groupId = product.getGroupId();
            String productName = product.getProductName();
            
            result = dataCache.getIfPresent(groupId);
            if (result == null) {
                loadProductsInCache(groupId);
            }
            result.put(productName, product);
        }
        
        return result;
    }
    
    private Map<String, Product> removeFromCache(Product product) {
        Map<String, Product> result = null;
        
        if (notNull(product)) {
            String groupId = product.getGroupId();
            String productName = product.getProductName();
            
            result = dataCache.getIfPresent(groupId);
            if (result != null) {
                result.remove(productName);
            }
        }
        
        return result;
    }
    
    @Transactional(rollbackFor=Exception.class, timeout = 30)
    public void deleteByGroupId(String groupId) throws BcsNoticeException{
        logger.debug("delete:" + groupId);
        
        productRepository.deleteByGroupId(groupId);
        dataCache.invalidate(groupId);
		DataSyncUtil.settingReSync(PRODUCT_SYNC);
    }

    private void loadProductsInCache(String groupId) {
        findByGroupId(groupId);
    }
}
