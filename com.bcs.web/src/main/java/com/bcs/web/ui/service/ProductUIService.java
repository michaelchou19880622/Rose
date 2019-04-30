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

import com.bcs.core.db.entity.Product;
import com.bcs.core.db.service.ProductService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.log.util.SystemLogUtil;
import com.bcs.core.upload.ImportDataFromExcel;
import com.bcs.core.upload.ImportDataFromText;

@Service
public class ProductUIService {

	/** Logger */
	private static Logger logger = Logger.getLogger(ProductUIService.class);
	
	@Autowired
	private ProductService productService;
    @Autowired
    private ImportDataFromExcel importDataFromExcel;
    @Autowired
    private ImportDataFromText importDataFromText;

	@Transactional(rollbackFor=Exception.class, timeout = 300)
    public Map<String, Object> uploadProductList(MultipartFile filePart, String modifyUser, Date modifyTime, String groupId) throws Exception{

        String fileName = filePart.getOriginalFilename();
        logger.info("getOriginalFilename:" + fileName);
        String contentType = filePart.getContentType();
        logger.info("getContentType:" + contentType);
        logger.info("getSize:" + filePart.getSize());

        Set<String> products = null;
        if("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType) || "application/vnd.ms-excel".equals(contentType)){
            products = importDataFromExcel.importData(filePart.getInputStream());    
        }
        else if("text/plain".equals(contentType)){
            products = importDataFromText.importData(filePart.getInputStream()); 
        }
        
        if(products != null && products.size() > 0){
            List<String> list = new ArrayList<String>(products);

            List<String> newProductNames = new ArrayList<String>();
            
            // Check Product Exist by Part
            for(int i = 1; i <= list.size(); i++){
                String productName = list.get(i-1);
                if (productService.findByProductNameAndGroupId(productName, groupId) == null) {
                    newProductNames.add(productName);
                }
            }

            if(newProductNames != null && newProductNames.size() > 0){
                logger.debug("newProductNames:" + newProductNames);
                
                for(String productName : newProductNames){
                    Product product = new Product();
                    
                    product.setProductName(productName);
                    
                    product.setModifyTime(modifyTime);
                    product.setModifyUser(modifyUser);
                    product.setGroupId(groupId);
                    
                    productService.save(product);
                }

                Map<String, Object> result = new HashMap<String, Object>();
                result.put("count", newProductNames.size());
                
                return result;
            }
            else{
                throw new BcsNoticeException("沒有上傳商品");
            }
        }
        else if(products == null){
            throw new BcsNoticeException("上傳格式錯誤");
        }
        else{
            throw new BcsNoticeException("沒有上傳商品");
        }
    }
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public void deleteFromUI(Long productId, String account) throws BcsNoticeException {
        logger.info("deleteFromUI:" + productId);
        
        String productName = productService.findProductNameByProductId(productId);
        productService.delete(productId);
        createSystemLog("Delete", productName, account, new Date(), productId.toString());
    }
	
	private void createSystemLog(String action, Object content, String modifyUser, Date modifyTime, String referenceId) {
        SystemLogUtil.saveLogDebug("Product", action, modifyUser, content, referenceId);
    }
	
	@Transactional(rollbackFor=Exception.class, timeout = 30)
    public Product saveFromUI(Product product, String account) throws BcsNoticeException{
        logger.info("saveFromUI:" + product);

        Long productId = product.getProductId();
        
        Product productInDb = productService.findByProductNameAndGroupId(product.getProductName(), product.getGroupId());
        
        if (productInDb == null) {
            String action = (productId == null ? "Create" : "Edit");
            
            // Set Modify Admin User
            product.setModifyUser(account);
            product.setModifyTime(new Date());
            
            // Save Product
            productService.save(product);
            
            product = productService.findOne(product.getProductId());
            createSystemLog(action, product, product.getModifyUser(), product.getModifyTime(), product.getProductId().toString());
            return product;
        } else {
            logger.debug("productName:" + product.getProductName() + " have already exist.");
            
            return productInDb;
        }
    }
}
