package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bcs.core.aspect.annotation.WebServiceLog;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bcs.core.db.entity.SystemConfig;
import com.bcs.core.db.service.SystemConfigService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.taishin.api.model.BillingNoticeTemplateMsgModel;
import com.bcs.core.taishin.api.model.TemplateActionModel;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentLink;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsg;
import com.bcs.core.taishin.circle.db.entity.BillingNoticeContentTemplateMsgAction;
import com.bcs.core.taishin.circle.service.BillingNoticeContentTemplateMsgService;
import com.bcs.core.taishin.service.ExportToExcelForBillingNoticePushBNApiEffects;
import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.ui.service.LoadFileUIService;


@Controller
@RequestMapping("/bcs")
public class BCSBillingNoticeTemplateMsgController {
	@Autowired
	private BillingNoticeContentTemplateMsgService contentTemplateMsgService;

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private ExportToExcelForBillingNoticePushBNApiEffects exportToExcelForBillingNoticePushBNApiEffects;

	/** Logger */
	private static Logger logger = Logger.getLogger(BCSBillingNoticeTemplateMsgController.class);

	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/edit/billingNoticeCreatePage")
	public String templateMsgCreatePage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("billingNoticeCreatePage");
		return BcsPageEnum.BillingNoticeCreatePage.toString();
	}

	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/edit/billingNoticeListPage")
	public String templateMsgListPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("billingNoticeListPage");
		return BcsPageEnum.BillingNoticeListPage.toString();
	}

	/**
	 * 新增與更新樣板訊息
	 */
	@WebServiceLog
