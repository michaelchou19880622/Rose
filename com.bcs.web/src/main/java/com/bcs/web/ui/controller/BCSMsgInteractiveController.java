package com.bcs.web.ui.controller;

import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.bcs.core.bot.db.service.MsgBotReceiveService;
import com.bcs.core.bot.report.service.InteractiveReportService;
import com.bcs.core.db.entity.AdminUser;
import com.bcs.core.db.entity.Campaign;
import com.bcs.core.db.entity.LineUser;
import com.bcs.core.db.entity.MsgDetail;
import com.bcs.core.db.entity.MsgInteractiveCampaign;
import com.bcs.core.db.entity.MsgInteractiveDetail;
import com.bcs.core.db.entity.MsgInteractiveMain;
import com.bcs.core.db.service.AdminUserService;
import com.bcs.core.db.service.CampaignService;
import com.bcs.core.db.service.MsgInteractiveCampaignService;
import com.bcs.core.db.service.MsgInteractiveDetailService;
import com.bcs.core.db.service.MsgInteractiveMainService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;
import com.bcs.web.ui.model.InteractiveMsgModel;
import com.bcs.web.ui.service.ExportExcelUIService;
import com.bcs.web.ui.service.InteractiveMsgUIService;
import com.bcs.web.ui.service.SendMsgUIService;


@Controller
@RequestMapping("/bcs")
public class BCSMsgInteractiveController extends BCSBaseController {

