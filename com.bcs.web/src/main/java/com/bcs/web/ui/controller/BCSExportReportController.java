package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.bot.report.export.ExportToExcelForContentPushReport;
import com.bcs.core.bot.report.export.ExportToExcelForKeywordReport;
import com.bcs.core.report.export.ExportToExcelForCampaignUserList;
import com.bcs.core.report.export.ExportToExcelForCouponReport;
import com.bcs.core.report.export.ExportToExcelForLinkClickReport;
import com.bcs.core.report.export.ExportToExcelForPageVisitReport;
import com.bcs.core.report.export.ExportToExcelForPushApiEffects;
import com.bcs.core.report.export.ExportToExcelForRewardCardReport;
import com.bcs.core.report.export.ExportToExcelForWinnerList;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.MobilePageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.service.LoadFileUIService;


@Controller
@RequestMapping("/bcs")
public class BCSExportReportController extends BCSBaseController {
	@Autowired
	private ExportToExcelForContentPushReport exportToExcelForContentPushReport;
	@Autowired
	private ExportToExcelForKeywordReport exportToExcelForKeywordReport;
	@Autowired
	private ExportToExcelForLinkClickReport exportToExcelForLinkClickReport;
	@Autowired
	private ExportToExcelForPageVisitReport exportToExcelForPageVisitReport;
	@Autowired
	private ExportToExcelForCouponReport exportToExcelForCouponReport;
    @Autowired
    private ExportToExcelForWinnerList exportToExcelForWinnerList;
    @Autowired
    private ExportToExcelForCampaignUserList exportToExcelForCampaignUserList;
    @Autowired 
    private ExportToExcelForRewardCardReport exportToExcelForRewardCardReport;
    @Autowired 
    private ExportToExcelForPushApiEffects exportToExcelForPushApiEffects;
    
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSExportReportController.class);
	
	/**
	 * 匯出 Push Report EXCEL
	 */
	@ControllerLog(description="匯出 Push Report EXCEL")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForPushReport")
	@ResponseBody
	public void exportToExcelForPushReport(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
			@RequestParam String endDate
			) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = "PushReportList_" + sdf.format(date) + ".xlsx";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToExcelForContentPushReport.exportToExcel(filePath, startDate, endDate, fileName);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}

		LoadFileUIService.loadFileToResponse(filePath, fileName, response);
	}
	
	/**
	 * 匯出Keyword Report EXCEL
	 */
	@ControllerLog(description="匯出Keyword Report EXCEL")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForKeywordReport")
	@ResponseBody
	public void exportToExcelForKeywordReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
			@RequestParam String endDate,
			@RequestParam Long iMsgId,
			@RequestParam String userStatus
			) throws IOException{

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = "KeywordReportList_" + sdf.format(date) + ".xlsx";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToExcelForKeywordReport.exportToExcelForKeywordReport(filePath, fileName, startDate, endDate, iMsgId, userStatus);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}

		LoadFileUIService.loadFileToResponse(filePath, fileName, response);
	}
	
	/**
	 * 匯出 Link Click Report EXCEL
	 */
	@ControllerLog(description="匯出 Link Click Report EXCEL")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForLinkClickReport")
	@ResponseBody
	public void exportToExcelForLinkClickReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
			@RequestParam String endDate,
			@RequestParam String linkUrl
			) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = "LinkUrlClickReportList_" + sdf.format(date) + ".xlsx";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToExcelForLinkClickReport.exportToExcelForLinkClickReport(filePath, fileName, startDate, endDate, linkUrl);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}

		LoadFileUIService.loadFileToResponse(filePath, fileName, response);
	}
	
	/**
	 * 匯出Page Visit Report EXCEL
	 */
	@ControllerLog(description="匯出Page Visit Report EXCEL")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForPageVisitReport")
	@ResponseBody
	public void exportToExcelForPageVisitReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
			@RequestParam String endDate,
			@RequestParam String pageUrl
			) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = "PageVisitReportList_" + sdf.format(date) + ".xlsx";
		try {
			MobilePageEnum page = MobilePageEnum.valueOf(pageUrl);
			
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToExcelForPageVisitReport.exportToExcelForPageVisitReport(filePath, fileName, startDate, endDate, pageUrl, page.getTitle());
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}

		LoadFileUIService.loadFileToResponse(filePath, fileName, response);
	}
	
	/**
	 * 匯出 Coupon Report EXCEL
	 */
	@ControllerLog(description="匯出 Coupon Report EXCEL")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForCouponReport")
	@ResponseBody
	public void exportToExcelForCouponReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
			@RequestParam String endDate,
			@RequestParam String couponId) throws IOException{
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
		Date date = new Date();
		String fileName = "CouponReportList_" + sdf.format(date) + ".xlsx";
		try {
			File folder = new File(filePath);
			if(!folder.exists()){
				folder.mkdirs();
			}
			
			exportToExcelForCouponReport.exportToExcelForCouponReport(filePath, fileName, startDate, endDate, couponId);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
		}

		LoadFileUIService.loadFileToResponse(filePath, fileName, response);
	}
    
    /**
     * 匯出 Winner List
     */
	@ControllerLog(description="匯出 Winner List")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForWinnerList")
    @ResponseBody
    public void exportToExcelForWinnerList(
    		HttpServletRequest request, 
    		HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
    		@RequestParam String gameId,
    		@RequestParam(required=false) Optional<String> couponPrizeId,
    		@RequestParam String startDate,
			@RequestParam String endDate,
			@RequestParam(required=false) Optional<Integer> pageIndex
			) throws IOException{
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "WinnerList_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            
            exportToExcelForWinnerList.exportToExcelForWinnerList(filePath, fileName, startDate, endDate, gameId, couponPrizeId,pageIndex);
            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    }
    
    /**
     * 匯出 Winner List By CouponId
     */
	@ControllerLog(description="匯出 Winner List By CouponId")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForWinnerListByCouponId")
    @ResponseBody
    public void exportToExcelForWinnerListByCouponId(
    		HttpServletRequest request, 
    		HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
    		@RequestParam String couponId,
    		@RequestParam String startDate,
    		@RequestParam String endDate,
    		@RequestParam(required=false) Optional<Integer> pageIndex) throws IOException{
        logger.info("exportToExcelForWinnerListByCouponId");
    	logger.info("couponId:"+couponId);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "WinnerListByCouponId_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            
            exportToExcelForWinnerList.exportToExcelForWinnerListByCouponId(filePath, fileName, startDate, endDate, couponId,pageIndex);
            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    }
    
    /**
     * 匯出Campaign User List
     */
	@ControllerLog(description="匯出Campaign User List")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForCampaignUserList")
    @ResponseBody
    public void exportToExcelForCampaignUserList(
    		HttpServletRequest request, 
    		HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
    		@RequestParam String iMsgId,
    		@RequestParam String prizeId,
    		@RequestParam String startDate,
    		@RequestParam String endDate) throws IOException{
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "CampaignUserList_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            
            exportToExcelForCampaignUserList.exportToExcelForCampaignUserList(filePath, fileName, startDate, endDate, iMsgId, prizeId);
            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    }
    
    /**
     * 匯出單張 RewardCard Record
     */
	@ControllerLog(description="匯出單張 RewardCard Record")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForRewardRecord")
    @ResponseBody
    public void exportToExcelForRewardRecord(
    		HttpServletRequest request, 
    		HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
    		@RequestParam String rewardCardId,
    		@RequestParam String startDate,
    		@RequestParam String endDate,
    		@RequestParam(required=false) Optional<Integer> pageIndex) throws IOException{
        logger.info("exportToExcelForRewardRecord");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "RewardCardRecordList_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForRewardCardReport.exportToExcelForWinnerList(filePath, fileName, startDate, endDate, rewardCardId,pageIndex);            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    }
    
    /**
     * 匯出此 Reward Card 的各個優惠券紀錄
     */
	@ControllerLog(description="匯出此 Reward Card 的各個優惠券紀錄")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForRewardCardCouponRecord")
    @ResponseBody
    public void exportToExcelForRewardCardCouponRecord(
    		HttpServletRequest request, 
    		HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
    		@RequestParam String rewardCardId) throws IOException{
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "RewardCardCouponRecordList_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForRewardCardReport.exportToExcelForRewardCardCouponRecord(filePath, fileName,rewardCardId);            
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        LoadFileUIService.loadFileToResponse(filePath, fileName, response);
    }
	
	/**
     * 匯出 Push API 明細報表
     */
	@ControllerLog(description="匯出 Push API 明細報表")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForPushApiEffectDetail")
    @ResponseBody
    public void exportToExcelForPushApiEffectDetail(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String createTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "PushApiEffectDetail_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForPushApiEffects.exportExcel(filePath, fileName, createTime);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        try {
			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	/**
     * 匯出 Push API 成效報表
     */
	@ControllerLog(description="匯出 Push API 成效報表")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForPushApiEffects")
    @ResponseBody
    public void exportToExcelForPushApiEffects(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String startDate, @RequestParam String endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String filePath = CoreConfigReader.getString("file.path") + System.getProperty("file.separator") + "REPORT";
        Date date = new Date();
        String fileName = "PushApiEffects_" + sdf.format(date) + ".xlsx";
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForPushApiEffects.exportExcel(filePath, fileName, startDate, endDate);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        try {
			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
