package com.bcs.web.ui.controller;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
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

import com.bcs.core.utils.ErrorRecord;
import com.bcs.core.utils.ObjectUtil;
import com.bcs.core.db.service.ContentRichMsgService;
import com.bcs.core.db.service.ContentTemplateMsgService;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.web.security.CurrentUser;
import com.bcs.core.web.security.CustomUser;
import com.bcs.core.web.ui.controller.BCSBaseController;
import com.bcs.core.web.ui.page.enums.BcsPageEnum;
import com.bcs.web.aop.ControllerLog;

import com.bcs.web.ui.service.LinePointUIService;
import com.bcs.web.ui.service.LoadFileUIService;
import com.bcs.core.linepoint.api.model.LinePointPushModel;
import com.bcs.core.linepoint.akka.service.LinePointPushAkkaService;
import com.bcs.core.linepoint.db.entity.LinePointDetail;
import com.bcs.core.linepoint.db.entity.LinePointMain;
import com.bcs.core.linepoint.db.entity.LinePointScheduledDetail;
import com.bcs.core.linepoint.db.service.ExportToExcelForLinePointPushApiEffects;

@Controller
@RequestMapping("/bcs")
public class BCSLinePointController extends BCSBaseController {

	@Autowired
	private LinePointUIService linePointUIService;
	@Autowired
	private LinePointPushAkkaService linePointPushAkkaService;
	@Autowired
	private ExportToExcelForLinePointPushApiEffects exportToExcelForLinePointPushApiEffects;
	@Autowired
	private ContentRichMsgService contentRichMsgService;
	@Autowired
	private ContentTemplateMsgService contentTemplateMsgService;
	
	/** Logger */
	private static Logger logger = Logger.getLogger(BCSLinePointController.class);
	
	@ControllerLog(description = "建立 Line Point 活動")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/linePointCreatePage")
	public String linePointCreatePage(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
		logger.info("linePointCreatePage");
		return BcsPageEnum.LinePointCreatePage.toString();
	}

	@ControllerLog(description = "Line Point 活動列表")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/linePointListPage")
	public String linePointListPage(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
		logger.info("linePointListPage");
		return BcsPageEnum.LinePointListPage.toString();
	}

