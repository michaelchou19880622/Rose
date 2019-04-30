package com.bcs.core.db.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
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
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.repository.ContentResourceRepository;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.DataSyncUtil;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.FileUtil;
import com.bcs.core.utils.QrcodeGenerator;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class ContentResourceService {
	public static final String RESOURCE_SYNC = "RESOURCE_SYNC";
	
	/** Logger */
	private static Logger logger = Logger.getLogger(ContentResourceService.class);
	
	@Autowired
	private ContentResourceRepository contentResourceRepository;

	protected LoadingCache<String, ContentResource> dataCache; // No Need Sync
	
	private Timer flushTimer = new Timer();

	private class CustomTask extends TimerTask{
		@Override
		public void run() {

			try{
				// Check Data Sync
				Boolean isReSyncData = DataSyncUtil.isReSyncData(RESOURCE_SYNC);
				if(isReSyncData){
					syncResourceFile();
					DataSyncUtil.syncDataFinish(RESOURCE_SYNC);
				}
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}
	
	private void syncResourceFile(){
		List<ContentResource> list = contentResourceRepository.findAll();
		for(ContentResource contentResource : list){
			try{
				FileUtil.loadFromDB(contentResource);
			}
			catch(Throwable e){
				logger.error(ErrorRecord.recordError(e));
			}
		}
	}

	public ContentResourceService(){
		
		flushTimer.schedule(new CustomTask(), 120000, 30000);
		
		dataCache = CacheBuilder.newBuilder()
				.concurrencyLevel(1)
				.expireAfterAccess(30, TimeUnit.MINUTES)
				.build(new CacheLoader<String, ContentResource>() {
					@Override
					public ContentResource load(String key) throws Exception {
						return new ContentResource("-");
					}
				});
	}
	
	@PreDestroy
	public void cleanUp() {
		logger.info("[DESTROY] ContentResourceService cleaning up...");
		try{
			if(dataCache != null){
				dataCache.invalidateAll();
				dataCache = null;
			}
		}
		catch(Throwable e){}
		
		System.gc();
		logger.info("[DESTROY] ContentResourceService destroyed.");
	}
	
	public ContentResource uploadFile(MultipartFile filePart, String resourceType, String modifyUser) throws Exception {
		ContentResource resource = FileUtil.uploadFile(filePart, null, resourceType, modifyUser);
		contentResourceRepository.save(resource);

		if(resource != null){
			dataCache.put(resource.getResourceId(), resource);
		}
		DataSyncUtil.settingReSync(RESOURCE_SYNC);
		
		return resource;
	}
	
	public ContentResource uploadFile(InputStream inputStream, String resourceTitle, Long resourceSize, String contentType, String resourceType, String modifyUser) throws Exception {
		ContentResource resource = FileUtil.uploadFile(inputStream, resourceTitle, resourceSize, contentType, resourceType, modifyUser);
		contentResourceRepository.save(resource);

		if(resource != null){
			dataCache.put(resource.getResourceId(), resource);
		}
		
		return resource;
	}
	
	private boolean notNull(ContentResource result){
		if(result != null && StringUtils.isNotBlank(result.getResourceId()) && !"-".equals(result.getResourceId())){
			return true;
		}
		return false;
	}
	
	public ContentResource findOne(String resourceId){
		try {
			ContentResource result = dataCache.get(resourceId);
			if(notNull(result)){
				return result;
			}
		} catch (Exception e) {}
		
		ContentResource result = contentResourceRepository.findOne(resourceId);
		if(result != null){
			dataCache.put(resourceId, result);
		}
		return result;
	}
	
	public ContentResource createQRImg(String modifyUser, String rewardCardPointId) throws Exception {
	    logger.info("createQRImg:" + rewardCardPointId);
	    OutputStream out = null;
	    
	    String errorMsg = "";
	    boolean isBcsNoticeException = false;
	    try {
	        
    	    String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + ContentResource.RESOURCE_TYPE_QRIMAGE;
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            String resourceId = generateResourceId();
            
            File outputFile = new File(filePath + System.getProperty("file.separator") + resourceId);
            out = new FileOutputStream(outputFile);
            QrcodeGenerator.generateQrcode(UriHelper.getRewardCardGetPointUri(rewardCardPointId), out);

            File inputFile = new File(filePath + System.getProperty("file.separator") + resourceId);
            
            ContentResource resource = new ContentResource();
            
            resource.setResourceId(resourceId);
            resource.setResourceTitle(rewardCardPointId + ".png");
            resource.setResourceSize(inputFile.length());
            resource.setResourceType(ContentResource.RESOURCE_TYPE_QRIMAGE);
            resource.setResourcePreview(ContentResource.RESOURCE_TYPE_IMAGE);
            resource.setModifyUser(modifyUser);
            resource.setModifyTime(new Date());
            resource.setContentType("image/png");
            resource.setResourceHeight(400L);
            resource.setResourceWidth(400L);
            resource.setUseFlag(false);
            
            contentResourceRepository.save(resource);
            if(resource != null){
                dataCache.put(resource.getResourceId(), resource);
            }
            
            return resource;
	    }catch(Exception e) {
	        logger.error(ErrorRecord.recordError(e));
	        if(e instanceof BcsNoticeException){
                isBcsNoticeException = true;
            }
            errorMsg = e.getMessage();
	    }finally {
            if (out != null) {
                out.close();
            }
            logger.debug("finally");
	    }
	    if(isBcsNoticeException){
            throw new BcsNoticeException(errorMsg);
        }
        else{
            throw new Exception(errorMsg);
        }
	}
	
    public String generateResourceId() {
        String resourceId = UUID.randomUUID().toString().toLowerCase();
        
        while (contentResourceRepository.findOne(resourceId) != null) {
            resourceId = UUID.randomUUID().toString().toLowerCase();
        }
        return resourceId;
    }
}
