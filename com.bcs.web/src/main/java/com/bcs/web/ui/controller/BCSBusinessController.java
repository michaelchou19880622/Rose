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

import com.bcs.core.db.entity.Business;
import com.bcs.core.db.entity.BusinessGroup;
import com.bcs.core.db.service.BusinessGroupService;
import com.bcs.core.db.service.BusinessService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.BusinessGroupUIService;
import com.bcs.web.ui.service.BusinessUIService;


@Controller
@RequestMapping("/bcs")
public class BCSBusinessController extends BCSBaseController {
	
	@Autowired
	private BusinessService businessService;
	@Autowired
	private BusinessUIService businessUIService;
    
    @Autowired
    private BusinessGroupService businessGroupService;
    @Autowired
    private BusinessGroupUIService businessGroupUIService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSBusinessController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/admin/businessCreatePage")
	public String businessCreatePage(
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		logger.info("businessCreatePage");
				
		return BcsPageEnum.BusinessCreatePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/uploadBusinessList")
    @ResponseBody
    public ResponseEntity<?> uploadBusinessList(@RequestPart MultipartFile filePart,
            @CurrentUser CustomUser customUser,  
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("uploadBusinessList");

        String groupId = request.getParameter("groupId");
        try{
            if(filePart != null){
                String currentUser = customUser.getAccount();
                
                Map<String, Object> result = businessUIService.uploadBusinessList(filePart, currentUser, new Date(), groupId);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Upload Business List Null");
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getBusinessList")
    @ResponseBody
    public ResponseEntity<?> getBusinessList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getBusinessList");

        String groupId = request.getParameter("groupId");
        List<Business> result = businessService.findByGroupId(groupId);
        
        logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteBusiness")
    @ResponseBody
    public ResponseEntity<?> deleteBusiness(
            @CurrentUser CustomUser customUser,
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("deleteBusiness");
        String id = request.getParameter("id");
        
        try{
            if(StringUtils.isNotBlank(id)){
                logger.info("id:" + id);
                businessUIService.deleteFromUI(Long.parseLong(id), customUser.getAccount());
                
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            }
            else{
                throw new Exception("Id Null");
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getBusiness")
    @ResponseBody
    public ResponseEntity<?> getBusiness(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getBusiness");        
        String id = request.getParameter("id");
        
        try{
            if(StringUtils.isNotBlank(id)){
                logger.info("id:" + id);
                Business business = businessService.findOne(Long.parseLong(id));
                
                if(business != null){
                    return new ResponseEntity<>(business, HttpStatus.OK);
                }
            }
            
            throw new Exception("Id Null");
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
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/createBusiness", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createBusiness(@RequestBody Business business,
            @CurrentUser CustomUser customUser,   
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("createBusiness");
        
        try{
            
            if(business != null){
                if(StringUtils.isBlank(business.getBizId())){
                    throw new Exception("BizId Null");
                } else if (StringUtils.isBlank(business.getGroupId())){
                    throw new Exception("GroupId Null");
                }
                
                String account = customUser.getAccount();
                
                Business result = businessUIService.saveFromUI(business, account);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Business Null");
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


    @RequestMapping(method = RequestMethod.GET, value = "/admin/businessGroupCreatePage")
    public String businessGroupCreatePage(
            HttpServletRequest request, 
            HttpServletResponse response) throws Exception {
        logger.info("businessGroupCreatePage");
                
        return BcsPageEnum.BusinessGroupCreatePage.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/businessGroupListPage")
    public String businessGroupListPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("businessGroupListPage");
        return BcsPageEnum.BusinessGroupListPage.toString();
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/admin/getBusinessGroupList")
    @ResponseBody
    public ResponseEntity<?> getBusinessGroupList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getBusinessGroupList");

        List<BusinessGroup> result = businessGroupService.getAllList();
        
        logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    @RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteBusinessGroup")
    @ResponseBody
    public ResponseEntity<?> deleteBusinessGroup(
            @CurrentUser CustomUser customUser,
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("deleteBusinessGroup");
        String groupId = request.getParameter("groupId");
        
        try{
            if(StringUtils.isNotBlank(groupId)){
                logger.info("groupId:" + groupId);
                businessGroupUIService.deleteFromUI(groupId, customUser.getAccount());
                
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            }
            else{
                throw new Exception("BusinessGroup Id Null");
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
    
    @RequestMapping(method = RequestMethod.GET, value = "/admin/getBusinessGroup")
    @ResponseBody
    public ResponseEntity<?> getBusinessGroup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getBusinessGroup");        
        String groupId = request.getParameter("groupId");
        
        try{
            if(StringUtils.isNotBlank(groupId)){
                logger.info("groupId:" + groupId);
                BusinessGroup businessGroup = businessGroupService.findOne(groupId);
                
                if(businessGroup != null){
                    return new ResponseEntity<>(businessGroup, HttpStatus.OK);
                }
            }
            
            throw new Exception("BusinessGroup Id Null");
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
    
    @RequestMapping(method = RequestMethod.POST, value = "/admin/createBusinessGroup", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createBusinessGroup(@RequestBody BusinessGroup businessGroup,
            @CurrentUser CustomUser customUser,   
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("createBusinessGroup");
        
        try{
            if(businessGroup != null){
                if(StringUtils.isBlank(businessGroup.getGroupName())){
                    throw new Exception("GroupName Null");
                }
                
                String account = customUser.getAccount();
                
                BusinessGroup result = businessGroupUIService.saveFromUI(businessGroup, account);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("BusinessGroup Null");
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
}