//	@ControllerLog(description="新增與更新樣帳務通知板訊息")
	@RequestMapping(method = RequestMethod.POST, value = "/edit/createBillingNotice", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createTemplateMsg(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestBody List<BillingNoticeTemplateMsgModel> createTemplateMsgModels,
			@RequestParam String actionType,
			@RequestParam String templateId) throws IOException {

		logger.info("createTemplateMsgModels:"+createTemplateMsgModels);

		try {

			if (!validateData(createTemplateMsgModels)) {
				throw new BcsNoticeException("必填欄位不可為空！");
			}

			String adminUserAccount = customUser.getAccount(); //取得登入者的帳號

			//初始化
			//儲存用的List
			List<BillingNoticeContentTemplateMsg> contentTemplateMsgs = new ArrayList<>();
			List<BillingNoticeContentTemplateMsgAction> contentTemplateMsgActions = new ArrayList<>();
			List<BillingNoticeContentLink> contentLinks = new ArrayList<>();
			//先前的IdList
			List<String> templateIds = new ArrayList<>();
			List<Map<String, String>> actionIdAndLinkIds;

			BillingNoticeContentTemplateMsg contentTemplateMsg; //儲存格式
			BillingNoticeContentLink contentLink; //儲存格式
			BillingNoticeContentTemplateMsgAction contentTemplateMsgAction; //儲存格式
			String templateParentId = "";
			String linkId = "";


			// set old template with same Title's productSwitch to off
			boolean productSwitch = createTemplateMsgModels.get(0).isTemplateSwitch();
			String templateTitle = createTemplateMsgModels.get(0).getTemplateTitle();
			if(productSwitch) {
				logger.info("ProdutionSwith is On, other's will set to off!");
				contentTemplateMsgService.setPreTitleTemplateToOff(templateTitle, adminUserAccount);
			}

			// Edit
			if (actionType.equals("Edit")) {
				// set old template with same Title to deleted
				contentTemplateMsgService.getPreTemplateIds(templateId, adminUserAccount);
				templateIds.add(templateId);
				for(int i=1; i<createTemplateMsgModels.size(); i++){
					templateIds.add(checkDuplicateUUID("1"));
				}
			// Add & Copy
			} else {
				for(int i=0; i<createTemplateMsgModels.size(); i++){
					templateIds.add(checkDuplicateUUID("1"));
				}
			}

			//取出每個templateMsg的資料
			BillingNoticeTemplateMsgModel createTemplateMsgModel;
			TemplateActionModel templateActionModel;
			Map<String, String> map;
			templateParentId = templateIds.get(0);


			// for every pages
			for(int i = 0; i < createTemplateMsgModels.size(); i++){
				createTemplateMsgModel = createTemplateMsgModels.get(i);

				System.out.println("model " + i + ": " + createTemplateMsgModel.toString());

				// 新增樣板
				if (i + 1 > templateIds.size()) {
					templateIds.add(checkDuplicateUUID("1"));
				}

				// ------------- adding ----------------------

				//template的資料
				contentTemplateMsg = new BillingNoticeContentTemplateMsg();

				// i	TEMPLATE_ID
				contentTemplateMsg.setTemplateId(templateIds.get(i));
				// 0	PRODUCT_SWITCH
				contentTemplateMsg.setProductSwitch(createTemplateMsgModel.isTemplateSwitch());

				//
				// 1 ALT_TEXT
				if(!createTemplateMsgModel.getAltText().isEmpty()){
					contentTemplateMsg.setAltText(createTemplateMsgModel.getAltText());
				}
				// 2 TEMPLATE_TYPE
				contentTemplateMsg.setTemplateType(createTemplateMsgModel.getTemplateType());

				// 5 CURFEW_START_TIME + 6 CURFEW_END_TIME
				//logger.info("Curfew Time: " + createTemplateMsgModel.getCurfewStartTime().toString() + ", " + createTemplateMsgModel.getCurfewEndTime().toString());
				contentTemplateMsg.setCurfewStartTime(createTemplateMsgModel.getCurfewStartTime());
				contentTemplateMsg.setCurfewEndTime(createTemplateMsgModel.getCurfewEndTime());
				//logger.info("Curfew Time: " + contentTemplateMsg.getCurfewStartTime().toString() + ", " + contentTemplateMsg.getCurfewEndTime().toString());

				// 7 TEMPLATE_TEXT
				contentTemplateMsg.setTemplateText(createTemplateMsgModel.getTemplateText());

				contentTemplateMsg.setTemplateLevel(createTemplateMsgModel.getTemplateLevel());

				contentTemplateMsg.setModifyUser(adminUserAccount);
				contentTemplateMsg.setModifyTime(new Date());
				contentTemplateMsg.setStatus(BillingNoticeContentTemplateMsg.STATUS_ACTIVE);
				contentTemplateMsg.setTemplateLetter(createTemplateMsgModel.getTemplateLetter());

				if(i > 0){
					contentTemplateMsg.setTemplateParentId(templateParentId);
				}

				if(!createTemplateMsgModel.getTemplateImageId().isEmpty()){
					contentTemplateMsg.setTemplateImageId(createTemplateMsgModel.getTemplateImageId());
				}
				if(!createTemplateMsgModel.getTemplateTitle().isEmpty()){
					contentTemplateMsg.setTemplateTitle(createTemplateMsgModel.getTemplateTitle());
				}

				contentTemplateMsgs.add(contentTemplateMsg);


				// 取出template的action
				List<TemplateActionModel> templateActions = createTemplateMsgModel.getTemplateActions();
				if (actionType.equals("Edit")) { //變更
					contentTemplateMsgService.getPreActionIdAndLinkId(templateIds.get(i)); //取得原先圖文訊息的DetailId與LinkId
				}

				//新增與複制
				actionIdAndLinkIds = new ArrayList<>();
				for(int k=0; k<templateActions.size(); k++){
					map = new LinkedHashMap<>();
					map.put("actionId", checkDuplicateUUID("2"));
					map.put("linkId", null);
					actionIdAndLinkIds.add(map);
				}

				for(int j=0; j<templateActions.size(); j++){
					templateActionModel = templateActions.get(j);

					if(j+1>actionIdAndLinkIds.size()){
						map = new LinkedHashMap<>();
						map.put("actionId", checkDuplicateUUID("2"));
						map.put("linkId", null);
						actionIdAndLinkIds.add(map);
					}

					linkId = actionIdAndLinkIds.get(j).get("linkId");
					//action資料
					contentTemplateMsgAction = new BillingNoticeContentTemplateMsgAction();
					contentTemplateMsgAction.setTemplateIdAction(actionIdAndLinkIds.get(j).get("actionId"));
					contentTemplateMsgAction.setTemplateId(templateIds.get(i));
					contentTemplateMsgAction.setActionLetter(templateActionModel.getActionLetter());
					contentTemplateMsgAction.setActionType(templateActionModel.getActionType());
					contentTemplateMsgAction.setActionLabel(templateActionModel.getActionLabel());
					contentTemplateMsgAction.setStatus(BillingNoticeContentTemplateMsgAction.STATUS_ACTIVE);

					switch(templateActionModel.getActionType()){
						case BillingNoticeContentTemplateMsgAction.ACTION_TYPE_URI:
							contentLink = new BillingNoticeContentLink();
							if(linkId==null){
								linkId = checkDuplicateUUID("3");
							}
							contentLink.setLinkId(linkId);
							contentLink.setLinkTitle(templateActionModel.getActionLabel());
							contentLink.setLinkUrl(templateActionModel.getActionText());
							contentLink.setModifyTime(new Date());
							contentLink.setModifyUser(adminUserAccount);
							contentLinks.add(contentLink);
							contentTemplateMsgAction.setLinkId(linkId);
							break;
						case BillingNoticeContentTemplateMsgAction.ACTION_TYPE_MESSAGE:
							contentTemplateMsgAction.setActionText(templateActionModel.getActionText());
							break;
						case BillingNoticeContentTemplateMsgAction.ACTION_TYPE_POSTBACK:
							//contentTemplateMsgAction.setActionText(templateActionModel.getActionText());
							contentTemplateMsgAction.setActionData(templateActionModel.getActionData());
							break;
					}

					contentTemplateMsgActions.add(contentTemplateMsgAction);
				}
			}

			//
			System.out.println("final: "+contentTemplateMsgs.toString());

			contentTemplateMsgService.createTemplateMsg(contentTemplateMsgs, contentTemplateMsgActions, contentLinks);

			return new ResponseEntity<>("save success", HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));

			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}

	/**
	 * 取得樣板訊息
	 */
	@WebServiceLog
//	@ControllerLog(description="取得帳務通知樣板訊息")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBillingNotice/{templateId}")
	@ResponseBody
	public ResponseEntity<?> getTemplateMsg(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String templateId) throws IOException {
		logger.info("getBillingNotice TemplateMsg");

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getContentTemplateMsg(templateId);

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得樣板訊息列表
	 */
	@WebServiceLog
//	@ControllerLog(description="取得帳務通知樣板訊息列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBillingNoticeAllList")
	@ResponseBody
	public ResponseEntity<?> getAllMsgList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getBillingNoticeList");

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getAllContentTemplateMsg();

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得ProductionOn樣板訊息列表
	 */
	@WebServiceLog
//	@ControllerLog(description="取得帳務通知ProductionOn樣板訊息列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBillingNoticeOnList")
	@ResponseBody
	public ResponseEntity<?> getOnMsgList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getBillingNoticeOnList");

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getProductOnContentTemplateMsg();

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得ProductionOff樣板訊息列表
	 */
	@WebServiceLog
//	@ControllerLog(description="取得帳務通知ProductionOn樣板訊息列表")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBillingNoticeOffList")
	@ResponseBody
	public ResponseEntity<?> getOffMsgList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("getBillingNoticeOffList");

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getProductOffContentTemplateMsg();

			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	/**
	 * 取得帳務通知成效Total
	 */
	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBNEffectsTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getBNEffectsTotalPages(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam(value = "startDate", required=false) String startDate,
			@RequestParam(value = "endDate", required=false) String endDate) throws IOException {
		logger.info("getBNEffectsTotalPages, startDate=" + startDate + " endDate=" + endDate);
		if(startDate == null) startDate = "1911-01-01";
		if(endDate == null) endDate = "3099-01-01";

		try{
			String count = contentTemplateMsgService.getBNEffectsTotalPages(startDate, endDate);
			logger.info("getBNEffectsTotalPages, startDate=" + startDate + " endDate=" + endDate + " totalPages=" + count);
			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + count + "\"}", HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得帳務通知成效清單
	 */
	@WebServiceLog
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBNEffectsList")
	@ResponseBody
	public ResponseEntity<?> getBNEffectsList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam(value = "startDate", required=false) String startDate,
			@RequestParam(value = "endDate", required=false) String endDate,
			@RequestParam(value = "page", required=false) Integer page) throws IOException {
		logger.info("getBNEffectsList, startDate=" + startDate + " endDate=" + endDate + " page=" + page);
		if(startDate == null) startDate = "1911-01-01";
		if(endDate == null) endDate = "3099-01-01";

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getBNEffects(startDate, endDate, page);
			logger.info("getBNEffectsList, page=" + page + " sizeOfList=" + result.size());
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得帳務通知明細成效清單
	 */
	@WebServiceLog
//	@ControllerLog(description="取得帳務通知成效清單")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBNEffectsDetailTotalPages", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getBNEffectsDetailTotalPages(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam  String date,
			@RequestParam  String templateName,
			@RequestParam  String sendType) throws IOException {
		try{
			String count = contentTemplateMsgService.getBNEffectsDetailTotalPages(date, templateName, sendType);
			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + count + "\"}", HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 取得帳務通知明細成效清單
	 */
	@WebServiceLog
//	@ControllerLog(description="取得帳務通知成效清單")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBNEffectsDetailList")
	@ResponseBody
	public ResponseEntity<?> getBNEffectsDetailList(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@RequestParam  String date,
			@RequestParam  String templateName,
			@RequestParam  String sendType,
			@RequestParam(value = "page", required=false) Integer page) throws IOException {
		logger.info("page1: " + page);

		try{
			Map<String, List<String>> result = contentTemplateMsgService.getBNEffectsDetail(date, templateName, sendType, page);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		catch(Exception e){
			logger.error(ErrorRecord.recordError(e));

			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 檢查必填欄位不可為空/get
	 */
	public Boolean validateData(List<BillingNoticeTemplateMsgModel> createTemplateMsgModels) {
		BillingNoticeTemplateMsgModel createTemplateMsgModel;
		TemplateActionModel templateActionModel;
		boolean imageIsEmpty = createTemplateMsgModels.get(0).getTemplateImageId().isEmpty();
		boolean titleIsEmpty = createTemplateMsgModels.get(0).getTemplateTitle().isEmpty();
		String actionType;

		for(int i=0; i<createTemplateMsgModels.size(); i++){
			createTemplateMsgModel = createTemplateMsgModels.get(i);

			if(createTemplateMsgModel.getTemplateImageId().isEmpty() ^ imageIsEmpty){
				return false;
			}

			if(createTemplateMsgModel.getTemplateTitle().isEmpty() ^ titleIsEmpty){
				return false;
			}

			if(createTemplateMsgModel.getAltText().isEmpty() && i==0){
				return false;
			}

			List<TemplateActionModel> templateActions = createTemplateMsgModel.getTemplateActions();
			for(int j=0; j<templateActions.size(); j++){
				templateActionModel = templateActions.get(j);
				actionType = templateActionModel.getActionType();

				if(templateActionModel.getActionLabel().isEmpty()){
					return false;
				}

				switch(actionType){
					case "uri" :
						if(templateActionModel.getActionText().isEmpty()){
							return false;
						}
						break;
					case "message" :
						if(templateActionModel.getActionText().isEmpty()){
							return false;
						}
						break;
					case "postback" :
						if(templateActionModel.getActionData().isEmpty()){
							return false;
						}
						break;
				}
			}
		}
		return true;
	}

	/**
	 * 回傳一個沒有重覆的uuid
	 */
	public String checkDuplicateUUID(String queryType) {
		String uuid = UUID.randomUUID().toString().toLowerCase();
		boolean duplicateUUID = contentTemplateMsgService.checkDuplicateUUID(queryType, uuid);
		while (duplicateUUID) {
			uuid = UUID.randomUUID().toString().toLowerCase();
			duplicateUUID = contentTemplateMsgService.checkDuplicateUUID(queryType, uuid);
		}

		return uuid;
	}

	/**
	 * 刪除樣板訊息
	 */
	@WebServiceLog
//	@ControllerLog(description="刪除帳務通知樣板訊息")
	@RequestMapping(method = RequestMethod.DELETE, value = "/admin/deleteBillingNotice/{templateId}")
	@ResponseBody
	public ResponseEntity<?> deleteTemplateMsg(
			HttpServletRequest request,
			HttpServletResponse response,
			@CurrentUser CustomUser customUser,
			@PathVariable String templateId) {
		logger.info("deleteBillingNotice");

		try {
			// Check Delete Right
			boolean isAdmin = customUser.isAdmin();
			if(isAdmin) {
				contentTemplateMsgService.deleteTemplateMsg(templateId, customUser.getAccount());
				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
			} else {
				throw new BcsNoticeException("此帳號沒有刪除權限");
			}
		} catch(Exception e) {
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
	 * 取得BigSwitch
	 */
	@WebServiceLog
//	@ControllerLog(description="取得大開關")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/getBillingNoticeBigSwitch", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> getBillingNoticeBigSwitch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("getBillingNoticeBigSwitch");
		try {
			logger.info(".bn.bigswitch = " + "." + CONFIG_STR.BN_BIGSWITCH.toString());
			SystemConfig systemConfig = systemConfigService.findSystemConfig("." + CONFIG_STR.BN_BIGSWITCH.toString());
            String bigSwitch = CoreConfigReader.getString(CONFIG_STR.BN_BIGSWITCH, false);
            if (systemConfig != null) {
                bigSwitch = systemConfig.getValue();
            }
            logger.info("bigSwitch:" + bigSwitch);
            return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + bigSwitch + "\"}", HttpStatus.OK);
		} catch(Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 設置BigSwitch
	 */
	@WebServiceLog
//	@ControllerLog(description="設置大開關")
	@RequestMapping(method = RequestMethod.GET, value = "/edit/setBillingNoticeBigSwitch/{OnOff}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<?> setBillingNoticeBigSwitch(HttpServletRequest request, HttpServletResponse response,  @PathVariable String OnOff) {
		logger.info("setBillingNoticeBigSwitch, OnOff=" + OnOff);
		try {
			logger.info(".bn.bigswitch = " + "." + CONFIG_STR.BN_BIGSWITCH.toString());
			SystemConfig systemConfig = systemConfigService.findSystemConfig("." + CONFIG_STR.BN_BIGSWITCH.toString());
			if (systemConfig == null) {
				systemConfig = new SystemConfig();
				systemConfig.setConfigId("." + CONFIG_STR.BN_BIGSWITCH.toString());
				systemConfig.setDescription("BigSwitch");
			}
			systemConfig.setValue(OnOff);
			systemConfig.setModifyTime(Calendar.getInstance().getTime());
			systemConfigService.save(systemConfig);

			logger.info("bigSwitch:" + systemConfig.getValue());
			return new ResponseEntity<>("{\"result\": 1, \"msg\": \"" + systemConfig.getValue() + "\"}", HttpStatus.OK);
		} catch(Exception e) {
			logger.error(ErrorRecord.recordError(e));
			return new ResponseEntity<>("{\"result\": 0, \"msg\": \"" + e.getMessage() + "\"}", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
     * 匯出 Push API 成效報表
     */
	@WebServiceLog
//	@ControllerLog(description="匯出 BN Push API 成效報表")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForBNPushApiEffects")
    @ResponseBody
    public void exportToExcelForBNPushApiEffects(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser, @RequestParam String startDate, @RequestParam String endDate) {

		// file path
        String filePath = CoreConfigReader.getString("file.path");

        // file name
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		Date date = new Date();
        String fileName = "BNPushApiEffects_" + sdf.format(date) + ".xlsx";

        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForBillingNoticePushBNApiEffects.exportExcel(filePath, fileName, startDate, endDate);
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
	@WebServiceLog
//	@ControllerLog(description="匯出 BN Push API 成效報表")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForBNPushApiEffectsDetail")
    @ResponseBody
    public void exportToExcelForBNPushApiEffectsDetail(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
    		@RequestParam String date, @RequestParam String title, @RequestParam String sendType) {

		// file path
        //String filePath = "C:\\bcs\\";
        String filePath = CoreConfigReader.getString("file.path");

        // file name
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        String fileName = "BNPushApiEffects_" + sdf.format(new Date()) + ".xlsx";

        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForBillingNoticePushBNApiEffects.exportExcel(filePath, fileName, date, title, sendType);
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