	@Autowired
	private MsgInteractiveMainService msgInteractiveMainService;
	@Autowired
	private SendMsgUIService sendMsgUIService;
	@Autowired
	private InteractiveMsgUIService interactiveMsgUIService;
	@Autowired
	private AdminUserService adminUserService;
	@Autowired
	private MsgInteractiveDetailService msgInteractiveDetailService;
	@Autowired
	private InteractiveReportService interactiveReportService;
	@Autowired
	private MsgBotReceiveService msgBotReceiveService;
	@Autowired
	private ExportExcelUIService exportExcelUIService;
	@Autowired
	private MsgInteractiveCampaignService msgInteractiveCampaignService;
	@Autowired
	private CampaignService campaignService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSMsgInteractiveController.class);

	@RequestMapping(method = RequestMethod.GET, value = "/edit/interactiveResponsePage")
	public String interactiveResponsePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("interactiveResponsePage");
		return BcsPageEnum.InteractiveResponsePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/interactiveResponseDisablePage")
	public String interactiveResponseDisablePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("interactiveResponseDisablePage");
		return BcsPageEnum.InteractiveResponseDisablePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/interactiveResponseCreatePage")
	public String interactiveResponseCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("interactiveResponseCreatePage");
		return BcsPageEnum.InteractiveResponseCreatePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/keywordResponsePage")
	public String keywordResponsePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("keywordResponsePage");
		return BcsPageEnum.KeywordResponsePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/keywordResponseDisablePage")
	public String keywordResponseDisablePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("keywordResponseDisablePage");
		return BcsPageEnum.KeywordResponseDisablePage.toString();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/keywordResponseExpirePage")
    public String keywordResponseExpirePage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("keywordResponseExpirePage");
        return BcsPageEnum.KeywordResponseExpirePage.toString();
    }
	
	@RequestMapping(method = RequestMethod.GET, value = "/edit/keywordResponseIneffectivePage")
    public String keywordResponseIneffectiveBtnPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("keywordResponseIneffectiveBtnPage");
        return BcsPageEnum.KeywordResponseIneffectiveBtnPage.toString();
    }

	@RequestMapping(method = RequestMethod.GET, value = "/edit/keywordResponseCreatePage")
	public String keywordResponseCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("keywordResponseCreatePage");
		return BcsPageEnum.KeywordResponseCreatePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/blackKeywordResponsePage")
	public String blackKeywordResponsePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("blackKeywordResponsePage");
		return BcsPageEnum.BlackKeywordResponsePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/blackKeywordResponseDisablePage")
	public String blackKeywordResponseDisablePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("blackKeywordResponseDisablePage");
		return BcsPageEnum.BlackKeywordResponseDisablePage.toString();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/edit/blackKeywordResponseCreatePage")
	public String blackKeywordResponseCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("blackKeywordResponseCreatePage");
		return BcsPageEnum.BlackKeywordResponseCreatePage.toString();
	}

    @RequestMapping(method = RequestMethod.GET, value = "/edit/campaignResponsePage")
    public String campaignResponsePage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("campaignResponsePage");
        return BcsPageEnum.CampaignResponsePage.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/campaignResponseDisablePage")
    public String campaignResponseDisablePage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("campaignResponseDisablePage");
        return BcsPageEnum.CampaignResponseDisablePage.toString();
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/edit/campaignResponseCreatePage")
    public String campaignResponseCreatePage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("campaignResponseCreatePage");
        return BcsPageEnum.CampaignResponseCreatePage.toString();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/campaignUserListPage")
    public String campaignUserListPage(HttpServletRequest request, HttpServletResponse response) {
        logger.info("campaignUserListPage");
        return BcsPageEnum.CampaignUserListPage.toString();
    }
    
	/**
	 * 取得 訊息資料列表
	 * 
	 * @param request
	 * @param response
	 * @return Map<String, Object>
	 * @throws IOException
	 */
    @ControllerLog(description="訊息資料列表")
	@RequestMapping(method = RequestMethod.GET, value ="/edit/getInteractiveMsgList")
	@ResponseBody
	public ResponseEntity<?> getInteractiveMsgList(
			HttpServletRequest request, 
			HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
    		@RequestParam String type,
    		@RequestParam String status) throws IOException {
		logger.info("getInteractiveMsgList");

		try{
			if(StringUtils.isNotBlank(type)){
				logger.info("type:" + type);
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				
				Map<MsgInteractiveMain, List<MsgDetail>> map = null;
				if("EXPIRE".equals(status) || "INEFFECTIVE".equals(status)) {
				    map = msgInteractiveMainService.queryGetMsgInteractiveMainDetailByType(type);
				}else {
				    map = msgInteractiveMainService.queryGetMsgInteractiveMainDetailByTypeAndStatus(type, status);
				}
				
				if(map != null){
				    //分析是否過期or未生效
				    map = analyzeMsg(map, status);

					result.put("MsgMain", map);

					/**
					 * SendGroup Result
					 */
					sendMsgUIService.setGroups(result);

					/**
					 * AdminUser Result Map
					 */
					try{
						Map<String, AdminUser> admins = adminUserService.findAllMap();
//						Map<Long, Long> distinctMap = new HashMap<Long, Long>();
						Map<String, String> adminMap = new HashMap<String, String>();
						for(MsgInteractiveMain msg : map.keySet()){
							Long iMsgId = msg.getiMsgId();
							String userAccount = msg.getModifyUser();
							if(admins.containsKey(userAccount)){
								adminMap.put(userAccount, admins.get(userAccount).getUserName());
							}

							/**
							 * Set MsgInteractiveDetail list
							 */
							List<MsgInteractiveDetail> list =msgInteractiveDetailService.findByiMsgId(iMsgId);
							sendMsgUIService.setResourceMap(result, "iMsgId-" + iMsgId, list);
							
							// countInteractiveResponseDistinct
//							distinctMap.put(iMsgId, msgInteractiveMainService.countInteractiveResponseDistinct(iMsgId, true));
						}
						result.put("AdminUser", adminMap);
//						result.put("DistinctMap", distinctMap);
					}
					catch(Exception e){
						logger.error(ErrorRecord.recordError(e));
					}

					/**
					 * Set Detail Content
					 */
					for(List<MsgDetail> details : map.values()){
						sendMsgUIService.setDetailContent(result, details);
					}
					
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
			}
			
//			throw new Exception("type Null");
			logger.error("type Null");
			throw new BcsNoticeException("查詢參數錯誤");
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
	 * 取得 訊息資料 by iMsgId
	 * 
	 * @param request
	 * @param response
	 * @return Map<String, Object>
	 * @throws IOException
	 */
    @ControllerLog(description="取得訊息資料 by iMsgId")
	@RequestMapping(method = RequestMethod.GET, value ="/edit/getInteractiveMsg")
	@ResponseBody
	public ResponseEntity<?> getInteractiveMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
    		@RequestParam String iMsgId) throws IOException {
		logger.info("getInteractiveMsg");
		
		try{
			if(StringUtils.isNotBlank(iMsgId)){
				logger.info("iMsgId:" + iMsgId);
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				
				Map<MsgInteractiveMain, List<MsgDetail>> map = msgInteractiveMainService.queryGetMsgInteractiveMainDetailByMsgId(Long.parseLong(iMsgId));

				if(map != null && map.size() == 1){
					result.put("MsgMain", map);
					
					/**
					 * Set Detail Content
					 */
					for(List<MsgDetail> details : map.values()){
						sendMsgUIService.setDetailContent(result, details);
					}
					
					/**
					 * Set MsgInteractiveDetail list
					 */
					List<MsgInteractiveDetail> list =msgInteractiveDetailService.findByiMsgId(Long.parseLong(iMsgId));
					sendMsgUIService.setResourceMap(result, "iMsgId-" + iMsgId, list);
					
					MsgInteractiveCampaign msgCampaign = msgInteractiveCampaignService.findByiMsgId(Long.parseLong(iMsgId));
					interactiveMsgUIService.setResourceMap(result, "MsgCampaignMap", "iMsgId-" + iMsgId, msgCampaign);
					
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
			}
			
//			throw new Exception("iMsgId Null");
			logger.error("iMsgId Null");
			throw new BcsNoticeException("查詢參數錯誤");
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
	 * 取得 黑名單訊息資料列表
	 * 
	 * @param request
	 * @param response
	 * @return Map<String, Object>
	 * @throws IOException
	 */
    @ControllerLog(description="取得黑名單訊息資料列表")
	@RequestMapping(method = RequestMethod.GET, value ="/edit/getBlackInteractiveMsgList")
	@ResponseBody
	public ResponseEntity<?> getBlackInteractiveMsgList(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String type,
			@RequestParam String status) throws IOException {
		logger.info("getBlackInteractiveMsgList");

		try{
			if(StringUtils.isNotBlank(type)){
				logger.info("type:" + type);
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				
				List<MsgInteractiveMain> keywordList = msgInteractiveMainService.findByInteractiveTypeAndInteractiveStatus(type, status);
				
				if(keywordList != null){
					result.put("MsgMain", keywordList);

					/**
					 * AdminUser Result Map
					 */
					try{
						Map<String, AdminUser> admins = adminUserService.findAllMap();
						Map<String, String> adminMap = new HashMap<String, String>();
//						Map<Long, Long> distinctMap = new HashMap<Long, Long>();
						for(MsgInteractiveMain msg : keywordList){
							String userAccount = msg.getModifyUser();
							if(admins.containsKey(userAccount)){
								adminMap.put(userAccount, admins.get(userAccount).getUserName());
							}

							/**
							 * Set MsgInteractiveDetail list
							 */
							List<MsgInteractiveDetail> list =msgInteractiveDetailService.findByiMsgId(msg.getiMsgId());
							sendMsgUIService.setResourceMap(result, "iMsgId-" + msg.getiMsgId(), list);
							
							// countInteractiveResponseDistinct
//							distinctMap.put(msg.getiMsgId(), msgInteractiveMainService.countInteractiveResponseDistinct(msg.getiMsgId(), true));
						}
						result.put("AdminUser", adminMap);
//						result.put("DistinctMap", distinctMap);
					}
					catch(Exception e){
						logger.error(ErrorRecord.recordError(e));
					}
					
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
			}
			
//			throw new Exception("type Null");
			logger.error("type Null");
			throw new BcsNoticeException("查詢參數錯誤");
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
	 * 取得 黑名單訊息資料 by iMsgId
	 * 
	 * @param request
	 * @param response
	 * @return Map<String, Object>
	 * @throws IOException
	 */
    @ControllerLog(description="取得黑名單訊息資料 by iMsgId")
	@RequestMapping(method = RequestMethod.GET, value ="/edit/getBlackInteractiveMsg")
	@ResponseBody
	public ResponseEntity<?> getBlackInteractiveMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String iMsgId) throws IOException {
		logger.info("getBlackInteractiveMsg");
		
		try{
			if(StringUtils.isNotBlank(iMsgId)){
				logger.info("iMsgId:" + iMsgId);
				Map<String, Object> result = new LinkedHashMap<String, Object>();
				
				MsgInteractiveMain main = msgInteractiveMainService.findOne(Long.parseLong(iMsgId));

				if(main != null){
					result.put("MsgMain", main);
					
					/**
					 * Set MsgInteractiveDetail list
					 */
					List<MsgInteractiveDetail> list =msgInteractiveDetailService.findByiMsgId(Long.parseLong(iMsgId));
					sendMsgUIService.setResourceMap(result, "iMsgId-" + iMsgId, list);
					
					return new ResponseEntity<>(result, HttpStatus.OK);
				}
			}
			
//			throw new Exception("iMsgId Null");
			logger.error("iMsgId Null");
			throw new BcsNoticeException("查詢參數錯誤");
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
	
    @ControllerLog(description="getCampaignInteractiveMsgList")
	@RequestMapping(method = RequestMethod.GET, value ="/edit/getCampaignInteractiveMsgList")
    @ResponseBody
    public ResponseEntity<?> getCampaignInteractiveMsgList(
    		HttpServletRequest request, 
    		HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
			@RequestParam String type,
			@RequestParam String status) throws IOException {
        logger.info("getInteractiveMsgList");
        
        try{
            if(StringUtils.isNotBlank(type)){
                logger.info("type:" + type);
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                
                Map<MsgInteractiveMain, List<MsgDetail>> map = msgInteractiveMainService.queryGetMsgInteractiveMainDetailByTypeAndStatus(type, status);
                
                if(map != null){
                    result.put("MsgMain", map);

                    /**
                     * SendGroup Result
                     */
                    sendMsgUIService.setGroups(result);

                    /**
                     * AdminUser Result Map
                     */
                    try{
                        Map<String, AdminUser> admins = adminUserService.findAllMap();
                        Map<String, String> adminMap = new HashMap<String, String>();
                        for(MsgInteractiveMain msg : map.keySet()){
                            Long iMsgId = msg.getiMsgId();
                            String userAccount = msg.getModifyUser();
                            if(admins.containsKey(userAccount)){
                                adminMap.put(userAccount, admins.get(userAccount).getUserName());
                            }

                            /**
                             * Set MsgInteractiveDetail list
                             */
                            List<MsgInteractiveDetail> list =msgInteractiveDetailService.findByiMsgId(iMsgId);
                            sendMsgUIService.setResourceMap(result, "iMsgId-" + iMsgId, list);

                            MsgInteractiveCampaign msgCampaign = msgInteractiveCampaignService.findByiMsgId(iMsgId);
                            Campaign campaign = campaignService.findOne(msgCampaign.getCampaignId());
                            interactiveMsgUIService.setResourceMap(result, "CampaignMap", "iMsgId-" + iMsgId, campaign);
                        }
                        result.put("AdminUser", adminMap);
                    }
                    catch(Exception e){
                        logger.error(ErrorRecord.recordError(e));
                    }

                    /**
                     * Set Detail Content
                     */
                    for(List<MsgDetail> details : map.values()){
                        sendMsgUIService.setDetailContent(result, details);
                    }
                    
                    return new ResponseEntity<>(result, HttpStatus.OK);
                }
            }
            
//          throw new Exception("type Null");
            logger.error("type Null");
            throw new BcsNoticeException("查詢參數錯誤");
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
	 * 設定訊息
	 * 
	 * @param sendMsgModel
	 * @param customUser
	 * @param request
	 * @param response
	 * @return String Result
	 * @throws IOException
	 */
    @ControllerLog(description="設定訊息")
	@RequestMapping(method = RequestMethod.POST, value ="/edit/settingInteractiveMsg", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> settingInteractiveMsg(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser, 
			@RequestBody InteractiveMsgModel interactiveMsgModel			 
			) throws IOException {
		logger.info("settingInteractiveMsg");

		try{
			if(interactiveMsgModel != null){
				if(StringUtils.isBlank(interactiveMsgModel.getActionType())){
					throw new Exception("ActionType Null");
				}
				
				/**
				 * SaveSetting
				 */
				if(InteractiveMsgModel.ACTION_TYPE.SaveSetting.toString().equals(interactiveMsgModel.getActionType())){
					
					if(MsgInteractiveMain.INTERACTIVE_TYPE_KEYWORD.equals(interactiveMsgModel.getInteractiveType())){
						return saveSettingMsg(interactiveMsgModel, customUser);
					}
					else if(MsgInteractiveMain.INTERACTIVE_TYPE_BLACK_KEYWORD.equals(interactiveMsgModel.getInteractiveType())){
						return saveSettingMsg(interactiveMsgModel, customUser);
					}
                    else if(MsgInteractiveMain.INTERACTIVE_TYPE_INTERACTIVE.equals(interactiveMsgModel.getInteractiveType())){
                        return saveSettingMsg(interactiveMsgModel, customUser);
                    }
                    else if(MsgInteractiveMain.INTERACTIVE_TYPE_CAMPAIGN.equals(interactiveMsgModel.getInteractiveType())){
                        return saveSettingMsg(interactiveMsgModel, customUser);
                    }
					else{
						throw new Exception("Validate INTERACTIVE_TYPE Error");
					}
				}
				else{
					throw new Exception("Validate ActionType Error");
				}
			}
			else{
				throw new Exception("InteractiveMsgModel Null");
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

	/**
	 * 刪除訊息
	 * 
	 * @param customUser
	 * @param request
	 * @param response
	 * @return String
	 * @throws IOException
	 */
    @ControllerLog(description="刪除訊息")
	@RequestMapping(method = RequestMethod.DELETE, value ="/admin/deleteInteractiveMsg")
	@ResponseBody
	public ResponseEntity<?> deleteInteractiveMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String iMsgId) throws IOException {
		logger.info("deleteInteractiveMsg");
		
		// Check Delete Right
		boolean isAdmin = customUser.isAdmin();
		if(isAdmin){
			
			try{
				if(StringUtils.isNotBlank(iMsgId)){
					logger.info("iMsgId:" + iMsgId);
					interactiveMsgUIService.deleteMessageMain(Long.parseLong(iMsgId), customUser.getAccount());
					
					return new ResponseEntity<>("Delete Success", HttpStatus.OK);
				}
				else{
//					throw new Exception("iMsgId Null");
					logger.error("iMsgId Null");
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
		else{

			return new ResponseEntity<>("User No Delete Right", HttpStatus.OK);
		}
	}

	/**
	 * 改變訊息
	 * 
	 * @param customUser
	 * @param request
	 * @param response
	 * @return String
	 * @throws IOException
	 */
	@RequestMapping(method = RequestMethod.DELETE, value ="/edit/redesignInteractiveMsg")
	@ResponseBody
	public ResponseEntity<?> redesignInteractiveMsg(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String iMsgId) throws IOException {
		logger.info("redesignInteractiveMsg");
		
		try{
			if(StringUtils.isNotBlank(iMsgId)){
				logger.info("iMsgId:" + iMsgId);
				interactiveMsgUIService.switchMessageMainStatus(Long.parseLong(iMsgId), customUser.getAccount());
				
				return new ResponseEntity<>("Change Success", HttpStatus.OK);
			}
			else{
				logger.error("iMsgId Null");
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
	
	/**
	 * 傳送訊息
	 * 
	 * @param sendMsgModel
	 * @param customUser
	 * @return String
	 * @throws Exception
	 */
	private ResponseEntity<?> saveSettingMsg(InteractiveMsgModel interactiveMsgModel, CustomUser customUser) throws Exception{
		String adminUserAccount = customUser.getAccount();
		
		interactiveMsgUIService.saveSettingMessage(interactiveMsgModel, adminUserAccount);
		
		String result = "Send Message Success";
		
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ControllerLog(description="getKeywordOrInteractiveReport")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getKeywordOrInteractiveReport")
	@ResponseBody
	public ResponseEntity<?> getKeywordOrInteractiveReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
    		@RequestParam String endDate,
    		@RequestParam Long iMsgId,
    		@RequestParam String userStatus) {
		logger.info("getKeywordOrInteractiveReport");
		
		try {
			
			if (StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)) {
				Map<String, Map<String, BigInteger>> result = interactiveReportService.getInteractiveReport(startDate, endDate, iMsgId, userStatus);
				return new ResponseEntity<>(result, HttpStatus.OK);
			} else {
				if (StringUtils.isBlank(startDate)) {
					logger.error("startDate null");
					throw new BcsNoticeException("缺少查詢起始日期");
				} else {
					logger.error("endDate null");
					throw new BcsNoticeException("缺少查詢結束日期");
				}
			}
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			
			if(e instanceof BcsNoticeException) {
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			} else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	/**
	 * 匯出Keyword Report EXCEL MID
	 * @throws Exception 
	 */
	@ControllerLog(description="exportMidForKeywordReport")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/exportMidForKeywordReport")
	@ResponseBody
	public void exportMidForKeywordReport(
			HttpServletRequest request, 
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
    		@RequestParam String endDate,
    		@RequestParam Long iMsgId,
    		@RequestParam String userStatus,
    		@RequestParam String reportType) throws Exception{

		MsgInteractiveMain main = msgInteractiveMainService.findOne(iMsgId);
		
		if(main == null){
			throw new Exception("iMsgId Error");
		}
		
		if(StringUtils.isNotBlank(startDate) && StringUtils.isNotBlank(endDate)){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			Date timeEnd = sdf.parse(endDate);
			Calendar calendarEnd = Calendar.getInstance();
			calendarEnd.setTime(timeEnd);
			calendarEnd.add(Calendar.DATE, 1);

			List<String> titles = new ArrayList<String>();
			List<List<String>> data = new ArrayList<List<String>>();
			
			if (userStatus.equals(LineUser.STATUS_BINDED)) { //綁定(升級)
				List<String> binded = msgBotReceiveService.findReceiveMidByReferenceIdAndStatus(iMsgId.toString(), startDate, sdf.format(calendarEnd.getTime()), userStatus);
				data.add(binded);
				titles.add("升級使用者");
			}
			else if(userStatus.equals(LineUser.STATUS_UNBIND)){
				List<String> unbind = msgBotReceiveService.findReceiveMidByReferenceIdAndStatus(iMsgId.toString(), startDate, sdf.format(calendarEnd.getTime()), userStatus);
				data.add(unbind);
				titles.add("一般使用者");
			}
			else{
				
				List<String> unbind =msgBotReceiveService.findReceiveMidByReferenceIdAndStatus(iMsgId.toString(), startDate, sdf.format(calendarEnd.getTime()), LineUser.STATUS_UNBIND);
				data.add(unbind);
				titles.add("一般使用者");
				List<String> binded = msgBotReceiveService.findReceiveMidByReferenceIdAndStatus(iMsgId.toString(), startDate, sdf.format(calendarEnd.getTime()), LineUser.STATUS_BINDED);
				data.add(binded);
				titles.add("升級使用者");
			}
			
			String time = startDate + "~" + endDate;
			String title = main.getMainKeyword();
			if("Keyword".equals(reportType)){
				title += ":關鍵字";
			}
			else if("BlackKeyword".equals(reportType)){
				title += ":黑名單";
			}
			else if("Interactive".equals(reportType)){
				title += ":互動式";
			}

			exportExcelUIService.exportMidResultToExcel(request, response, "CouponReport", title , time, titles, data);
		}
		else{

			throw new Exception("Time Range Error");
		}
	}
    
	@ControllerLog(description="getCampaignUserList")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/getCampaignUserList")
    @ResponseBody
    public ResponseEntity<?> getCampaignUserList(
    		HttpServletRequest request,
    		HttpServletResponse response,
    		@CurrentUser CustomUser customUser,
			@RequestParam String startDate,
    		@RequestParam String endDate,
    		@RequestParam String iMsgId) throws IOException {
        logger.info("getCampaignUserList");

        List<Map<String, Object>> result = msgInteractiveCampaignService.findCampaignUserList(Long.valueOf(iMsgId), startDate, endDate);
        
        logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
	@ControllerLog(description="uploadMainKeywordList")
    @RequestMapping(method = RequestMethod.POST, value = "/edit/uploadMainKeywordList")
    @ResponseBody
    public ResponseEntity<?> uploadMainKeywordList(
    		HttpServletRequest request, 
            HttpServletResponse response,
            @CurrentUser CustomUser customUser, 
    		@RequestPart MultipartFile filePart
           ) throws Exception {
        logger.info("uploadMainKeywordList");
        
        try{
            if(filePart != null){
                String currentUser = customUser.getAccount();
                
                Map<String, List<String>> result = interactiveMsgUIService.uploadMainKeywordList(filePart, currentUser, new Date());
                
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            else{
                throw new Exception("Upload MainKeyword List Null");
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
    
    public Map<MsgInteractiveMain, List<MsgDetail>> analyzeMsg(Map<MsgInteractiveMain, List<MsgDetail>> map, String status){
        Calendar calendar = Calendar.getInstance();
        if(!"EXPIRE".equals(status)) {
            Iterator<Entry<MsgInteractiveMain, List<MsgDetail>>> iterator = map.entrySet().iterator();
            while(iterator.hasNext()) {
                Date endTime = iterator.next().getKey().getInteractiveEndTime();
                long endLong = 0L;
                int year = 0;
                if(endTime != null) {
                    endLong = endTime.getTime();
                    calendar.setTime(endTime);
                    year = calendar.get(Calendar.YEAR);
                }
                if(endTime != null && year != 1970 && endLong < new Date().getTime()) {
                    iterator.remove();
                }
            }
        }else {
            Iterator<Entry<MsgInteractiveMain, List<MsgDetail>>> iterator = map.entrySet().iterator();
            while(iterator.hasNext()) {
                Date endTime = iterator.next().getKey().getInteractiveEndTime();
                long endLong = 0L;
                int year = 0;
                if(endTime != null) {
                    endLong = endTime.getTime();
                    calendar.setTime(endTime);
                    year = calendar.get(Calendar.YEAR);
                }
                if(endTime == null || year == 1970 || endLong > new Date().getTime()) {
                    iterator.remove();
                }
            }
        }
        
        if(!"INEFFECTIVE".equals(status)) {
            Iterator<Entry<MsgInteractiveMain, List<MsgDetail>>> iterator = map.entrySet().iterator();
            while(iterator.hasNext()) {
                Date startTime = iterator.next().getKey().getInteractiveStartTime();
                long startLong = 0L;
                if(startTime != null) {
                    startLong = startTime.getTime();
                }
                if(startLong > new Date().getTime()) {
                    iterator.remove();
                }
            }
        }else {
            Iterator<Entry<MsgInteractiveMain, List<MsgDetail>>> iterator = map.entrySet().iterator();
            while(iterator.hasNext()) {
                Date startTime = iterator.next().getKey().getInteractiveStartTime();
                long startLong = 0L;
                int year = 0;
                if(startTime != null) {
                    startLong = startTime.getTime();
                    calendar.setTime(startTime);
                    year = calendar.get(Calendar.YEAR);
                }
                if(startTime == null || year == 1970 || startLong < new Date().getTime()) {
                    iterator.remove();
                }
            }
        }
        return map;
    }
}
