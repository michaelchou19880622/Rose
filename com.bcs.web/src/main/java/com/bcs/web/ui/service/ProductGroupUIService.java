package com.bcs.web.ui.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bcs.core.db.entity.ProductGroup;
import com.bcs.core.db.service.ProductGroupService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;

@Service
public class ProductGroupUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(ProductGroupUIService.class);
	
	@Autowired
	private ProductGroupService productGroupService;
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public void deleteFromUI(String groupId, String account) throws BcsNoticeException {
        logger.info("deleteFromUI:" + groupId);
        
        String groupName = productGroupService.findGroupNameByGroupId(groupId);
        productGroupService.delete(groupId);
        createSystemLog("Delete", groupName, account, new Date(), groupId.toString());
    }
	
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
        SystemLogUtil.saveLogDebug("Product", action, modifyUser, content, referenceId);
    }
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public ProductGroup saveFromUI(ProductGroup productGroup, String account) throws BcsNoticeException{
        logger.info("saveFromUI:" + productGroup);

        String groupId = productGroup.getGroupId();
        
        ProductGroup productGroupInDb = productGroupService.findByName(productGroup.getGroupName());
        
        if (productGroupInDb == null) {
            String action = "Edit";
            
            if (groupId == null) {
                action = "Create";
                
                groupId = productGroupService.generateGroupId(); 
                productGroup.setGroupId(groupId);
            }
            
            // Set Modify Admin User
            productGroup.setModifyUser(account);
            productGroup.setModifyTime(new Date());
            
            // Save ProductGroup
            productGroupService.save(productGroup);
            
            productGroup = productGroupService.findOne(productGroup.getGroupId());
            createSystemLog(action, productGroup, productGroup.getModifyUser(), productGroup.getModifyTime(), productGroup.getGroupId().toString());
            return productGroup;
        } else {
            logger.debug("groupName:" + productGroup.getGroupName() + " have already exist.");
            
            return productGroupInDb;
        }
    }
}
