package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.ShareCampaign;
import com.bcs.core.db.service.ShareCampaignService;
import com.bcs.core.db.service.ShareUserRecordService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.report.export.ExportReportForMGM;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.resource.UriHelper;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.LoadFileUIService;
import com.bcs.web.ui.service.ShareCampaignUIService;


@Controller
@RequestMapping("/bcs")
public class BCSShareCampaignController extends BCSBaseController {
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSShareCampaignController.class);

	@Autowired
	private ShareCampaignUIService shareCampaignUIService;
	@Autowired
    private ExportReportForMGM exportReportForMGM;
	@Autowired
	private ShareCampaignService shareCampaignService;
	@Autowired
	private ShareUserRecordService shareUserRecordService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/shareCampaignCreatePage")
	public String shareCampaignCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("shareCampaignCreatePage");
		return BcsPageEnum.ShareCampaignCreatePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/shareCampaignListPage")
	public String shareCampaignListPage(HttpServletRequest request, HttpServletResponse response, Model model) {
		logger.info("shareCampaignListPage");
		model.addAttribute("mgmTracingUrlPre", UriHelper.getMgmTracingUrl());
		return BcsPageEnum.ShareCampaignListPage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/shareCampaignListDisablePage")
	public String shareCampaignListDisablePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("shareCampaignListDisablePage");		
		return BcsPageEnum.ShareCampaignListDisablePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/shareCampaignReportPage")
	public String shareCampaignReportPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("shareCampaignReportPage");		
		return BcsPageEnum.ShareCampaignReportPage.toString();
	}
	
	/**
	 * 查詢MGM列表 SHARE_CAMPAIGN_STATUS_ACTIVE
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getShareCampaignList")
	@ResponseBody
	public ResponseEntity<?> getShareCampaignList(
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("getShareCampaignList");
		
		try {

            List<ShareCampaign> shareCampaigns = shareCampaignService.findByStatus(ShareCampaign.STATUS_ACTIVE);

			return new ResponseEntity<>(shareCampaigns, HttpStatus.OK);
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
	
	/**
	 * 查詢MGM列表 SHARE_CAMPAIGN_STATUS_DISABLE
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getShareCampaignListDisable")
	@ResponseBody
	public ResponseEntity<?> getShareCampaignListDisable(
			HttpServletRequest request, HttpServletResponse response) {
		logger.info("getShareCampaignListDisable");
		
		try {

            List<ShareCampaign> shareCampaigns = shareCampaignService.findByStatus(ShareCampaign.STATUS_DISABLE);
 
            return new ResponseEntity<>(shareCampaigns, HttpStatus.OK);
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

	/**
	 * 取得MGM活動
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getShareCampaign")
	@ResponseBody
	public ResponseEntity<?> getShareCampaign(
			@RequestParam String campaignId,  
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("getShareCampaign");				
		
		try{
			if(campaignId != null){
				logger.info("campaignId:" + campaignId);
				ShareCampaign shareCampaign = shareCampaignService.findOne(campaignId);
				
				if(shareCampaign != null){
					return new ResponseEntity<>(shareCampaign, HttpStatus.OK);
				}
			}
			
			throw new Exception("ShareCampaign Null");
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
	
	/**
	 * 新增或修改MGM
	 * 
	 * @param shareCampaign
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/edit/saveShareCampaign", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> saveShareCampaign(
			@RequestBody ShareCampaign shareCampaign, 
			@CurrentUser CustomUser customUser, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("saveShareCampaign");
		
		try{
		    shareCampaignUIService.checkShareCampaign(shareCampaign);
			shareCampaignUIService.saveFromUI(shareCampaign, customUser.getAccount());
			return new ResponseEntity<>(null, HttpStatus.OK);
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
	
	/**
	 * 刪除MGM活動
	 * 
	 * @param campaignId
	 * @param customUser
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteShareCampaign")
	@ResponseBody
	public ResponseEntity<?> deleteShareCampaign(
			@RequestParam String campaignId, 
			@CurrentUser CustomUser customUser,
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("deleteShareCampaign");
		
		try{
			shareCampaignUIService.deleteFromUI(campaignId, customUser.getAccount());
			return new ResponseEntity<>("Delete Success", HttpStatus.OK);
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

	/**
	 * 改變狀態
	 * 
	 * @param customUser
	 * @param request
	 * @param response
	 * @return String
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.DELETE, value ="/edit/redesignShareCampaign")
	@ResponseBody
	public ResponseEntity<?> redesignShareCampaign(
			@CurrentUser CustomUser customUser, 
			HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		logger.info("redesignShareCampaign");

		String campaignId = request.getParameter("campaignId");
		
		try{
			if(StringUtils.isNotBlank(campaignId)){
				logger.info("campaignId:" + campaignId);
				shareCampaignUIService.switchShareCampaignStatus(campaignId, customUser.getAccount());
				
				return new ResponseEntity<>("Change Success", HttpStatus.OK);
			}
			else{
				logger.error("campaignId Null");
				throw new BcsNoticeException("請選擇正確的MGM活動");
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

	@RequestMapping(method = RequestMethod.GET, value = "/edit/countShareUserRecord")
    @ResponseBody
    public ResponseEntity<?> countShareUserRecord(  
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        logger.info("countShareUserRecord");                
        String campaignId = request.getParameter("campaignId");
        
        try{ 
            if(campaignId != null) {
                Integer result = shareUserRecordService.countByCampaignId(campaignId);
                
                if(result != null) {
                    return new ResponseEntity<>(result, HttpStatus.OK);
                }
            }
         
            throw new Exception("result Null");
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
	
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForShareUserRecord")
    @ResponseBody
    public void exportToExcelForShareUserRecord(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String campaignId = request.getParameter("campaignId");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String reportType = request.getParameter("reportType");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        
        Date date = new Date();
        String fileName = sdf.format(date) + ".xlsx";

        if(ExportReportForMGM.REPORT_TYPE_COMPLETED.equals(reportType)) {
            fileName = "MGM_COMPLETED_"+ fileName;
        }
        else if(ExportReportForMGM.REPORT_TYPE_UNCOMPLETED.equals(reportType)) {
            fileName = "MGM_UNCOMPLETED_"+ fileName;
        }
        else {
            fileName = "MGM_DETAIL_"+ fileName;
        }
        
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            
            exportReportForMGM.exportToExcel(filePath, fileName, startDate, endDate, campaignId, reportType);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    }
}
