package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.db.entity.Product;
import com.bcs.core.db.entity.ProductGroup;
import com.bcs.core.db.service.ProductGroupService;
import com.bcs.core.db.service.ProductService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.ProductGroupUIService;
import com.bcs.web.ui.service.ProductUIService;


@Controller
@RequestMapping("/bcs")
public class BCSProductController extends BCSBaseController {
	
	@Autowired
	private ProductService productService;
	@Autowired
	private ProductUIService productUIService;
    
    @Autowired
    private ProductGroupService productGroupService;
    @Autowired
    private ProductGroupUIService productGroupUIService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSProductController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/admin/productCreatePage")
	public String productCreatePage(
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		logger.info("productCreatePage");
				
		return BcsPageEnum.ProductCreatePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/uploadProductList")
    @ResponseBody
    public ResponseEntity<?> uploadProductList(@RequestPart MultipartFile filePart,
            @CurrentUser CustomUser customUser,  
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("uploadProductList");

        String groupId = request.getParameter("groupId");
        try{
            if(filePart != null){
                String currentUser = customUser.getAccount();
                
                Map<String, Object> result = productUIService.uploadProductList(filePart, currentUser, new Date(), groupId);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Upload Product List Null");
            }
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getProductList")
    @ResponseBody
    public ResponseEntity<?> getProductList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getProductList");

        String groupId = request.getParameter("groupId");
        List<Product> result = productService.findByGroupId(groupId);
        
        logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteProduct")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(
            @CurrentUser CustomUser customUser,
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("deleteProduct");
        String productId = request.getParameter("productId");
        
        try{
            if(StringUtils.isNotBlank(productId)){
                logger.info("productId:" + productId);
                productUIService.deleteFromUI(Long.parseLong(productId), customUser.getAccount());
                
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            }
            else{
                throw new Exception("Product Id Null");
            }
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getProduct")
    @ResponseBody
    public ResponseEntity<?> getProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getProduct");        
        String productId = request.getParameter("productId");
        
        try{
            if(StringUtils.isNotBlank(productId)){
                logger.info("productId:" + productId);
                Product product = productService.findOne(Long.parseLong(productId));
                
                if(product != null){
                    return new ResponseEntity<>(product, HttpStatus.OK);
                }
            }
            
            throw new Exception("Product Id Null");
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/createProduct", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createProduct(@RequestBody Product product,
            @CurrentUser CustomUser customUser,   
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("createProduct");
        
        try{
            
            if(product != null){
                if(StringUtils.isBlank(product.getProductName())){
                    throw new Exception("ProductName Null");
                } else if (StringUtils.isBlank(product.getGroupId())){
                    throw new Exception("GroupId Null");
                }
                
                String account = customUser.getAccount();
                
                Product result = productUIService.saveFromUI(product, account);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Product Null");
            }
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }


    @RequestMapping(method = RequestMethod.GET, value = "/admin/productGroupCreatePage")
    public String productGroupCreatePage(
            HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
        logger.info("productGroupCreatePage");
                
        return BcsPageEnum.ProductGroupCreatePage.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/productGroupListPage")
    public String productGroupListPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("productGroupListPage");
        return BcsPageEnum.ProductGroupListPage.toString();
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/admin/getProductGroupList")
    @ResponseBody
    public ResponseEntity<?> getProductGroupList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getProductGroupList");

        List<ProductGroup> result = productGroupService.findAll();
        
        logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteProductGroup")
    @ResponseBody
    public ResponseEntity<?> deleteProductGroup(
            @CurrentUser CustomUser customUser,
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("deleteProductGroup");
        String groupId = request.getParameter("groupId");
        
        try{
            if(StringUtils.isNotBlank(groupId)){
                logger.info("groupId:" + groupId);
                productGroupUIService.deleteFromUI(groupId, customUser.getAccount());
                
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            }
            else{
                throw new Exception("ProductGroup Id Null");
            }
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/admin/getProductGroup")
    @ResponseBody
    public ResponseEntity<?> getProductGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getProductGroup");        
        String groupId = request.getParameter("groupId");
        
        try{
            if(StringUtils.isNotBlank(groupId)){
                logger.info("groupId:" + groupId);
                ProductGroup productGroup = productGroupService.findOne(groupId);
                
                if(productGroup != null){
                    return new ResponseEntity<>(productGroup, HttpStatus.OK);
                }
            }
            
            throw new Exception("ProductGroup Id Null");
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/admin/createProductGroup", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createProductGroup(@RequestBody ProductGroup productGroup,
            @CurrentUser CustomUser customUser,   
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("createProductGroup");
        
        try{
            if(productGroup != null){
                if(StringUtils.isBlank(productGroup.getGroupName())){
                    throw new Exception("GroupName Null");
                }
                
                String account = customUser.getAccount();
                
                ProductGroup result = productGroupUIService.saveFromUI(productGroup, account);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("ProductGroup Null");
            }
        }
        catch(Exception e){
            logger.error(ErrorRecord.recordError(e));

            if(e instanceof BcsNoticeException){
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
            }
            else{
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/admin/getProductGroupNameList")
    @ResponseBody
    public ResponseEntity<?> getProductGroupNameList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getProductGroupNameList");       
        Map<String, String> map = productGroupService.findGroupNameMap();
        logger.debug("map:" + ObjectUtil.objectToJsonStr(map));
        return new ResponseEntity<>(map, HttpStatus.OK);
    }
}
