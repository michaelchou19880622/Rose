package com.bcs.web.ui.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.Business;
import com.bcs.core.db.service.BusinessService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;

@Service
public class BusinessUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(BusinessUIService.class);
	
	@Autowired
	private BusinessService businessService;
    @Autowired
    private ImportDataFromExcel importDataFromExcel;
    @Autowired
    private ImportDataFromText importDataFromText;

	@Transactional(rollbackFor=Exception.class, timeout = 300)
    public Map<String, Object> uploadBusinessList(MultipartFile filePart, String modifyUser, Date modifyTime, String groupId) throws Exception{

        String fileName = filePart.getOriginalFilename();
        logger.info("getOriginalFilename:" + fileName);
        String contentType = filePart.getContentType();
        logger.info("getContentType:" + contentType);
        logger.info("getSize:" + filePart.getSize());

        Set<String> businesses = null;
        if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
            businesses = importDataFromExcel.importData(filePart.getInputStream());    
        }
        else if("text/plain".equals(contentType)){
            businesses = importDataFromText.importData(filePart.getInputStream()); 
        }
        
        if(businesses != null && businesses.size() > 0){
            List<String> list = new ArrayList<String>(businesses);

            List<String> newBizIds = new ArrayList<String>();
            
            // Check Business Exist by Part
            for(int i = 1; i <= list.size(); i++){
                String bizId = list.get(i-1);
                if (bizId.length() == 8 && businessService.findByBizIdAndGroupId(bizId, groupId) == null) {
                    newBizIds.add(bizId);
                }
            }

            if(newBizIds != null && newBizIds.size() > 0){
                logger.debug("newBizIds:" + newBizIds);
                
                for(String bizId : newBizIds){
                    Business business = new Business();
                    
                    business.setBizId(bizId);
                    
                    business.setModifyTime(modifyTime);
                    business.setModifyUser(modifyUser);
                    business.setGroupId(groupId);
                    
                    businessService.save(business);
                }

                Map<String, Object> result = new HashMap<String, Object>();
                result.put("count", newBizIds.size());
                
                return result;
            }
            else{
                throw new BcsNoticeException("沒有上傳通路");
            }
        }
        else if(businesses == null){
            throw new BcsNoticeException("上傳格式錯誤");
        }
        else{
            throw new BcsNoticeException("沒有上傳通路");
        }
    }
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public void deleteFromUI(Long id, String account) throws BcsNoticeException {
        logger.info("deleteFromUI:" + id);
        
        Business business = businessService.findOne(id);
        String bizId = business.getBizId();
        businessService.delete(id);
        createSystemLog("Delete", bizId, account, new Date(), id.toString());
    }
	
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
        SystemLogUtil.saveLogDebug("Business", action, modifyUser, content, referenceId);
    }
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public Business saveFromUI(Business business, String account) throws BcsNoticeException{
        logger.info("saveFromUI:" + business);

        Long id = business.getId();
        
        Business businessInDb = businessService.findByBizIdAndGroupId(business.getBizId(), business.getGroupId());
        
        if (businessInDb == null) {
            String action = (id == null ? "Create" : "Edit");
            
            // Set Modify Admin User
            business.setModifyUser(account);
            business.setModifyTime(new Date());
            
            // Save Business
            businessService.save(business);
            
            business = businessService.findOne(business.getId());
            createSystemLog(action, business, business.getModifyUser(), business.getModifyTime(), business.getId().toString());
            return business;
        } else {
            logger.debug("BizId:" + business.getBizId() + " have already exist.");
            
            return businessInDb;
        }
    }
}