	@ControllerLog(description = "發送 Line Point 活動")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/linePointSendPage")
	public String linePointSendPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("linePointSendPage");
		return BcsPageEnum.LinePointSendPage.toString();
	}

	@ControllerLog(description = "Line Point 活動報表")
	@RequestMapping(method = RequestMethod.GET, value = "/lpQuerier/linePointReportPage")
	public String linePointReportPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("linePointReportPage");
		return BcsPageEnum.LinePointReportPage.toString();
	}

	@ControllerLog(description = "Line Point 活動明細")
	@RequestMapping(method = RequestMethod.GET, value = "/lpQuerier/linePointDetailPage")
	public String linePointDetailPage(HttpServletRequest request, HttpServletResponse response) {
		logger.info("linePointDetailPage");
		return BcsPageEnum.LinePointDetailPage.toString();
	}
	
	@ControllerLog(description = "Add/Edit Line Point Main")
	@RequestMapping(method = RequestMethod.POST, value = "/lpCreator/createLinePointMain", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> createLinePointMain(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @RequestBody LinePointMain linePointMain) throws IOException {
		logger.info("createLinePointMain");
		try {
			if (linePointMain != null) {
				String adminUserAccount = customUser.getAccount();
				LinePointMain result = linePointUIService.saveLinePointMainFromUI(linePointMain, adminUserAccount);
				return new ResponseEntity<>(result, HttpStatus.OK);
			} else 
				throw new Exception("LinePointMain is Null");
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ControllerLog(description = "Get All Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getAllLinePointMainList")
	@ResponseBody
	public ResponseEntity<?> getAllLinePointMainList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("[getAllLinePointMainList]");
		List<LinePointMain> result = new ArrayList();
		List<LinePointMain> list = linePointUIService.linePointMainFindAll();		
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ControllerLog(description = "Get Manual Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getManualLinePointMainList")
	@ResponseBody
	public ResponseEntity<?> getManualLinePointMainList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("[getManualLinePointMainList]");
		List<LinePointMain> result = new ArrayList();
		List<LinePointMain> list = linePointUIService.linePointMainFindManual();
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ControllerLog(description = "Get Auto Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getAutoLinePointMainList")
	@ResponseBody
	public ResponseEntity<?> getAutoLinePointMainList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser) throws IOException {
		logger.info("[getAutoLinePointMainList]");
		List<LinePointMain> result = new ArrayList();
		List<LinePointMain> list = linePointUIService.linePointMainFindAuto();
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	//----
	@ControllerLog(description = "Get All Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getAllLinePointMainListSearch/{searchText}")
	@ResponseBody
	public ResponseEntity<?> getAllLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
		logger.info("[findAllLinePointMainList]");
		try {
			List<LinePointMain> result = new ArrayList();
			List<LinePointMain> list = linePointUIService.linePointMainFindAll(searchText);
			result.addAll(list);
			logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
			return new ResponseEntity<>(result, HttpStatus.OK);
		}catch(Exception e) {
			logger.info("Error1: " + e.getMessage());
			return new ResponseEntity<>("Error1: " + e.getMessage(), HttpStatus.OK);
		}

	}

	@ControllerLog(description = "Get Manual Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getManualLinePointMainListSearch/{searchText}")
	@ResponseBody
	public ResponseEntity<?> getManualLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
		logger.info("[getManualLinePointMainListSearch]");
		List<LinePointMain> result = new ArrayList();
		List<LinePointMain> list = linePointUIService.linePointMainFindManual(searchText);
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ControllerLog(description = "Get Auto Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getAutoLinePointMainListSearch/{searchText}")
	@ResponseBody
	public ResponseEntity<?> getAutoLinePointMainListSearch(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @PathVariable String searchText) throws IOException {
		logger.info("[getAutoLinePointMainListSearch]");
		List<LinePointMain> result = new ArrayList();
		List<LinePointMain> list = linePointUIService.linePointMainFindAuto(searchText);
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	//----
	@ControllerLog(description = "Get Undone Manual Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getUndoneManualLinePointMainList")
	@ResponseBody
	public ResponseEntity<?> getUndoneManualLinePointMainList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser ) throws IOException {
		logger.info("getUndoneManualLinePointMainList");
		List<LinePointMain> result = new ArrayList();
		List<LinePointMain> list = linePointUIService.linePointMainFindUndoneManual();
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ControllerLog(description = "Get Undone Auto Line Point Main")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getUndoneAutoLinePointMainList")
	@ResponseBody
	public ResponseEntity<?> getUndoneAutoLinePointMainList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser ) throws IOException {
		logger.info("getUndoneAutoLinePointMainList");
		List<LinePointMain> result = new ArrayList();
		List<LinePointMain> list = linePointUIService.linePointMainFindUndoneAuto();
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ControllerLog(description = "Get Success Line Point Detail")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getSuccessLinePointDetailList/{linePointMainId}")
	@ResponseBody
	public ResponseEntity<?> getSuccessLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @PathVariable String linePointMainId) throws IOException {
		logger.info("getSuccessLinePointDetailList");
		Long mainId = Long.parseLong(linePointMainId);
		
		List<LinePointDetail> result = new ArrayList();
		List<LinePointDetail> list = linePointUIService.findSuccess(mainId);
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@ControllerLog(description = "Get Fail Line Point Detail")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getFailLinePointDetailList/{linePointMainId}")
	@ResponseBody
	public ResponseEntity<?> getFailLinePointDetailList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @PathVariable String linePointMainId) throws IOException {
		logger.info("getFailLinePointDetailList");
		Long mainId = Long.parseLong(linePointMainId);
		
		List<LinePointDetail> result = new ArrayList();
		List<LinePointDetail> list = linePointUIService.findFail(mainId);
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ControllerLog(description = "Get Success Line Point Detail")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getLinePointScheduledDetailList/{mainId}")
	@ResponseBody
	public ResponseEntity<?> getLinePointScheduledDetailList(HttpServletRequest request, HttpServletResponse response,
			@CurrentUser CustomUser customUser, @PathVariable Long mainId) throws IOException {
		logger.info("getLinePointScheduledDetailList");

		List<LinePointScheduledDetail> result = new ArrayList();
		List<LinePointScheduledDetail> list = linePointUIService.findScheduledDetailList(mainId);
		result.addAll(list);
		logger.debug("result:" + ObjectUtil.objectToJsonStr(result));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
	
	@ControllerLog(description="Delete Line Point Main")
	@RequestMapping(method = RequestMethod.DELETE, value = "/lpCreator/deleteLinePointMain")
	@ResponseBody
	public ResponseEntity<?> deleteLinePointMain( HttpServletRequest request,  HttpServletResponse response, @CurrentUser CustomUser customUser,
			@RequestParam(required=false) String campaignId, @RequestParam(required=false) String listType) throws IOException {
		logger.info("deleteLinePointMain");
		try{
			if(StringUtils.isNotBlank(campaignId)){
				logger.info("campaignId:" + campaignId);
				linePointUIService.deleteFromUI(Long.parseLong(campaignId), customUser.getAccount(), listType);
				return new ResponseEntity<>("Delete Success", HttpStatus.OK);
			} else
				throw new Exception("ID IS NULL");
		} catch(Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException)
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@ControllerLog(description = "Push Line Point")
	@RequestMapping(method = RequestMethod.POST, value = "/lpCreator/pushLinePoint", consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<?> pushLinePoint(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
			@RequestBody List<String> uids, @RequestParam Long eventId) throws IOException {
		try {
				JSONArray uid = new JSONArray();
				for(String u : uids) {
					uid.put(u);
				}
				LinePointMain linePointMain = linePointUIService.linePointMainFindOne(eventId);				
				LinePointPushModel linePointPushModel = new LinePointPushModel();
				linePointPushModel.setAmount(linePointMain.getAmount());
				linePointPushModel.setUid(uid);
				linePointPushModel.setEventId(eventId);
				linePointPushModel.setSource(LinePointPushModel.SOURCE_TYPE_BCS);
				linePointPushModel.setSendTimeType(LinePointPushModel.SEND_TYPE_IMMEDIATE);
				linePointPushModel.setTriggerTime(new Date());
				linePointPushAkkaService.tell(linePointPushModel);
				return new ResponseEntity<>("",HttpStatus.OK);
		} catch (Exception e) {
			logger.error(ErrorRecord.recordError(e));
			if (e instanceof BcsNoticeException) 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			else 
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
     * 匯出 Push API 成效報表
     */
	@ControllerLog(description="匯出Line Point Push API 成效報表")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForLPPushApiEffects")
    @ResponseBody
    public void exportToExcelForLPPushApiEffects(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser) {
      
		// file path
        String filePath = CoreConfigReader.getString("file.path");
        
        // file name
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		Date date = new Date();
        String fileName = "LPPushApiEffects_" + sdf.format(date) + ".xlsx";
        
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForLinePointPushApiEffects.exportExcel(filePath, fileName);
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
	@ControllerLog(description="匯出Line Point Push API Detail 成效報表")
    @RequestMapping(method = RequestMethod.GET, value = "/edit/exportToExcelForLPPushApiEffectsDetail/{mainId}/{status}")
    @ResponseBody
    public void exportToExcelForLPPushApiEffectsDetail(HttpServletRequest request, HttpServletResponse response, 
    		@CurrentUser CustomUser customUser, @PathVariable Long mainId, @PathVariable String status) {
      
		// file path
        String filePath = CoreConfigReader.getString("file.path");
        
        // file name
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		Date date = new Date();
        String fileName = "LPPushApiEffectsDetail_" + sdf.format(date) + ".xlsx";
        
        try {
            File folder = new File(filePath);
            if(!folder.exists()){
                folder.mkdirs();
            }
            exportToExcelForLinePointPushApiEffects.exportExcel(filePath, fileName, mainId, status);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
        }

        try {
			LoadFileUIService.loadFileToResponse(filePath, fileName, response);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	// --- get Send Message
	@ControllerLog(description="取得圖文訊息")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getRichMsg/{richId}")
	@ResponseBody
	public ResponseEntity<?> getRichMsg(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
			@PathVariable String richId) throws IOException {
		logger.info("lpCreator getRichMsg");
		try{
			Map<String, List<String>> result = contentRichMsgService.getContentRichMsg(richId);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));
			if(e instanceof BcsNoticeException){
				return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_IMPLEMENTED);
			}else{
				return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@ControllerLog(description="取得樣板訊息")
	@RequestMapping(method = RequestMethod.GET, value = "/lpCreator/getTemplateMsg/{templateId}")
	@ResponseBody
	public ResponseEntity<?> getTemplateMsg(HttpServletRequest request, HttpServletResponse response, @CurrentUser CustomUser customUser,
			@PathVariable String templateId) throws IOException {
		logger.info("lpCreator getTemplateMsg");
		try{
			Map<String, List<String>> result = contentTemplateMsgService.getContentTemplateMsg(templateId);
			return new ResponseEntity<>(result, HttpStatus.OK);
		}catch(Exception e){
			logger.error(ErrorRecord.recordError(e));	
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}