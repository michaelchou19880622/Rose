package com.bcs.web.ui.controller;

import java.io.IOException;
import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.service.CampaignService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.CampaignUIService;


@Controller
@RequestMapping("/bcs")
public class BCSCampaignController extends BCSBaseController {
	
	@Autowired
	private CampaignService campaignService;
	@Autowired
	private CampaignUIService campaignUIService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSCampaignController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/admin/campaignCreatePage")
	public String campaignCreatePage(
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		logger.info("campaignCreatePage");
				
		return BcsPageEnum.CampaignCreatePage.toString();
	}

    @RequestMapping(method = RequestMethod.GET, value = "/admin/campaignListPage")
    public String campaignListPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("campaignListPage");
        return BcsPageEnum.CampaignListPage.toString();
    }
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getCampaignList")
    @ResponseBody
    public ResponseEntity<?> getCampaignList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getCampaignList");
        
        String isActive = request.getParameter("isActive");

        List<Campaign> result = campaignService.findByIsActive(Boolean.valueOf(isActive));
        
        logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
	
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteCampaign")
    @ResponseBody
    public ResponseEntity<?> deleteCampaign(
            @CurrentUser CustomUser customUser,
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("deleteCampaign");
        String campaignId = request.getParameter("campaignId");
        
        try{
            if(StringUtils.isNotBlank(campaignId)){
                logger.info("campaignId:" + campaignId);
                campaignUIService.deleteFromUI(campaignId, customUser.getAccount());
                
                return new ResponseEntity<>("Delete Success", HttpStatus.OK);
            }
            else{
                throw new Exception("Campaign Id Null");
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/admin/getCampaign")
    @ResponseBody
    public ResponseEntity<?> getCampaign(HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("getCampaign");        
        String campaignId = request.getParameter("campaignId");
        
        try{
            if(StringUtils.isNotBlank(campaignId)){
                logger.info("campaignId:" + campaignId);
                Campaign campaign = campaignService.findOne(campaignId);
                
                if(campaign != null){
                    return new ResponseEntity<>(campaign, HttpStatus.OK);
                }
            }
            
            throw new Exception("Campaign Id Null");
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
	
	@RequestMapping(method = RequestMethod.POST, value = "/admin/createCampaign", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createCampaign(@RequestBody Campaign campaign,
            @CurrentUser CustomUser customUser,   
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("createCampaign");
        
        try{
            
            if(campaign != null){
                if(StringUtils.isBlank(campaign.getCampaignName())){
                    throw new Exception("CampaignName Null");
                }
                
                String account = customUser.getAccount();
                
                Campaign result = campaignUIService.saveFromUI(campaign, account);
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Campaign Null");
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
	
	@RequestMapping(method = RequestMethod.DELETE, value ="/admin/redesignCampaign")
    @ResponseBody
    public ResponseEntity<?> redesignCampaign(
            @CurrentUser CustomUser customUser, 
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("redesignCampaign");

        String campaignId = request.getParameter("campaignId");
        
        try{
            if(StringUtils.isNotBlank(campaignId)){
                logger.info("campaignId:" + campaignId);
                campaignUIService.switchIsActive(campaignId, customUser.getAccount());
                
                return new ResponseEntity<>("Change Success", HttpStatus.OK);
            }
            else{
                logger.error("campaignId Null");
                throw new BcsNoticeException("請選擇正確的訊息");
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
